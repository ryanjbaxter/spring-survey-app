package io.spring.sample.uijs;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
public class ApiProxyController {

    private final RestClient gatewayClient;

    public ApiProxyController(RestClient.Builder restClientBuilder) {
        this.gatewayClient = restClientBuilder.baseUrl("http://gateway").build();
    }

    @GetMapping("/api/polls/questions")
    public ResponseEntity<String> getQuestions() {
        String body = gatewayClient.get()
                .uri("/api/polls/questions")
                .retrieve()
                .body(String.class);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @PostMapping("/api/polls/submit")
    public ResponseEntity<String> submitPoll(@RequestBody Map<String, Object> payload) {
        String body = gatewayClient.post()
                .uri("/api/polls/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @GetMapping("/api/results/{questionId}")
    public ResponseEntity<String> getResults(@PathVariable String questionId) {
        String body = gatewayClient.get()
                .uri("/api/results/" + questionId)
                .retrieve()
                .body(String.class);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
