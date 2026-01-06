package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.dashboard.*;
import com.gn.pharmacy.entity.*;
import com.gn.pharmacy.repository.*;
import com.gn.pharmacy.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final MbPRepository mbpRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public DashboardSummaryDto getDashboardSummary() {
        BigDecimal totalProfit = orderRepository.sumTotalAmountByStatus("COMPLETED").orElse(BigDecimal.ZERO);
        long totalPrescriptions = prescriptionRepository.count();

        long totalInventoryItems = productRepository.findAll().stream()
                .mapToLong(p -> Optional.ofNullable(p.getTotalCalculatedStock()).orElse(0))
                .sum() +
                mbpRepository.findAll().stream()
                        .mapToLong(m -> Optional.ofNullable(m.getTotalCalculatedStock()).orElse(0))
                        .sum();

        long lowStockItems = inventoryRepository.findAll().stream()
                .filter(batch -> batch.getQuantity() < 20)
                .count();

        YearMonth current = YearMonth.now();
        YearMonth last = current.minusMonths(1);

        BigDecimal currentMonthProfit = orderRepository.sumTotalAmountByMonth(
                String.valueOf(current.getYear()),
                String.format("%02d", current.getMonthValue())
        ).orElse(BigDecimal.ZERO);

        BigDecimal lastMonthProfit = orderRepository.sumTotalAmountByMonth(
                String.valueOf(last.getYear()),
                String.format("%02d", last.getMonthValue())
        ).orElse(BigDecimal.ZERO);

        String trend = currentMonthProfit.compareTo(lastMonthProfit) >= 0 ? "up" : "down";
        double change = lastMonthProfit.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                currentMonthProfit.subtract(lastMonthProfit)
                        .divide(lastMonthProfit, 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

        return new DashboardSummaryDto(totalProfit, totalPrescriptions, totalInventoryItems, lowStockItems, trend, change);
    }

    @Override
    public MonthlyRevenueDto getMonthlyRevenue(int year) {
        List<String> months = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

        List<BigDecimal> revenues = new ArrayList<>();
        String yearStr = String.valueOf(year);
        for (int i = 1; i <= 12; i++) {
            String monthStr = String.format("%02d", i);
            BigDecimal amount = orderRepository.sumTotalAmountByMonth(yearStr, monthStr).orElse(BigDecimal.ZERO);
            revenues.add(amount);
        }
        return new MonthlyRevenueDto(months, revenues);
    }

    @Override
    public CategoryDistributionDto getCategoryDistribution() {
        Map<String, Long> map = new HashMap<>();
        productRepository.findAll().forEach(p -> map.merge(p.getProductCategory() != null ? p.getProductCategory() : "Uncategorized", 1L, Long::sum));
        mbpRepository.findAll().forEach(m -> map.merge(m.getCategory() != null ? m.getCategory() : "Mother & Baby", 1L, Long::sum));
        return new CategoryDistributionDto(new ArrayList<>(map.keySet()), new ArrayList<>(map.values()));
    }

    @Override
    public List<RecentPrescriptionDto> getRecentPrescriptions(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return prescriptionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .getContent()
                .stream()
                .map(p -> new RecentPrescriptionDto(
                        p.getFirstName() + " " + p.getLastName(),
                        p.getPrescriptionId(),
                        p.getCreatedAt(),
                        p.getOrderStatus() != null ? p.getOrderStatus().toUpperCase() : "PENDING"
                ))
                .toList();
    }

    @Override
    public List<LowStockDto> getLowStockItems(int limit) {
        return inventoryRepository.findAll().stream()
                .filter(b -> b.getQuantity() < 30)
                .sorted(Comparator.comparingInt(InventoryEntity::getQuantity))
                .limit(limit)
                .map(b -> {
                    String name = b.getProduct() != null ? b.getProduct().getProductName() : b.getMbp() != null ? b.getMbp().getTitle() : "Unknown";
                    String sku = b.getProduct() != null ? b.getProduct().getSku() : b.getMbp() != null ? b.getMbp().getSku() : "";
                    String level = b.getQuantity() == 0 ? "Out" : b.getQuantity() < 10 ? "Critical" : "Low";
                    return new LowStockDto(name, sku, b.getQuantity(), level);
                })
                .toList();
    }

    @Override
    public List<TopSellingDto> getTopSellingProducts(int limit, int months) {
        LocalDateTime from = LocalDateTime.now().minusMonths(months);
        return orderItemRepository.findTopSelling(from, PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Object[] arr = (Object[]) row;
                    return new TopSellingDto((String) arr[0], (Long) arr[1], (BigDecimal) arr[2]);
                })
                .toList();
    }

    @Override
    public ExpirySummaryDto getExpirySummary() {
        LocalDate today = LocalDate.now();
        LocalDate d30 = today.plusDays(30);
        LocalDate d60 = today.plusDays(60);
        LocalDate d90 = today.plusDays(90);

        List<InventoryEntity> batches = inventoryRepository.findAll();

        List<ExpiryItemDto> items = new ArrayList<>();
        long w30 = 0, w60 = 0, w90 = 0;

        for (InventoryEntity b : batches) {
            String expStr = b.getExpDate();
            if (expStr == null || expStr.trim().isEmpty()) continue;

            LocalDate expDate = parseDate(expStr.trim());
            if (expDate == null) continue;

            String name = b.getProduct() != null ? b.getProduct().getProductName() :
                    b.getMbp() != null ? b.getMbp().getTitle() : "Unknown";

            String period;
            if (!expDate.isAfter(d30)) {
                period = "Within 30 Days";
                w30++;
            } else if (!expDate.isAfter(d60)) {
                period = "Within 60 Days";
                w60++;
            } else if (!expDate.isAfter(d90)) {
                period = "Within 90 Days";
                w90++;
            } else {
                continue; // Skip if beyond 90 days
            }

            items.add(new ExpiryItemDto(name, expDate, period));
        }

        // Sort items by expiry date ascending
        items.sort(Comparator.comparing(ExpiryItemDto::expiryDate));

        return new ExpirySummaryDto(w30, w60, w90, items);
    }

    // Helper method inside the class
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;

        // Try common formats
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy/MM/dd"};
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        return null; // Invalid format
    }
}