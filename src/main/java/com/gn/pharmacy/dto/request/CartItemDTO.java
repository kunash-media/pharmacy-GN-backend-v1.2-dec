package com.gn.pharmacy.dto.request;

public class CartItemDTO {
    private String type; // "PRODUCT" or "MBP"
    private Long itemId; // productId or mbp.id
    private Integer quantity;
    private String selectedSize;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }
}