package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.dashboard.*;

import java.util.List;

public interface DashboardService {
    DashboardSummaryDto getDashboardSummary();
    MonthlyRevenueDto getMonthlyRevenue(int year);
    CategoryDistributionDto getCategoryDistribution();
    List<RecentPrescriptionDto> getRecentPrescriptions(int limit);
    List<LowStockDto> getLowStockItems(int limit);
    List<TopSellingDto> getTopSellingProducts(int limit, int months);
    ExpirySummaryDto getExpirySummary();
}