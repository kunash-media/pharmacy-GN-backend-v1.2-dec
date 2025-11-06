package com.gn.pharmacy.dto.request;public class ShippingAddressDTO {

    private Long shippingId;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private Integer flat_no;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;


    // === NEW FIELDS ===
    private String nearBy;
    private String landmark;

    // Getters and Setters


    public Integer getFlat_no() {
        return flat_no;
    }

    public void setFlat_no(Integer flat_no) {
        this.flat_no = flat_no;
    }

    public Long getShippingId() { return shippingId; }
    public void setShippingId(Long shippingId) { this.shippingId = shippingId; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }

    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }

    public String getShippingPincode() { return shippingPincode; }
    public void setShippingPincode(String shippingPincode) { this.shippingPincode = shippingPincode; }


    // === NEW GETTERS & SETTERS ===
    public String getNearBy() { return nearBy; }
    public void setNearBy(String nearBy) { this.nearBy = nearBy; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
}