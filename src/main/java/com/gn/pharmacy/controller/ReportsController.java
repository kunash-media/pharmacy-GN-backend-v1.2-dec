package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.dashboard.ApiResponse;
import com.gn.pharmacy.dto.reports.*;
import com.gn.pharmacy.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")

public class ReportsController {

    @Autowired
    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService){
        this.reportsService = reportsService;
    }

    /**
     * Get all available categories and their subcategories dynamically
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(reportsService.getAllCategories()));
    }

    /**
     * Sales Report - Paginated with filters
     */
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<PagedSalesReportDto>> getSalesReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getSalesReport(from, to, category, subcategory, page, limit)
        ));
    }

    /**
     * Inventory Report - Paginated with filters + low stock option
     */
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<PagedInventoryReportDto>> getInventoryReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(defaultValue = "false") boolean lowStockOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getInventoryReport(from, to, category, subcategory, lowStockOnly, page, limit)
        ));
    }

    /**
     * Customers Report - Paginated with filters
     */
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<PagedCustomerReportDto>> getCustomerReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getCustomerReport(from, to, category, subcategory, page, limit)
        ));
    }

    /**
     * Financial Summary Report
     */
    @GetMapping("/financial")
    public ResponseEntity<ApiResponse<FinancialSummaryDto>> getFinancialSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "month") String groupBy) {

        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getFinancialSummary(from, to, groupBy)
        ));
    }

    /**
     * Quick overview summary for all report types (optional - useful for cards)
     */
//    @GetMapping("/summary")
//    public ResponseEntity<ApiResponse<ReportsOverviewDto>> getReportsOverview(
//            @RequestParam(required = false) String from,
//            @RequestParam(required = false) String to) {
//
//        return ResponseEntity.ok(ApiResponse.success(
//                reportsService.getReportsOverview(from, to)
//        ));
//    }
}