import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AIInterviewer {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "your_openai_api_key";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the AI Interviewer!");
        System.out.print("How many questions would you like to be asked? ");
        int numQuestions = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        for (int i = 1; i <= numQuestions; i++) {
            try {
                // Generate a question
                String question = generateQuestion();
                System.out.println("\nQuestion " + i + ": " + question);

                // Get user's answer
                System.out.print("Your answer: ");
                String answer = scanner.nextLine();

                // Evaluate the answer
                String evaluation = evaluateAnswer(question, answer);
                System.out.println("\nEvaluation: " + evaluation);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static String generateQuestion() throws IOException {
        String prompt = "Generate a professional interview question for a software engineer role.";
        return sendToOpenAI(prompt, "You are an expert interviewer.");
    }

    private static String evaluateAnswer(String question, String answer) throws IOException {
        String prompt = "Question: " + question + "\nAnswer: " + answer + 
                        "\nEvaluate the quality of this answer in terms of relevance, depth, and clarity. Provide a score out of 10 and a brief explanation.";
        return sendToOpenAI(prompt, "You are an expert evaluator.");
    }

    private static String sendToOpenAI(String prompt, String systemMessage) throws IOException {
        // Create the request payload
        String jsonPayload = String.format(
            "{ \"model\": \"gpt-4\", \"messages\": [" +
            "{ \"role\": \"system\", \"content\": \"%s\" }," +
            "{ \"role\": \"user\", \"content\": \"%s\" } ] }",
            systemMessage, prompt
        );

        // Set up the HTTP connection
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        // Extract the AI response from the JSON
        String responseText = response.toString();
        int startIndex = responseText.indexOf("\"content\":\"") + 11;
        int endIndex = responseText.indexOf("\"", startIndex);
        return responseText.substring(startIndex, endIndex).replace("\\n", "\n");
    }
}
