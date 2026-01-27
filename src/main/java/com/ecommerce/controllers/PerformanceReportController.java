package com.ecommerce.controllers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ecommerce.service.PerformanceReportService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

/**
 * Controller for Performance Report Generation
 * Allows analysts to generate and view performance benchmarks
 * 
 * User Story 4.1: Generate performance reports
 */
public class PerformanceReportController {
    
    @FXML private Button runBenchmarkButton;
    @FXML private Button saveReportButton;
    @FXML private TextArea reportOutput;
    
    private PerformanceReportService reportService;
    private PerformanceReportService.PerformanceReport currentReport;
    
    @FXML
    public void initialize() {
        reportService = new PerformanceReportService();
        saveReportButton.setDisable(true);
    }
    
    /**
     * Run performance benchmarks and display results
     */
    @FXML
    private void handleRunBenchmark() {
        runBenchmarkButton.setDisable(true);
        reportOutput.setText("Running performance benchmarks...\n\n");
        
        // Run in background to keep UI responsive
        new Thread(() -> {
            try {
                currentReport = reportService.runBenchmarks();
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    displayReport();
                    saveReportButton.setDisable(false);
                    runBenchmarkButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    reportOutput.setText("Error running benchmarks: " + e.getMessage());
                    runBenchmarkButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * Display report in text area
     */
    private void displayReport() {
        StringBuilder output = new StringBuilder();
        
        output.append("========================================\n");
        output.append("PERFORMANCE BENCHMARK REPORT\n");
        output.append("========================================\n\n");
        
        output.append("Generated: ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("\n\n");
        
        output.append(String.format("Total Tests: %d\n", currentReport.getMetrics().size()));
        output.append(String.format("Average Improvement: %.1f%%\n\n", 
            currentReport.getAverageImprovement()));
        
        output.append(currentReport.toTable());
        
        output.append("\n\nMETHODOLOGY:\n");
        output.append("- Database Indexing: B-tree indexes on frequently queried columns\n");
        output.append("- Caching: In-memory caching with 5-minute TTL\n");
        output.append("- Connection Pooling: Singleton pattern for connection reuse\n");
        output.append("- Query Optimization: Reduced N+1 queries using JOINs\n");
        output.append("- In-Memory Operations: Cart without database I/O\n\n");
        
        output.append("✓ Report generated successfully. Click 'Save Report' to export.\n");
        
        reportOutput.setText(output.toString());
    }
    
    /**
     * Save report to markdown file
     */
    @FXML
    private void handleSaveReport() {
        if (currentReport == null) {
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Performance Report");
        fileChooser.setInitialFileName("performance_report_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".md");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Markdown Files", "*.md")
        );
        
        File file = fileChooser.showSaveDialog(saveReportButton.getScene().getWindow());
        
        if (file != null) {
            reportService.generateReportFile(currentReport, file.getAbsolutePath());
            reportOutput.appendText("\n✓ Report saved to: " + file.getAbsolutePath());
        }
    }
}
