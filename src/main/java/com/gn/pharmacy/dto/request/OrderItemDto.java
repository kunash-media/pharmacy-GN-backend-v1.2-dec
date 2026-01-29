package com.gn.pharmacy.dto.request;


import java.util.ArrayList;
import java.util.List;

public class OrderItemDto {

    private Long orderItemId;
    private Long mbpId;

    private Long productId;
    private Integer quantity;
    private Double itemPrice;
    private Double itemOldPrice;
    private Double subtotal;
    private String itemName;

    private String size;

    private List<ExchangeDto> exchanges = new ArrayList<>();

    // ADD THIS FIELD
    private String productMainImage;  // URL like "/api/products/15/image"

    // Getters and Setters
    public OrderItemDto(){}


    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getMbpId() {
        return mbpId;
    }

    public void setMbpId(Long mbpId) {
        this.mbpId = mbpId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }


    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
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

    public String getProductMainImage() {
        return productMainImage;
    }

    public void setProductMainImage(String productMainImage) {
        this.productMainImage = productMainImage;
    }


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<ExchangeDto> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<ExchangeDto> exchanges) {
        this.exchanges = exchanges;
    }
}
