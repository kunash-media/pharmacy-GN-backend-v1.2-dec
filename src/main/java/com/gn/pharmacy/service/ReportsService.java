package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.reports.*;

import java.util.List;

public interface ReportsService {

    List<CategoryDto> getAllCategories();

    PagedSalesReportDto getSalesReport(String from, String to, String category, String subcategory, int page, int limit);

    PagedInventoryReportDto getInventoryReport(String from, String to, String category, String subcategory, boolean lowStockOnly, int page, int limit);

    PagedCustomerReportDto getCustomerReport(String from, String to, String category, String subcategory, int page, int limit);

    FinancialSummaryDto getFinancialSummary(String from, String to, String groupBy);

//    ReportsOverviewDto getReportsOverview(String from, String to);
}