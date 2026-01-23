package com.gn.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BatchVariant {
    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "mfg_date")
    private String mfgDate;

    @Column(name = "exp_date")
    private String expDate;

    // Constructors, getters, setters
    public BatchVariant() {}

    public BatchVariant(String size, Integer quantity, String mfgDate, String expDate) {
        this.size = size;
        this.quantity = quantity;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
    }

    // getters/setters...


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }
}