package com.gn.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Exchange {

    @Column(name = "mb_product_id")
    private Long mbProductId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "exchange_reason", length = 255)
    private String exchangeReason;

    @Column(name = "exchange_size", length = 50)
    private String exchangeSize;

    @Column(name = "product_size", length = 50)
    private String productSize;

    @Column(name = "exchange_status", length = 50)
    private String exchangeStatus = "in-process";

    @Column(name = "is_exchanged")
    private boolean isExchanged = false;

    // Constructors
    public Exchange() {}

    // Getters and Setters
    public Long getMbProductId() {
        return mbProductId;
    }

    public void setMbProductId(Long mbProductId) {
        this.mbProductId = mbProductId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getExchangeReason() {
        return exchangeReason;
    }

    public void setExchangeReason(String exchangeReason) {
        this.exchangeReason = exchangeReason;
    }

    public String getExchangeSize() {
        return exchangeSize;
    }

    public void setExchangeSize(String exchangeSize) {
        this.exchangeSize = exchangeSize;
    }

    public String getProductSize() {
        return productSize;
    }

    public void setProductSize(String productSize) {
        this.productSize = productSize;
    }

    public String getExchangeStatus() {
        return exchangeStatus;
    }

    public void setExchangeStatus(String exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

    public boolean isExchanged() {
        return isExchanged;
    }

    public void setExchanged(boolean exchanged) {
        isExchanged = exchanged;
    }
}