package com.quizi.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import com.quizi.util.ConfigManager; // [추가] 설정 관리자 임포트

public class OCRService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public WorkbookDTO extractQuestions(File imageFile, String dataPath) {
        System.out.println("🚀 [OCRService] OpenAI GPT-4o mini 이미지 분석 시작: " + imageFile.getName());
        WorkbookDTO wb = new WorkbookDTO();

        String base64Image = encodeFileToBase64(imageFile);
        if (base64Image == null) {
            return createErrorWorkbook("이미지 파일을 읽을 수 없습니다.");
        }

        String prompt = "You are an expert exam digitizer. Analyze the provided image which contains exam questions. " +
                "Extract all questions, options, and correct answers. " +
                "Also, infer the academic subject of this exam based on the content (e.g., Mathematics, History, IT, English). " +
                "Output ONLY a raw JSON object. " +
                "JSON structure: " +
                "{ " +
                "  \"subject\": \"Infer the subject in Korean\", " +
                "  \"questions\": [ " +
                "    { \"questionText\": \"...\", \"questionType\": \"multiple/short\", \"options\": [...], \"answerText\": \"...\", \"explanation\": \"...\", \"score\": 10 } " +
                "  ] " +
                "}";

        System.out.println("📡 [OCRService] OpenAI API 요청 전송 중...");
        String jsonResponse = callOpenAIVisionApi(prompt, base64Image);

        parseResponseToWorkbook(jsonResponse, wb);

        wb.setTitle("[GPT OCR] " + imageFile.getName());
        wb.setDescription("GPT-4o mini가 이미지를 분석하여 생성한 문제집입니다.");
        if (wb.getSubject() == null || wb.getSubject().isEmpty()) wb.setSubject("AI 추출");
        wb.setDifficulty("중");

        List<QuestionDTO> questions = wb.getQuestions();
        wb.setTimeLimit(questions == null || questions.isEmpty() ? 1 : questions.size() * 2);

        return wb;
    }

    private String encodeFileToBase64(File file) {
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String callOpenAIVisionApi(String promptText, String base64Image) {
        try {
            // [수정] ConfigManager를 통해 API Key를 안전하게 가져옴
            String apiKey = ConfigManager.getProperty("openai.api.key");

            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("❌ [OCRService] API Key가 설정되지 않았습니다. config.properties 또는 환경변수를 확인하세요.");
                return null;
            }

            // [보안 패치] 키 값의 공백과 따옴표 제거 (실수 방지)
            apiKey = apiKey.trim().replace("\"", "").replace("'", "");

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

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

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.add("content", contentArray);

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
                System.err.println("❌ [OCRService] API 오류 발생. 응답 코드: " + responseCode);

                // [추가] 401 인증 오류 시 명확한 안내 메시지 출력
                if (responseCode == 401) {
                    System.err.println("👉 API Key가 올바르지 않습니다. config.properties 파일의 키 값을 확인해주세요. (따옴표나 공백 주의)");
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) System.err.println(line);
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
            contentString = contentString.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonObject contentJson = JsonParser.parseString(contentString).getAsJsonObject();

            if (contentJson.has("subject")) wb.setSubject(contentJson.get("subject").getAsString());

            JsonArray questionsArray = null;
            if (contentJson.has("questions")) questionsArray = contentJson.getAsJsonArray("questions");
            else if (contentJson.has("exams")) questionsArray = contentJson.getAsJsonArray("exams");
            else if (contentJson.has("data")) questionsArray = contentJson.getAsJsonArray("data");
            else {
                for (Map.Entry<String, JsonElement> entry : contentJson.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        questionsArray = entry.getValue().getAsJsonArray();
                        break;
                    }
                }
            }

            if (questionsArray == null) {
                System.err.println("⚠️ [OCRService] JSON에서 배열 데이터를 찾을 수 없습니다.");
                questionsArray = new JsonArray();
            }

            Gson gson = new Gson();
            List<QuestionDTO> list = gson.fromJson(questionsArray, new TypeToken<List<QuestionDTO>>(){}.getType());

            long baseId = System.currentTimeMillis();
            for (int i = 0; i < list.size(); i++) {
                QuestionDTO q = list.get(i);
                q.setId(baseId + i);
                if (q.getScore() == 0) q.setScore(10);
            }
            wb.setQuestions(list);
            System.out.println("✅ [OCRService] 파싱 완료: " + list.size() + "문제 (Model: gpt-4o-mini)");

        } catch (Exception e) {
            System.err.println("❌ [OCRService] 파싱 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private WorkbookDTO createErrorWorkbook(String msg) {
        WorkbookDTO wb = new WorkbookDTO();
        wb.setTitle("오류 발생");
        wb.setDescription(msg);
        wb.setQuestions(new ArrayList<>());
        return wb;
    }
}