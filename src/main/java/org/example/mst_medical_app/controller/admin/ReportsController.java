package org.example.mst_medical_app.controller.admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.example.mst_medical_app.service.ReportService;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controller cho trang Báo cáo (Reports) của Admin.
 * Hiển thị biểu đồ trạng thái, giới tính, thống kê tháng và tổng KPI.
 */
public class ReportsController {

    @FXML
    private PieChart pieChartStatus;   // Biểu đồ trạng thái cuộc hẹn
    @FXML
    private PieChart pieChartGender;   // Biểu đồ giới tính bệnh nhân
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private Label totalPatientsLabel, totalAppointmentsLabel, totalRevenueLabel;

    private ReportService reportService;

    @FXML
    public void initialize() {
        reportService = new ReportService();

        loadAppointmentStatusChart();
        loadGenderChart();
        loadMonthlyBarChart();
        updateSummary();
    }

    // Biểu đồ trạng thái cuộc họp
    private void loadAppointmentStatusChart() {
        pieChartStatus.getData().clear();
        pieChartStatus.setTitle("Trạng thái cuộc hẹn");

        pieChartStatus.getData().addAll(reportService.getAppointmentStatusReport());

        Platform.runLater(() -> {
            double total = pieChartStatus.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

            for (PieChart.Data d : pieChartStatus.getData()) {
                String color;
                switch (d.getName().toUpperCase()) {
                    case "CONFIRMED" -> color = "#3B82F6"; // xanh dương
                    case "PENDING" -> color = "#FACC15";   // vàng
                    case "COMPLETED" -> color = "#10B981"; // xanh lá
                    case "CANCELED" -> color = "#EF4444";  // đỏ
                    default -> color = "#9CA3AF";          // xám
                }

                // Gán màu lát bánh
                d.getNode().setStyle("-fx-pie-color: " + color + ";");

                // Hiển thị phần trăm
                double percent = (d.getPieValue() / total) * 100;
                Tooltip tooltip = new Tooltip(d.getName() + ": " + String.format("%.1f", percent) + "%");
                Tooltip.install(d.getNode(), tooltip);

                // Đồng bộ màu legend
                pieChartStatus.lookupAll(".chart-legend-item-symbol").forEach(node -> {
                    if (node.getParent().toString().contains(d.getName())) {
                        node.setStyle("-fx-background-color: " + color + ";");
                    }
                });
            }
        });
    }

    // Biểu đồ giới tính bệnh nhân
    private void loadGenderChart() {
        if (pieChartGender == null) return;

        pieChartGender.getData().clear();
        pieChartGender.setTitle("Tỷ lệ giới tính bệnh nhân");

        pieChartGender.getData().addAll(reportService.getPatientGenderDashboard());

        Platform.runLater(() -> {
            double total = pieChartGender.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

            for (PieChart.Data d : pieChartGender.getData()) {
                String color;
                switch (d.getName().toUpperCase()) {
                    case "NAM" -> color = "#3B82F6";   // xanh dương
                    case "NỮ" -> color = "#EC4899";   // hồng
                    case "KHÁC" -> color = "#F59E0B"; // vàng
                    default -> color = "#9CA3AF";     // xám
                }

                d.getNode().setStyle("-fx-pie-color: " + color + ";");

                // Tooltip phần trăm
                double percent = (d.getPieValue() / total) * 100;
                Tooltip tooltip = new Tooltip(d.getName() + ": " + String.format("%.1f", percent) + "%");
                Tooltip.install(d.getNode(), tooltip);

                // Đồng bộ màu legend
                pieChartGender.lookupAll(".chart-legend-item-symbol").forEach(node -> {
                    if (node.getParent().toString().contains(d.getName())) {
                        node.setStyle("-fx-background-color: " + color + ";");
                    }
                });
            }
        });
    }

    // Biểu đồ số lượng cuộc họp theo tháng
    private void loadMonthlyBarChart() {
        barChart.getData().clear();
        barChart.setTitle("Số lượng lịch hẹn theo tháng");

        XYChart.Series<String, Number> series = reportService.getMonthlyAppointmentReport();
        series.setName("Số cuộc hẹn");

        barChart.getData().add(series);
    }

    // KPI tổng
    private void updateSummary() {
        int[] kpis = reportService.getDashboardKpiCounts();

        totalPatientsLabel.setText("👥 Tổng bệnh nhân: " + kpis[0]);
        totalAppointmentsLabel.setText("📅 Tổng cuộc hẹn: " + kpis[2]);

        int revenue = kpis[2] * 180000;
        String formatted = NumberFormat.getNumberInstance(Locale.US).format(revenue);
        totalRevenueLabel.setText("💰 Doanh thu: " + formatted + " đ");
    }
}
