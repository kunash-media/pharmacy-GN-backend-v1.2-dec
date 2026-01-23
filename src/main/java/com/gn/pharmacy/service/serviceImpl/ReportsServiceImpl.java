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
import java.time.LocalDate;
import java.time.ZoneId;
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

    public ReportsServiceImpl(ProductRepository productRepository, MbPRepository mbPRepository,
                              InventoryRepository inventoryRepository, OrderItemRepository orderItemRepository,
                              OrderRepository orderRepository) {
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
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (fromStr != null && !fromStr.isEmpty() && toStr != null && !toStr.isEmpty()) {
                try {
                    Date from = dateFormat.parse(fromStr);
                    Date to = dateFormat.parse(toStr);
                    predicates.add(cb.between(root.get("order").get("orderDate"), from, to));
                } catch (ParseException ignored) {}
            }

            if (category != null && !category.isEmpty()) {
                predicates.add(cb.or(
                        cb.equal(root.get("product").get("productCategory"), category),
                        cb.equal(root.get("mbP").get("category"), category)
                ));
            }

            if (subcategory != null && !subcategory.isEmpty()) {
                predicates.add(cb.or(
                        cb.equal(root.get("product").get("productSubCategory"), subcategory),
                        cb.equal(root.get("mbP").get("subCategory"), subcategory)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

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
                    item.getSubtotal() != null ? item.getSubtotal() : 0.0,
                    item.getOrder().getOrderDate(),
                    item.getOrder().getOrderStatus(),
                    custName.trim().isEmpty() ? "Anonymous" : custName.trim()
            );
        }).collect(Collectors.toList());

        List<OrderItemEntity> allMatching = orderItemRepository.findAll(spec);

        BigDecimal totalRevenue = allMatching.stream()
                .map(item -> item.getSubtotal() != null ? new BigDecimal(item.getSubtotal().toString()) : BigDecimal.ZERO)
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

        long totalOrders = allMatching.stream()
                .map(OrderItemEntity::getOrder)
                .distinct()
                .count();

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

        Specification<InventoryEntity> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isEmpty()) {
                predicates.add(cb.or(
                        cb.equal(root.get("product").get("productCategory"), category),
                        cb.equal(root.get("mbp").get("category"), category)
                ));
            }

            if (subcategory != null && !subcategory.isEmpty()) {
                predicates.add(cb.or(
                        cb.equal(root.get("product").get("productSubCategory"), subcategory),
                        cb.equal(root.get("mbp").get("subCategory"), subcategory)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<InventoryEntity> pageResult = inventoryRepository.findAll(spec, pageable);

        List<InventoryReportItemDto> items = pageResult.getContent().stream().map(item -> {
            String prodName = item.getProduct() != null ? item.getProduct().getProductName() :
                    (item.getMbp() != null ? item.getMbp().getTitle() : "Unknown");
            String cat = item.getProduct() != null ? item.getProduct().getProductCategory() :
                    (item.getMbp() != null ? item.getMbp().getCategory() : "Unknown");
            String sub = item.getProduct() != null ? item.getProduct().getProductSubCategory() :
                    (item.getMbp() != null ? item.getMbp().getSubCategory() : "Unknown");

            int totalStock = item.getVariants().stream()
                    .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                    .sum();

            BigDecimal price = item.getProduct() != null && !item.getProduct().getProductPrice().isEmpty() ?
                    item.getProduct().getProductPrice().get(0) : new BigDecimal("100");
            BigDecimal stockValue = new BigDecimal(totalStock).multiply(price);

            String expiry = item.getVariants().isEmpty() ? null :
                    item.getVariants().get(0).getExpDate();

            return new InventoryReportItemDto(
                    item.getInventoryId(),
                    prodName,
                    cat,
                    sub,
                    totalStock,
                    stockValue,
                    expiry,
                    item.getBatchNo(),
                    item.getStockStatus()
            );
        }).collect(Collectors.toList());

        List<InventoryEntity> allMatching = inventoryRepository.findAll(spec);

        BigDecimal totalStockValue = BigDecimal.ZERO;
        long lowStockCount = 0;
        long expiringSoonCount = 0;

        for (InventoryEntity inv : allMatching) {
            int totalQty = inv.getVariants().stream()
                    .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                    .sum();

            BigDecimal price = inv.getProduct() != null && !inv.getProduct().getProductPrice().isEmpty() ?
                    inv.getProduct().getProductPrice().get(0) : new BigDecimal("100");
            totalStockValue = totalStockValue.add(new BigDecimal(totalQty).multiply(price));

            if (totalQty < 10) lowStockCount++;

            boolean expiring = inv.getVariants().stream()
                    .anyMatch(v -> {
                        String exp = v.getExpDate();
                        if (exp == null || exp.trim().isEmpty()) return false;
                        try {
                            Date expDate = dateFormat.parse(exp);
                            return expDate.before(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
                        } catch (ParseException e) {
                            return false;
                        }
                    });
            if (expiring) expiringSoonCount++;
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

        if (fromStr != null && !fromStr.isEmpty() && toStr != null && !toStr.isEmpty()) {
            try {
                Date from = dateFormat.parse(fromStr);
                Date to = dateFormat.parse(toStr);
                spec = spec.and((root, q, c) -> c.between(root.get("orderDate"), from, to));
            } catch (ParseException ignored) {}
        }

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
                        totalRevenue = totalRevenue.add(new BigDecimal(i.getSubtotal().toString()));
                    }
                }
            }

            Date lastOrderDate = orders.stream()
                    .map(o -> {
                        try {
                            return dateFormat.parse(o.getOrderDate());
                        } catch (ParseException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            String topCategory = orders.stream()
                    .flatMap(o -> o.getOrderItems().stream())
                    .map(i -> i.getProduct() != null ? i.getProduct().getProductCategory() :
                            (i.getMbP() != null ? i.getMbP().getCategory() : "Unknown"))
                    .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

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

        // FIXED LINE: use record accessor totalRevenue() instead of getTotalRevenue()
        BigDecimal totalRevenueAll = items.stream()
                .map(item -> item.totalRevenue() != null ? item.totalRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = totalUnique > 0 ?
                totalRevenueAll.divide(BigDecimal.valueOf(totalUnique), 2, BigDecimal.ROUND_HALF_UP) :
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
        Specification<OrderEntity> spec = (root, query, cb) -> cb.conjunction();

        if (fromStr != null && !fromStr.isEmpty() && toStr != null && !toStr.isEmpty()) {
            try {
                Date from = dateFormat.parse(fromStr);
                Date to = dateFormat.parse(toStr);
                spec = spec.and((root, q, c) -> c.between(root.get("orderDate"), from, to));
            } catch (ParseException ignored) {}
        }

        List<OrderEntity> allOrders = orderRepository.findAll(spec);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (OrderEntity o : allOrders) {
            for (OrderItemEntity i : o.getOrderItems()) {
                if (i.getSubtotal() != null) {
                    totalRevenue = totalRevenue.add(new BigDecimal(i.getSubtotal().toString()));
                }
            }
        }

        BigDecimal totalExpenses = totalRevenue.multiply(new BigDecimal("0.75"));
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        double profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                netProfit.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0.0;

        Map<String, BigDecimal> monthlyRevenue = allOrders.stream()
                .filter(o -> o.getOrderDate() != null)
                .collect(Collectors.groupingBy(
                        o -> {
                            try {
                                Date date = dateFormat.parse(o.getOrderDate());
                                LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                return localDate.getYear() + "-" + String.format("%02d", localDate.getMonthValue());
                            } catch (Exception e) {
                                return "Unknown";
                            }
                        },
                        Collectors.reducing(BigDecimal.ZERO,
                                o -> o.getOrderItems().stream()
                                        .map(i -> i.getSubtotal() != null ? new BigDecimal(i.getSubtotal().toString()) : BigDecimal.ZERO)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                BigDecimal::add)
                ));

        List<FinancialPeriodDto> breakdown = monthlyRevenue.entrySet().stream()
                .map(e -> {
                    BigDecimal rev = e.getValue();
                    BigDecimal exp = rev.multiply(new BigDecimal("0.75"));
                    return new FinancialPeriodDto(e.getKey(), rev, exp, rev.subtract(exp));
                })
                .collect(Collectors.toList());

        return new FinancialSummaryDto(totalRevenue, totalExpenses, netProfit, profitMargin, breakdown);
    }
}