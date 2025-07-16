
package com.example.truyenchu.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.truyenchu.BuildConfig;
import com.example.truyenchu.model.ChatMessage;
import com.example.truyenchu.model.Truyen;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiHelper {

    public interface GeminiHelperCallback {
        void onTextResponse(String response);
        void onStoryResponse(Truyen truyen);
        void onMultipleStoriesResponse(List<Truyen> truyenList);
        void onError(String error);
    }

    private final GeminiHelperCallback callback;
    private final DatabaseReference databaseReference;
    private final GenerativeModelFutures generativeModel;
    private final Executor mainExecutor = Executors.newSingleThreadExecutor();

    public GeminiHelper(GeminiHelperCallback callback) {
        this.callback = callback;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("truyen");

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                BuildConfig.GEMINI_API_KEY
        );
        this.generativeModel = GenerativeModelFutures.from(gm);
    }

    public void getResponse(String userQuery, List<ChatMessage> chatHistory) {
        parseIntentFromGemini(userQuery, chatHistory);
    }

    private String cleanGeminiJsonResponse(String rawText) {
        if (rawText.contains("```json")) {
            int startIndex = rawText.indexOf("{");
            int endIndex = rawText.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                return rawText.substring(startIndex, endIndex + 1);
            }
        }
        return rawText.trim();
    }

    private void parseIntentFromGemini(String userQuery, List<ChatMessage> chatHistory) {
        String systemPrompt = "You are an intelligent assistant for a story reading app. Your task is to analyze the user's latest request in the context of the conversation history and convert it into a structured JSON command. Do not answer the question directly. "
                + "The available intents are: `find_story_by_name`, `find_stories_by_author`, `find_stories_by_genre`, `recommend_top_stories`, `general_greeting`. "
                + "For `find_story_by_name`, extract the `story_name`. "
                + "For `find_stories_by_author`, extract the `author_name`. "
                + "For `find_stories_by_genre`, extract the `genre_name`. "
                + "If you cannot determine a specific intent from the latest message, use `general_greeting`. "
                + "Your output must be only the JSON command.";

        List<Content> historyContent = new ArrayList<>();
        // Xây dựng Content một cách tường minh để đảm bảo tương thích
        historyContent.add(new Content("user", Collections.singletonList(new TextPart(systemPrompt))));
        historyContent.add(new Content("model", Collections.singletonList(new TextPart("OK, I am ready."))));

        for (ChatMessage message : chatHistory) {
            if (message.getViewType() == ChatMessage.TYPE_SENT_TEXT && message.getTextMessage() != null) {
                historyContent.add(new Content("user", Collections.singletonList(new TextPart(message.getTextMessage()))));
            } else if (message.getViewType() == ChatMessage.TYPE_RECEIVED_TEXT && message.getTextMessage() != null) {
                if (!message.getTextMessage().startsWith("Đã có lỗi xảy ra:")) {
                    historyContent.add(new Content("model", Collections.singletonList(new TextPart(message.getTextMessage()))));
                }
            }
        }
        historyContent.add(new Content("user", Collections.singletonList(new TextPart(userQuery))));

        // Chuyển List thành Array khi gọi generateContent
        ListenableFuture<GenerateContentResponse> future = generativeModel.generateContent(historyContent.toArray(new Content[0]));

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String rawResponse = result.getText();
                String jsonResponse = cleanGeminiJsonResponse(rawResponse);
                handleCommand(jsonResponse, userQuery);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Không thể kết nối tới AI: " + t.getMessage());
            }
        }, mainExecutor);
    }

    private void handleCommand(String jsonCommand, String originalQuery) {
        try {
            JsonObject command = JsonParser.parseString(jsonCommand).getAsJsonObject();
            String intent = command.get("intent").getAsString();

            switch (intent) {
                case "find_story_by_name":
                    if (command.has("story_name")) {
                        String storyName = command.get("story_name").getAsString();
                        filterStoriesAndRespond("name", storyName, originalQuery);
                    } else {
                        generateFinalResponse(null, originalQuery, null);
                    }
                    break;
                case "find_stories_by_author":
                    if (command.has("author_name")) {
                        String authorName = command.get("author_name").getAsString();
                        filterStoriesAndRespond("author", authorName, originalQuery);
                    } else {
                        generateFinalResponse(null, originalQuery, null);
                    }
                    break;
                case "find_stories_by_genre":
                    if (command.has("genre_name")) {
                        String genreName = command.get("genre_name").getAsString();
                        filterStoriesAndRespond("genre", genreName, originalQuery);
                    } else {
                        generateFinalResponse(null, originalQuery, null);
                    }
                    break;
                case "recommend_top_stories":
                    executeQueryForTopStories(originalQuery);
                    break;
                default:
                    generateFinalResponse(null, originalQuery, null);
                    break;
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            generateFinalResponse(null, originalQuery, null);
        }
    }

    private void filterStoriesAndRespond(String filterType, String filterValue, String originalQuery) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Truyen> filteredList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Truyen truyen = dataSnapshot.getValue(Truyen.class);
                        if (truyen == null) continue;

                        truyen.setId(dataSnapshot.getKey());
                        boolean matches = false;
                        String lowerCaseFilterValue = filterValue.toLowerCase(Locale.ROOT);

                        if ("name".equals(filterType) && truyen.getTen() != null) {
                            if (truyen.getTen().toLowerCase(Locale.ROOT).contains(lowerCaseFilterValue)) {
                                matches = true;
                            }
                        } else if ("author".equals(filterType) && truyen.getTacGia() != null) {
                            if (truyen.getTacGia().toLowerCase(Locale.ROOT).contains(lowerCaseFilterValue)) {
                                matches = true;
                            }
                        } else if ("genre".equals(filterType) && truyen.getTheLoaiTags() != null) {
                            if (truyen.getTheLoaiTags().toLowerCase(Locale.ROOT).contains(lowerCaseFilterValue)) {
                                matches = true;
                            }
                        }

                        if (matches) {
                            filteredList.add(truyen);
                        }

                        if (filteredList.size() >= 5) {
                            break;
                        }
                    }
                }
                List<Map<String, Object>> simplifiedList = new ArrayList<>();
                for (Truyen truyen : filteredList) {
                    Map<String, Object> simplifiedTruyen = new HashMap<>();
                    simplifiedTruyen.put("ten", truyen.getTen());
                    simplifiedTruyen.put("tacGia", truyen.getTacGia());
                    simplifiedTruyen.put("theLoai", truyen.getTheLoaiTags());
                    simplifiedTruyen.put("danhGia", truyen.getDanhGia());
                    simplifiedList.add(simplifiedTruyen);
                }
                generateFinalResponse(new Gson().toJson(simplifiedList), originalQuery, filteredList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Lỗi database: " + error.getMessage());
            }
        });
    }

    private void executeQueryForTopStories(String originalQuery) {
        Query query = databaseReference.orderByChild("danhGia").limitToLast(5);
        fetchDataFromFirebase(query, originalQuery, true);
    }

    private void fetchDataFromFirebase(Query query, String originalQuery, boolean shouldReverse) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Truyen> truyenList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Truyen truyen = dataSnapshot.getValue(Truyen.class);
                        if (truyen != null) {
                            truyen.setId(dataSnapshot.getKey());
                            truyenList.add(truyen);
                        }
                    }
                    if (shouldReverse) {
                        Collections.reverse(truyenList);
                    }
                }
                List<Map<String, Object>> simplifiedList = new ArrayList<>();
                for (Truyen truyen : truyenList) {
                    Map<String, Object> simplifiedTruyen = new HashMap<>();
                    simplifiedTruyen.put("ten", truyen.getTen());
                    simplifiedTruyen.put("tacGia", truyen.getTacGia());
                    simplifiedTruyen.put("theLoai", truyen.getTheLoaiTags());
                    simplifiedTruyen.put("danhGia", truyen.getDanhGia());
                    simplifiedList.add(simplifiedTruyen);
                }

                generateFinalResponse(new Gson().toJson(simplifiedList), originalQuery, truyenList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Lỗi database: " + error.getMessage());
            }
        });
    }

    private void generateFinalResponse(String databaseData, String originalQuery, @Nullable List<Truyen> truyenList) {
        String dataForPrompt = (databaseData == null) ? "[]" : databaseData;
        String prompt = "You are a friendly and helpful assistant in a story reading app. "
                + "A user asked the following question: \"" + originalQuery + "\". "
                + "Based on the data I found from my database, please formulate a helpful and engaging response in Vietnamese. The data is provided below in JSON format. "
                + "If the data is an empty list `[]`, politely inform the user that you couldn't find what they were looking for. "
                + "If the user's query is just a greeting, respond with a friendly greeting. "
                + "Do not mention the database or JSON. Just present the information naturally. "
                + "Database data: " + dataForPrompt
                + "\nYour response in Vietnamese:";

        // Xây dựng Content một cách tường minh
        Content content = new Content("user", Collections.singletonList(new TextPart(prompt)));
        ListenableFuture<GenerateContentResponse> future = generativeModel.generateContent(content);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.onTextResponse(result.getText());
                if (truyenList != null && !truyenList.isEmpty()) {
                    callback.onMultipleStoriesResponse(truyenList);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Không thể tạo câu trả lời: " + t.getMessage());
            }
        }, mainExecutor);
    }
}
