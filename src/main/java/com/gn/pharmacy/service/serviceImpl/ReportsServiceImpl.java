package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.reports.*;
import com.gn.pharmacy.entity.*;
import com.gn.pharmacy.repository.*;
import com.gn.pharmacy.service.ReportsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsServiceImpl implements ReportsService {

    private final ProductRepository productRepository;
    private final MbPRepository mbPRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ReportsServiceImpl(ProductRepository productRepository, MbPRepository mbPRepository, InventoryRepository inventoryRepository, OrderItemRepository orderItemRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.mbPRepository = mbPRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        Set<String> allCategories = new HashSet<>();
        allCategories.addAll(productRepository.findDistinctCategories());
        allCategories.addAll(mbPRepository.findDistinctCategories());

        List<CategoryDto> result = new ArrayList<>();
        for (String cat : allCategories) {
            Set<String> subs = new HashSet<>();
            subs.addAll(productRepository.findDistinctSubcategoriesByCategory(cat));
            subs.addAll(mbPRepository.findDistinctSubcategoriesByCategory(cat));
            result.add(new CategoryDto(cat, new ArrayList<>(subs)));
        }
        return result;
    }

    @Override
    public PagedSalesReportDto getSalesReport(String fromStr, String toStr, String category, String subcategory, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("order.orderDate").descending());

        Specification<OrderItemEntity> spec = (root, query, cb) -> {
            if (fromStr != null && !fromStr.isEmpty() && toStr != null && !toStr.isEmpty()) {
                return cb.between(root.get("order").get("orderDate"), fromStr, toStr);
            }
            return cb.conjunction();
        };

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("product").get("productCategory"), category),
                    cb.equal(root.get("mbP").get("category"), category)
            ));
        }

        if (subcategory != null && !subcategory.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("product").get("productSubCategory"), subcategory),
                    cb.equal(root.get("mbP").get("subCategory"), subcategory)
            ));
        }

        Page<OrderItemEntity> pageResult = orderItemRepository.findAll(spec, pageable);

        List<SalesReportItemDto> items = pageResult.getContent().stream().map(item -> {
            String prodName = item.getProduct() != null ? item.getProduct().getProductName() :
                    (item.getMbP() != null ? item.getMbP().getTitle() : "Unknown");
            String cat = item.getProduct() != null ? item.getProduct().getProductCategory() :
                    (item.getMbP() != null ? item.getMbP().getCategory() : "Unknown");
            String sub = item.getProduct() != null ? item.getProduct().getProductSubCategory() :
                    (item.getMbP() != null ? item.getMbP().getSubCategory() : "Unknown");
            String custName = (item.getOrder().getCustomerFirstName() != null ? item.getOrder().getCustomerFirstName() : "") +
                    " " + (item.getOrder().getCustomerLastName() != null ? item.getOrder().getCustomerLastName() : "");

            return new SalesReportItemDto(
                    item.getOrderItemId(),
                    prodName,
                    cat,
                    sub,
                    item.getQuantity(),
                    item.getSubtotal(),
                    item.getOrder().getOrderDate(),
                    item.getOrder().getOrderStatus(),
                    custName.trim().isEmpty() ? "Anonymous" : custName.trim()
            );
        }).collect(Collectors.toList());

        // Full summaries
        List<OrderItemEntity> allMatching = orderItemRepository.findAll(spec);

        BigDecimal totalRevenue = allMatching.stream()
                .map(item -> item.getSubtotal() != null ? BigDecimal.valueOf(item.getSubtotal()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String topProduct = allMatching.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct() != null ? item.getProduct().getProductName() :
                                (item.getMbP() != null ? item.getMbP().getTitle() : "Unknown"),
                        Collectors.summingInt(OrderItemEntity::getQuantity)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        long totalOrders = allMatching.size();

        BigDecimal totalProfit = totalRevenue.multiply(new BigDecimal("0.25"));

        return new PagedSalesReportDto(
                items,
                pageResult.getTotalElements(),
                page,
                limit,
                totalRevenue,
                topProduct,
                totalOrders,
                totalProfit
        );
    }



    @Override
    public PagedInventoryReportDto getInventoryReport(String fromStr, String toStr, String category, String subcategory, boolean lowStockOnly, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("lastUpdated").descending());

        Specification<InventoryEntity> spec = (root, query, cb) -> cb.conjunction();

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("product").get("productCategory"), category),
                    cb.equal(root.get("mbp").get("category"), category)
            ));
        }

        if (subcategory != null && !subcategory.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("product").get("productSubCategory"), subcategory),
                    cb.equal(root.get("mbp").get("subCategory"), subcategory)
            ));
        }

        if (lowStockOnly) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("quantity"), 10));
        }

        Page<InventoryEntity> pageResult = inventoryRepository.findAll(spec, pageable);

        List<InventoryReportItemDto> items = pageResult.getContent().stream().map(item -> {
            String prodName = item.getProduct() != null ? item.getProduct().getProductName() :
                    (item.getMbp() != null ? item.getMbp().getTitle() : "Unknown");
            String cat = item.getProduct() != null ? item.getProduct().getProductCategory() :
                    (item.getMbp() != null ? item.getMbp().getCategory() : "Unknown");
            String sub = item.getProduct() != null ? item.getProduct().getProductSubCategory() :
                    (item.getMbp() != null ? item.getMbp().getSubCategory() : "Unknown");

            Integer totalStock = item.getProduct() != null ? item.getProduct().getTotalCalculatedStock() :
                    (item.getMbp() != null ? item.getMbp().getTotalCalculatedStock() : item.getQuantity());

            BigDecimal price = item.getProduct() != null && !item.getProduct().getProductPrice().isEmpty() ?
                    item.getProduct().getProductPrice().get(0) : new BigDecimal("100");
            BigDecimal stockValue = new BigDecimal(totalStock).multiply(price);

            return new InventoryReportItemDto(
                    item.getInventoryId(),
                    prodName,
                    cat,
                    sub,
                    totalStock,
                    stockValue,
                    item.getExpDate(),
                    item.getBatchNo(),
                    item.getStockStatus()
            );
        }).collect(Collectors.toList());

        List<InventoryEntity> allMatching = inventoryRepository.findAll(spec);

        BigDecimal totalStockValue = BigDecimal.ZERO;
        long lowStockCount = 0;
        long expiringSoonCount = 0;
        for (InventoryEntity i : allMatching) {
            Integer qty = i.getProduct() != null ? i.getProduct().getTotalCalculatedStock() :
                    (i.getMbp() != null ? i.getMbp().getTotalCalculatedStock() : i.getQuantity());
            BigDecimal price = i.getProduct() != null && !i.getProduct().getProductPrice().isEmpty() ?
                    i.getProduct().getProductPrice().get(0) : new BigDecimal("100");
            totalStockValue = totalStockValue.add(new BigDecimal(qty).multiply(price));

            if (i.getQuantity() < 10) lowStockCount++;

            if (i.getExpDate() != null) {
                try {
                    Date exp = dateFormat.parse(i.getExpDate());
                    if (exp.before(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))) {
                        expiringSoonCount++;
                    }
                } catch (Exception ignored) {}
            }
        }

        long totalProducts = allMatching.size();

        return new PagedInventoryReportDto(
                items,
                pageResult.getTotalElements(),
                page,
                limit,
                totalStockValue,
                lowStockCount,
                expiringSoonCount,
                totalProducts
        );
    }

    @Override
    public PagedCustomerReportDto getCustomerReport(String fromStr, String toStr, String category, String subcategory, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("orderDate").descending());

        Specification<OrderEntity> spec = (root, query, cb) -> cb.conjunction();

        Page<OrderEntity> ordersPage = orderRepository.findAll(spec, pageable);

        Map<String, List<OrderEntity>> customerOrders = ordersPage.getContent().stream()
                .collect(Collectors.groupingBy(
                        o -> (o.getCustomerFirstName() != null ? o.getCustomerFirstName() : "") + " " +
                                (o.getCustomerLastName() != null ? o.getCustomerLastName() : "")
                ));

        List<CustomerReportItemDto> items = new ArrayList<>();
        for (Map.Entry<String, List<OrderEntity>> entry : customerOrders.entrySet()) {
            String custName = entry.getKey().trim().isEmpty() ? "Anonymous" : entry.getKey().trim();
            List<OrderEntity> orders = entry.getValue();

            long totalOrdersCount = orders.size();

            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (OrderEntity o : orders) {
                for (OrderItemEntity i : o.getOrderItems()) {
                    if (i.getSubtotal() != null) {
                        totalRevenue = totalRevenue.add(BigDecimal.valueOf(i.getSubtotal()));
                    }
                }
            }

            Date lastOrderDate = orders.stream()
                    .map(o -> {
                        try {
                            return dateFormat.parse(o.getOrderDate());
                        } catch (ParseException e) {
                            return new Date(0);
                        }
                    })
                    .max(Comparator.naturalOrder())
                    .orElse(new Date());

            String topCategory = "N/A";
            Map<String, Long> catCount = new HashMap<>();
            for (OrderEntity o : orders) {
                for (OrderItemEntity i : o.getOrderItems()) {
                    String cat = i.getProduct() != null ? i.getProduct().getProductCategory() :
                            (i.getMbP() != null ? i.getMbP().getCategory() : "Unknown");
                    catCount.put(cat, catCount.getOrDefault(cat, 0L) + 1);
                }
            }
            if (!catCount.isEmpty()) {
                topCategory = Collections.max(catCount.entrySet(), Map.Entry.comparingByValue()).getKey();
            }

            items.add(new CustomerReportItemDto(
                    null,
                    custName,
                    totalOrdersCount,
                    totalRevenue,
                    lastOrderDate,
                    topCategory
            ));
        }

        long totalUnique = customerOrders.size();

        String topCustomer = customerOrders.entrySet().stream()
                .max(Comparator.comparingLong(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse("N/A");

        BigDecimal avgOrderValue = totalUnique > 0 ?
                BigDecimal.valueOf(customerOrders.values().stream()
                        .mapToDouble(list -> list.stream()
                                .flatMap(o -> o.getOrderItems().stream())
                                .mapToDouble(i -> i.getSubtotal() != null ? i.getSubtotal().doubleValue() : 0.0)
                                .sum() / list.size())
                        .average().orElse(0.0)) :
                BigDecimal.ZERO;

        return new PagedCustomerReportDto(
                items,
                totalUnique,
                page,
                limit,
                totalUnique,
                topCustomer,
                avgOrderValue
        );
    }

    @Override
    public FinancialSummaryDto getFinancialSummary(String fromStr, String toStr, String groupBy) {
        List<OrderEntity> allOrders = orderRepository.findAll();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (OrderEntity o : allOrders) {
            for (OrderItemEntity i : o.getOrderItems()) {
                if (i.getSubtotal() != null) {
                    totalRevenue = totalRevenue.add(BigDecimal.valueOf(i.getSubtotal()));
                }
            }
        }

        BigDecimal totalExpenses = totalRevenue.multiply(new BigDecimal("0.75"));
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        double profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                netProfit.divide(totalRevenue, 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0.0;

        List<FinancialPeriodDto> breakdown = new ArrayList<>();
        // Simple dummy monthly breakdown
        for (int i = 1; i <= 12; i++) {
            BigDecimal rev = totalRevenue.divide(new BigDecimal("12"), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal exp = rev.multiply(new BigDecimal("0.75"));
            breakdown.add(new FinancialPeriodDto("2025-" + String.format("%02d", i), rev, exp, rev.subtract(exp)));
        }

        return new FinancialSummaryDto(totalRevenue, totalExpenses, netProfit, profitMargin, breakdown);
    }
}