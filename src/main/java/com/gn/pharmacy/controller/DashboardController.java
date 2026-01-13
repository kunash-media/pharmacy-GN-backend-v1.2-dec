package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.dashboard.*;
import com.gn.pharmacy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")

public class DashboardController {

    @Autowired
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboardSummary()));
    }

    @GetMapping("/revenue-monthly")
    public ResponseEntity<ApiResponse<MonthlyRevenueDto>> getMonthlyRevenue(@RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthlyRevenue(year)));
    }

    @GetMapping("/category-distribution")
    public ResponseEntity<ApiResponse<CategoryDistributionDto>> getCategoryDistribution() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCategoryDistribution()));
    }

    @GetMapping("/prescriptions/recent")
    public ResponseEntity<ApiResponse<List<RecentPrescriptionDto>>> getRecentPrescriptions(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRecentPrescriptions(limit)));
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockDto>>> getLowStock(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getLowStockItems(limit)));
    }

    @GetMapping("/sales/top-selling")
    public ResponseEntity<ApiResponse<List<TopSellingDto>>> getTopSelling(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTopSellingProducts(limit, months)));
    }

    @GetMapping("/inventory/expiry-summary")
    public ResponseEntity<ApiResponse<ExpirySummaryDto>> getExpirySummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getExpirySummary()));
    }
}