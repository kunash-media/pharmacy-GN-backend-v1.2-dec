package com.gn.pharmacy.dto.request;

public class ExchangeDto {

    private Long mbProductId;
    private Long userId;
    private Long orderId;
    private String exchangeReason;
    private String exchangeSize;
    private String productSize;
    private String exchangeStatus;
    private boolean isExchanged;

    // Constructors
    public ExchangeDto() {}

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