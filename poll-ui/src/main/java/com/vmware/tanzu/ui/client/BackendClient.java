package com.vmware.tanzu.ui.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.tanzu.ui.model.PollQuestion;
import com.vmware.tanzu.ui.model.PollResponse;
import com.vmware.tanzu.ui.model.ResultsUpdatedEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

@Service
public class BackendClient {

    private final RestClient pollServiceClient;
    private final RestClient resultsServiceClient;
    private final ObjectMapper objectMapper;
    private final String resultsStreamUrl = "http://gateway/api/results/stream";

    public BackendClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.pollServiceClient = restClientBuilder
            .baseUrl("http://gateway/api/polls")
            .build();
        
        this.resultsServiceClient = restClientBuilder
            .baseUrl("http://gateway/api/results")
            .build();
            
        this.objectMapper = objectMapper;
    }

    public List<PollQuestion> getQuestions() {
        return pollServiceClient.get()
            .uri("/questions")
            .retrieve()
            .body(new ParameterizedTypeReference<List<PollQuestion>>() {});
    }

    public void submitPoll(PollResponse response) {
        pollServiceClient.post()
            .uri("/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .body(response)
            .retrieve()
            .toBodilessEntity();
    }

    public void streamResults(Consumer<ResultsUpdatedEvent> eventConsumer) {
        // Use a separate thread to handle SSE streaming
        Thread sseThread = new Thread(() -> {
            try {
                URL url = new URL(resultsStreamUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "text/event-stream");
                connection.setDoInput(true);
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                
                String line;
                StringBuilder eventData = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (!jsonData.isEmpty()) {
                            ResultsUpdatedEvent event = objectMapper.readValue(
                                jsonData, ResultsUpdatedEvent.class);
                            eventConsumer.accept(event);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("SSE stream error: " + e.getMessage());
            }
        });
        sseThread.setDaemon(true);
        sseThread.start();
    }
}
