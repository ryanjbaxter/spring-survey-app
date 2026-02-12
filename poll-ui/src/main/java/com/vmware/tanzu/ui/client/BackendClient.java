package com.vmware.tanzu.ui.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.tanzu.ui.model.PollQuestion;
import com.vmware.tanzu.ui.model.PollResponse;
import com.vmware.tanzu.ui.model.ResultsUpdatedEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class BackendClient {

    private final RestClient pollServiceClient;
    private final RestClient resultsServiceClient;
    private final ObjectMapper objectMapper;
    private final List<Consumer<ResultsUpdatedEvent>> listeners = new CopyOnWriteArrayList<>();

    public BackendClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.pollServiceClient = restClientBuilder
            .baseUrl("http://gateway/api/polls")
            .build();

        this.resultsServiceClient = restClientBuilder
            .baseUrl("http://gateway/api/results")
            .build();

        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void startStreaming() {
        Thread sseThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    resultsServiceClient.get()
                        .uri("/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange((request, response) -> {
                            BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody()));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data:")) {
                                    String jsonData = line.substring(5).trim();
                                    if (!jsonData.isEmpty()) {
                                        ResultsUpdatedEvent event = objectMapper.readValue(
                                            jsonData, ResultsUpdatedEvent.class);
                                        for (Consumer<ResultsUpdatedEvent> listener : listeners) {
                                            listener.accept(event);
                                        }
                                    }
                                }
                            }
                            return null;
                        });
                } catch (Exception e) {
                    System.err.println("SSE stream error: " + e.getMessage() + ". Retrying in 5 seconds...");
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        sseThread.setDaemon(true);
        sseThread.start();
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

    public void addResultsListener(Consumer<ResultsUpdatedEvent> listener) {
        listeners.add(listener);
    }

    public void removeResultsListener(Consumer<ResultsUpdatedEvent> listener) {
        listeners.remove(listener);
    }
}
