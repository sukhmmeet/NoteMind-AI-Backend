package com.dhaliwal.notemind.util;

import com.dhaliwal.notemind.entity.type.SummaryType;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class Util {
    public String getPromptForNoteSummary(String title, String content) {
        return """
            You are an assistant that summarizes notes.

            Given the following note:

            Title: %s
            Content: %s
    
            Instructions:
            - Generate a concise summary in 2-3 sentences.
            - Capture the key idea and important details.
            - Generate 3-5 relevant tags.
            - Tags should be short keywords.
            - Do not repeat the title.
            - Keep everything clear and simple.

            Return ONLY valid JSON in this format:
            {
              "summary": "your summary here",
              "tags": ["tag1", "tag2", "tag3"]
            }
            """.formatted(title, content);
    }
    public String getPromptForNoteSummary(String title, String content, SummaryType summaryType) {

        String summaryInstruction = switch (summaryType) {
            case SHORT -> """
                - Generate a concise summary in 2-3 sentences.
                - Keep it under 80 words.
                - Focus only on the main idea.
                """;

            case DETAILED -> """
                - Generate a detailed summary in 6-10 sentences.
                - Include all important concepts.
                - Preserve technical details where relevant.
                - Explain the content clearly.
                """;

            case BULLET_POINTS -> """
                - Summarize the note using bullet points.
                - Use 5-10 bullet points.
                - Each bullet should contain one important idea.
                - Keep bullets short and easy to read.
                - Each bullet should start with '-'.
                - Each bullet should contain one key idea.
                - Do not write paragraphs.
                """;
            };

            return """
            You are an assistant that summarizes notes.

            Given the following note:

            Title: %s
            Content: %s

            Instructions:
            %s

            - Generate 3-5 relevant tags.
            - Tags should be short keywords.
            - Do not repeat the title.
            - Return ONLY valid JSON.

            {
              "summary": "your summary here",
              "tags": ["tag1", "tag2", "tag3"]
            }
            """.formatted(title, content, summaryInstruction);
    }
    public String getPromptForFlashCard(String title, String content, int count) {
        String countString = String.valueOf(count);
        return String.format("""
                You are an expert teacher.
                
                Your task is to generate high-quality study flashcards using the provided note title, note content, and optional image.
                
                Instructions:
                - Carefully analyze the note title.
                - Carefully read the note content.
                - If an image is provided, extract all useful educational information from it and combine it with the text.
                - Do not ignore either the image or the text.
                - Create flashcards that help a student memorize and understand the topic.
                - Questions should be clear, concise, and focused on one concept.
                - Answers should be accurate, brief, and complete.
                - Avoid duplicate flashcards.
                - Include important definitions, concepts, formulas, facts, comparisons, and processes whenever applicable.
                - If the image contains diagrams, tables, handwritten notes, equations, or labels, incorporate that information into the flashcards.
                - If the image contains no useful educational information, generate flashcards only from the text.
                
                Title:
                %s
                
                Content:
                %s
                
                Return ONLY valid JSON in the following format.
                
                {
                  "flashcards": [
                    {
                      "question": "Question 1",
                      "answer": "Answer 1"
                    },
                    {
                      "question": "Question 2",
                      "answer": "Answer 2"
                    }
                  ]
                }
                
                Rules:
                - Return only JSON.
                - Do not use Markdown.
                - Do not wrap the JSON in ``` blocks.
                - Do not include explanations or extra text.
                - Generate between %s flashcards depending on the amount of information available.
                """, title, content, countString);
    }

    public String getPromptForQuiz(String title, String content, int count) {
        return String.format("""
                You are an expert teacher.

                Create a multiple-choice quiz from the note below.
                Generate exactly %s questions when the content supports it.
                Each question must have exactly four distinct answer options.
                The correctAnswer must exactly match one option.
                Include a short explanation for the correct answer.
                Use the image as an additional source of information when one is provided.

                Title:
                %s

                Content:
                %s

                Return ONLY valid JSON in this format:
                {
                  "questions": [
                    {
                      "question": "Question text",
                      "options": ["Option A", "Option B", "Option C", "Option D"],
                      "correctAnswer": "Option B",
                      "explanation": "Why this answer is correct"
                    }
                  ]
                }

                Do not return Markdown, code fences, or extra text.
                """, count, title, content);
    }

    public String chooseModel(String imageUrl) {
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            return "stealth/crouton-alpha";
//        } else {
//            return "stealth/crouton-alpha";
//        }
        return "stealth/crouton-alpha";
    }
    public Map<String, Object> buildRequest(String prompt, String imageUrl) {

        Map<String, Object> request = new HashMap<>();
        request.put("model", chooseModel(imageUrl));

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");

        List<Object> content = new ArrayList<>();

        // text part (always present)
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);
        content.add(textPart);

        // image part (only if exists)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Map<String, Object> image = new HashMap<>();
            image.put("type", "image_url");

            Map<String, Object> imageData = new HashMap<>();
            imageData.put("url", imageUrl);

            image.put("image_url", imageData);
            content.add(image);
        }

        message.put("content", content);
        request.put("messages", List.of(message));

        return request;
    }
}
