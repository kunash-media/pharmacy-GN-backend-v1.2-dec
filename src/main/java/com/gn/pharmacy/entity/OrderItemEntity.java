package com.gn.pharmacy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbp_id")
    private MbPEntity  MbP;

    private int quantity;
    private Double itemPrice;
    private Double itemOldPrice;
    private Double subtotal;
    private String itemName;

    // Constructors
    public OrderItemEntity() {}

    public OrderItemEntity(Long orderItemId, OrderEntity order, ProductEntity product, MbPEntity mbP, int quantity, Double itemPrice, Double itemOldPrice, Double subtotal, String itemName) {
        this.orderItemId = orderItemId;
        this.order = order;
        this.product = product;
        this.MbP = mbP;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.itemOldPrice = itemOldPrice;
        this.subtotal = subtotal;
        this.itemName = itemName;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public MbPEntity getMbP() {
        return MbP;
    }

    public void setMbP(MbPEntity mbP) {
        MbP = mbP;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Double getItemOldPrice() {
        return itemOldPrice;
    }

    public void setItemOldPrice(Double itemOldPrice) {
        this.itemOldPrice = itemOldPrice;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}