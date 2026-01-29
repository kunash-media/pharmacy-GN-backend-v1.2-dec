package com.gn.pharmacy.dto.request;

public class ExchangeRequestDto {

    private String exchangeReason;
    private String exchangeSize;

    // Constructors
    public ExchangeRequestDto() {}

    // Getters and Setters
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
}