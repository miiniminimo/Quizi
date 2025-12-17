package com.quizi.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import com.quizi.util.ConfigManager; // 추가

public class AiService {

    // [수정] 상수는 제거하고 메서드 내에서 ConfigManager 사용
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public WorkbookDTO generateQuestions(String topic, int count, String difficulty) {
        WorkbookDTO wb = new WorkbookDTO();
        String prompt = createPrompt(topic, count, difficulty);
        System.out.println("📡 [AiService] OpenAI API 요청 전송 중... (" + topic + ")");
        String jsonResponse = callOpenAIApi(prompt);
        parseResponseToWorkbook(jsonResponse, wb);

        if (wb.getTitle() == null) wb.setTitle("[AI 생성] " + topic + " 정복하기");
        if (wb.getDescription() == null) wb.setDescription("AI가 생성한 '" + topic + "' 관련 문제입니다.");
        if (wb.getSubject() == null) wb.setSubject("일반");
        wb.setDifficulty(difficulty);
        wb.setTimeLimit(count * 2);
        return wb;
    }

    private String createPrompt(String topic, int count, String difficulty) {
        return "You are a quiz generator. Create " + count + " questions about '" + topic + "'." +
                "Difficulty: " + difficulty + "." +
                "Output ONLY a raw JSON object (not array). Do not use markdown code blocks. " +
                "JSON structure: { \"subject\": \"Infer subject\", \"questions\": [ { \"questionText\": \"...\", \"questionType\": \"multiple/short\", \"options\": [...], \"answerText\": \"...\", \"explanation\": \"...\", \"score\": 10 } ] }";
    }

    private String callOpenAIApi(String promptText) {
        try {
            // [수정] API Key 가져오기
            String apiKey = ConfigManager.getProperty("openai.api.key");

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", promptText + " Please return JSON only.");
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
            if (responseCode != 200) return null;

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
}