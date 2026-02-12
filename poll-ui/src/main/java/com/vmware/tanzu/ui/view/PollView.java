package com.vmware.tanzu.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import com.vmware.tanzu.ui.client.BackendClient;
import com.vmware.tanzu.ui.model.PollQuestion;
import com.vmware.tanzu.ui.model.PollResponse;
import com.vmware.tanzu.ui.model.ResultsUpdatedEvent;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Route("")
public class PollView extends VerticalLayout {

    private final BackendClient backendClient;
    private final String userId = UUID.randomUUID().toString();
    private final Map<String, Chart> charts = new HashMap<>();
    private final VerticalLayout pollsContainer = new VerticalLayout();
    private final VerticalLayout resultsContainer = new VerticalLayout();

    public PollView(BackendClient backendClient) {
        this.backendClient = backendClient;
        
        setSpacing(true);
        setPadding(true);
        setWidth("100%");
        setMaxWidth("1200px");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        // Header
        H1 header = new H1("Spring Cloud Poll Demo");
        header.getStyle().set("color", "#6DB33F");
        add(header);
        
        // Polls section
        H2 pollsHeader = new H2("Submit Your Answers");
        pollsContainer.setSpacing(true);
        pollsContainer.setPadding(false);
        pollsContainer.setWidth("100%");
        
        // Results section
        H2 resultsHeader = new H2("Live Results");
        resultsContainer.setSpacing(true);
        resultsContainer.setPadding(false);
        resultsContainer.setWidth("100%");
        
        add(pollsHeader, pollsContainer, resultsHeader, resultsContainer);
    }

    @PostConstruct
    public void init() {
        loadQuestions();
        subscribeToResults();
    }

    private void loadQuestions() {
        try {
            List<PollQuestion> questions = backendClient.getQuestions();
            questions.forEach(this::createPollForm);
        } catch (Exception e) {
            Notification.show("Error loading questions: " + e.getMessage(), 
                3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void createPollForm(PollQuestion question) {
        VerticalLayout questionLayout = new VerticalLayout();
        questionLayout.setSpacing(false);
        questionLayout.setPadding(true);
        questionLayout.setWidth("100%");
        questionLayout.getStyle()
            .set("border", "1px solid #e0e0e0")
            .set("border-radius", "8px")
            .set("background-color", "#f9f9f9");
        
        H2 questionText = new H2(question.text());
        questionText.getStyle().set("margin-top", "0");
        
        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setItems(question.answers());
        
        Button submitButton = new Button("Submit Answer", event -> {
            String selectedAnswer = radioGroup.getValue();
            if (selectedAnswer != null) {
                PollResponse response = new PollResponse(question.id(), selectedAnswer, userId);
                try {
                    backendClient.submitPoll(response);
                    Notification notification = Notification.show(
                        "Answer submitted!", 3000, Notification.Position.TOP_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    radioGroup.clear();
                } catch (Exception ex) {
                    Notification notification = Notification.show(
                        "Error submitting answer", 3000, Notification.Position.TOP_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        HorizontalLayout buttonLayout = new HorizontalLayout(submitButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        
        questionLayout.add(questionText, radioGroup, buttonLayout);
        pollsContainer.add(questionLayout);
        
        // Create results chart for this question
        createResultsChart(question);
    }

    private void createResultsChart(PollQuestion question) {
        VerticalLayout chartLayout = new VerticalLayout();
        chartLayout.setSpacing(false);
        chartLayout.setPadding(true);
        chartLayout.setWidth("100%");
        chartLayout.getStyle()
            .set("border", "1px solid #e0e0e0")
            .set("border-radius", "8px");
        
        H2 chartTitle = new H2(question.text());
        chartTitle.getStyle().set("margin-top", "0");
        
        Chart chart = new Chart(ChartType.BAR);
        chart.setWidth("100%");
        chart.setHeight("300px");
        
        Configuration config = chart.getConfiguration();
        config.setTitle("");
        config.getLegend().setEnabled(false);
        
        DataSeries series = new DataSeries();
        question.answers().forEach(answer -> {
            series.add(new DataSeriesItem(answer, 0));
        });
        config.setSeries(series);
        
        charts.put(question.id(), chart);
        chartLayout.add(chartTitle, chart);
        resultsContainer.add(chartLayout);
    }

    private void subscribeToResults() {
        backendClient.streamResults(this::updateChart);
    }

    private void updateChart(ResultsUpdatedEvent event) {
        Chart chart = charts.get(event.questionId());
        if (chart == null) return;
        
        UI ui = getUI().orElseThrow();
        ui.access(() -> {
            DataSeries series = new DataSeries();
            event.answerCounts().forEach((answer, count) -> {
                series.add(new DataSeriesItem(answer, count));
            });
            
            Configuration config = chart.getConfiguration();
            config.setSeries(series);
            chart.drawChart();
        });
    }
}
