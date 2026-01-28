package com.ecommerce.controllers.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

public class DashboardViewFactory {
    
    public static VBox createDashboardView(int totalProducts, int totalOrders, int totalUsers, double totalRevenue) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f6fa;");
        
        Label titleLabel = new Label("Dashboard Overview");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(statsBox, Priority.ALWAYS);
        
        statsBox.getChildren().addAll(
            createStatCard("Total Products", String.valueOf(totalProducts), "#3498db"),
            createStatCard("Total Orders", String.valueOf(totalOrders), "#2ecc71"),
            createStatCard("Total Users", String.valueOf(totalUsers), "#9b59b6"),
            createStatCard("Total Revenue", String.format("$%.2f", totalRevenue), "#e74c3c")
        );
        
        container.getChildren().addAll(titleLabel, statsBox);
        return container;
    }
    
    private static VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(250);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        HBox.setHgrow(card, Priority.ALWAYS);
        
        return card;
    }
}
