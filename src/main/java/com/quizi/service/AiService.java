package com.quizi.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.quizi.dto.QuestionDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.util.ConfigManager;

public class AiService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    // 1. 텍스트 주제로 생성
    public WorkbookDTO generateQuestions(String topic, int count, String difficulty) {
        WorkbookDTO wb = new WorkbookDTO();
        String prompt = createPrompt(topic, count, difficulty);
        System.out.println("📡 [AiService] 텍스트 기반 생성 요청: " + topic);
        String jsonResponse = callOpenAIApi(prompt, null);
        parseResponseToWorkbook(jsonResponse, wb);

        if (wb.getTitle() == null) wb.setTitle("[AI 생성] " + topic + " 정복하기");
        if (wb.getDescription() == null) wb.setDescription("AI가 생성한 '" + topic + "' 관련 문제입니다.");
        setCommonFields(wb, difficulty, count);
        return wb;
    }

    // 2. 파일(이미지) 내용으로 생성
    public WorkbookDTO generateQuestionsFromImage(File imageFile, int count, String difficulty) {
        WorkbookDTO wb = new WorkbookDTO();
        String base64Image = encodeFileToBase64(imageFile);

        if (base64Image == null) {
            System.err.println("❌ [AiService] 이미지 인코딩 실패");
            return null;
        }

        String prompt = "Analyze the provided study material (image). " +
                "Create " + count + " quiz questions based on the content of this image. " +
                "Difficulty: " + difficulty + ". " +
                "Output ONLY a raw JSON object. " +
                "IMPORTANT: All questions, options, answers, and explanations MUST be in Korean. " +
                "JSON structure: { \"subject\": \"Infer subject from content (in Korean)\", \"questions\": [ { \"questionText\": \"...\", \"questionType\": \"multiple/short\", \"options\": [...], \"answerText\": \"...\", \"explanation\": \"...\", \"score\": 10 } ] }";

        System.out.println("📡 [AiService] 이미지 기반 생성 요청: " + imageFile.getName());
        String jsonResponse = callOpenAIApi(prompt, base64Image);
        parseResponseToWorkbook(jsonResponse, wb);

        if (wb.getTitle() == null) wb.setTitle("[AI 파일생성] 학습 자료 퀴즈");
        if (wb.getDescription() == null) wb.setDescription("업로드한 학습 자료를 바탕으로 AI가 생성한 문제입니다.");
        setCommonFields(wb, difficulty, count);
        return wb;
    }

    private void setCommonFields(WorkbookDTO wb, String difficulty, int count) {
        if (wb.getSubject() == null) wb.setSubject("AI 생성");
        wb.setDifficulty(difficulty);
        wb.setTimeLimit(count * 2);
    }

    private String createPrompt(String topic, int count, String difficulty) {
        return "You are a quiz generator. Create " + count + " questions about '" + topic + "'." +
                "Difficulty: " + difficulty + "." +
                "Output ONLY a raw JSON object (not array). Do not use markdown code blocks. " +
                "IMPORTANT: All questions, options, answers, and explanations MUST be in Korean. " +
                "JSON structure: { \"subject\": \"Infer subject (in Korean)\", \"questions\": [ { \"questionText\": \"...\", \"questionType\": \"multiple/short\", \"options\": [...], \"answerText\": \"...\", \"explanation\": \"...\", \"score\": 10 } ] }";
    }

    // API 호출 (텍스트 모드 & 비전 모드 통합)
    private String callOpenAIApi(String promptText, String base64Image) {
        try {
            String apiKey = ConfigManager.getProperty("openai.api.key");

            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("❌ [AiService] API Key가 설정되지 않았습니다.");
                return null;
            }

            apiKey = apiKey.trim().replace("\"", "").replace("'", "");

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            // 메시지 구성
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");

            if (base64Image != null) {
                // [이미지 모드] 멀티모달 요청
                JsonArray contentArray = new JsonArray();

                JsonObject textObj = new JsonObject();
                textObj.addProperty("type", "text");
                textObj.addProperty("text", promptText + " Please return JSON only.");

                JsonObject imageObj = new JsonObject();
                imageObj.addProperty("type", "image_url");
                JsonObject imageUrl = new JsonObject();
                imageUrl.addProperty("url", "data:image/jpeg;base64," + base64Image);
                imageUrl.addProperty("detail", "auto");
                imageObj.add("image_url", imageUrl);

                contentArray.add(textObj);
                contentArray.add(imageObj);
                message.add("content", contentArray);
            } else {
                // [텍스트 모드] 단순 텍스트 요청
                message.addProperty("content", promptText + " Please return JSON only.");
            }

            JsonArray messages = new JsonArray();
            messages.add(message);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-4o-mini");
            requestBody.add("messages", messages);
            requestBody.addProperty("max_tokens", 4000);

            JsonObject format = new JsonObject();
            format.addProperty("type", "json_object");
            requestBody.add("response_format", format);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ [AiService] API 오류. 코드: " + responseCode);
                if (responseCode == 401) {
                    System.err.println("👉 API Key가 올바르지 않습니다. config.properties를 확인하세요.");
                }
                return null;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) response.append(responseLine.trim());
                return response.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void parseResponseToWorkbook(String apiResponse, WorkbookDTO wb) {
        if (apiResponse == null) return;
        try {
            JsonObject jsonObject = JsonParser.parseString(apiResponse).getAsJsonObject();
            String contentString = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();

            System.out.println("📝 [AiService] AI 답변:\n" + contentString);

            contentString = contentString.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonObject contentJson = JsonParser.parseString(contentString).getAsJsonObject();

            if (contentJson.has("subject")) wb.setSubject(contentJson.get("subject").getAsString());

            JsonArray questionsArray = null;
            if (contentJson.has("questions")) questionsArray = contentJson.getAsJsonArray("questions");
            else if (contentJson.has("data")) questionsArray = contentJson.getAsJsonArray("data");
            else {
                for (Map.Entry<String, JsonElement> entry : contentJson.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        questionsArray = entry.getValue().getAsJsonArray();
                        break;
                    }
                }
            }
            if (questionsArray == null) questionsArray = new JsonArray();

            Gson gson = new Gson();
            List<QuestionDTO> list = gson.fromJson(questionsArray, new TypeToken<List<QuestionDTO>>(){}.getType());
            long baseId = System.currentTimeMillis();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setId(baseId + i);
            }
            wb.setQuestions(list);
            System.out.println("✅ [AiService] 생성 완료: " + list.size() + "문제");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String encodeFileToBase64(File file) {
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) { return null; }
    }
}