
package com.tap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/chatbot")
public class ChatbotServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String userMessage = req.getParameter("message");

        String systemPrompt =
                "You are an AI assistant for Iron Lady. " +
                "Help users understand programs and application process.";

        String aiReply = callChatGPT(systemPrompt, userMessage);

        resp.setContentType("text/plain");
        resp.getWriter().write(aiReply);
    }

    private String callChatGPT(String systemPrompt, String userMessage)
            throws IOException {

        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty(
                "Authorization",
                "Bearer YOUR_NEW_API_KEY"
        );
        conn.setDoOutput(true);

        String requestBody =
                "{"
              + "\"model\":\"gpt-4o-mini\","
              + "\"messages\":["
              + "{\"role\":\"system\",\"content\":\"" + systemPrompt + "\"},"
              + "{\"role\":\"user\",\"content\":\"" + userMessage + "\"}"
              + "]"
              + "}";

        OutputStream os = conn.getOutputStream();
        os.write(requestBody.getBytes());
        os.close();

        int status = conn.getResponseCode();
        if (status == 429) {
            return "AI service is busy right now. Please try again later.";
        }
        if (status != 200) {
            return "Our AI assistant is currently busy. Please try again shortly..";
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        // Simple extraction (no Gson)
        String fullResponse = response.toString();
        String marker = "\"content\":\"";
        int start = fullResponse.indexOf(marker) + marker.length();
        int end = fullResponse.indexOf("\"", start);

        return fullResponse.substring(start, end);
    }
}
