package com.ecommerce.util;

import com.ecommerce.service.PerformanceReportService;

/**
 * Standalone Performance Benchmark Runner
 * Run this to generate performance reports without launching the full UI
 * 
 * Usage: java com.ecommerce.util.PerformanceBenchmarkRunner [output-file.md]
 */
public class PerformanceBenchmarkRunner {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║   E-Commerce Performance Benchmark Suite          ║");
        System.out.println("║   User Story 4.1: Performance Report Generation   ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            // Initialize service
            PerformanceReportService service = new PerformanceReportService();
            
            // Run benchmarks
            PerformanceReportService.PerformanceReport report = service.runBenchmarks();
            
            // Print summary to console
            service.printSummary(report);
            
            // Save to file if path provided
            String outputPath = args.length > 0 ? args[0] : "performance_report.md";
            service.generateReportFile(report, outputPath);
            
            System.out.println("\n✓ Benchmark completed successfully!");
            System.out.println("✓ Report saved to: " + outputPath);
            
        } catch (Exception e) {
            System.err.println("\n✗ Error running benchmarks: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
