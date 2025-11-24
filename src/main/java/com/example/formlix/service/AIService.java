package com.example.formlix.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AIService {

    // ✅ Using Hugging Face's OpenAI-compatible endpoint
    private static final String HF_API_URL = "https://router.huggingface.co/v1/chat/completions";

    // ✅ Popular, well-supported models (choose one):
    private static final String MODEL_ID = "meta-llama/Llama-3.2-3B-Instruct"; // Fast and reliable
    // Other alternatives:
    // "Qwen/Qwen2.5-7B-Instruct" - Very good quality
    // "meta-llama/Llama-3.3-70B-Instruct" - Best quality but slower
    // "mistralai/Mistral-7B-Instruct-v0.3" - Good balance
    // "microsoft/Phi-3.5-mini-instruct" - Fast and efficient

    @Value("${huggingface.api.key}")
    private String HF_API_KEY;

    // ✅ Updated: Now accepts a prompt string directly (with page info already included)
    public String generateContent(String prompt) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("🤖 AI SERVICE CALLED");
        System.out.println("═══════════════════════════════════════");

        try {
            // ✅ Check API key first
            if (HF_API_KEY == null || HF_API_KEY.trim().isEmpty() || HF_API_KEY.equals("your_api_key_here")) {
                String error = "❌ CRITICAL: Hugging Face API key is not configured! Please set huggingface.api.key in application.properties";
                System.err.println(error);
                return "Error: API key not configured. Please check application.properties";
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build();

            System.out.println("🚀 Sending request to Hugging Face...");
            System.out.println("📝 Using model: " + MODEL_ID);
            System.out.println("🔑 API Key: " + (HF_API_KEY != null ? HF_API_KEY.substring(0, Math.min(10, HF_API_KEY.length())) + "..." : "NOT SET"));
            System.out.println("📄 Prompt length: " + prompt.length() + " characters");
            System.out.println("📄 Prompt preview: " + prompt.substring(0, Math.min(150, prompt.length())) + "...");

            // ✅ OpenAI-compatible request format
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL_ID);

            // Messages array format
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            requestBody.put("messages", messages);

            // ✅ Adjusted parameters for longer, more detailed content
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4000); // ✅ Increased for longer reports (12+ pages)
            requestBody.put("stream", false);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(HF_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + HF_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    System.err.println("❌ Hugging Face API error code: " + response.code());
                    System.err.println("❌ Error details: " + errorBody);

                    // More helpful error message
                    if (response.code() == 400) {
                        return "Error: The selected model may not be available. " +
                                "Please check your Hugging Face account has access to this model. " +
                                "Error details: " + errorBody;
                    }

                    return "Error: Unable to generate content (" + response.code() + "). Details: " + errorBody;
                }

                String responseBody = response.body().string();
                System.out.println("✅ Received response from API");

                // ✅ Parse OpenAI-compatible response format
                JSONObject jsonResponse = new JSONObject(responseBody);

                if (jsonResponse.has("choices")) {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject messageObj = firstChoice.getJSONObject("message");
                        String content = messageObj.getString("content");
                        System.out.println("✅ Content generated successfully! Length: " + content.length() + " characters");
                        return content.trim();
                    }
                }

                return "Error: Unexpected response format.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error while generating AI content: " + e.getMessage();
        }
    }
}