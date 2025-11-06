package com.gn.pharmacy.dto.response;

public class ShippingAddressResponseDto {

    private Long shippingId;
    private UserDto user;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private Integer flat_no;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;
    private String nearBy;
    private String landmark;

    // Default constructor
    public ShippingAddressResponseDto() {}

    public ShippingAddressResponseDto(Long shippingId, UserDto user, String customerPhone, String customerEmail, String shippingAddress, Integer flat_no, String shippingCity, String shippingState, String shippingPincode, String nearBy, String landmark) {
        this.shippingId = shippingId;
        this.user = user;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.flat_no = flat_no;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingPincode = shippingPincode;
        this.nearBy = nearBy;
        this.landmark = landmark;
    }

    // Inner class for User
    public static class UserDto {
        private Long userId;
        private String username;
        private String email;

        public UserDto() {}

        public UserDto(Long userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Getters and Setters

    public String getNearBy() { return nearBy; }
    public void setNearBy(String nearBy) { this.nearBy = nearBy; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public Long getShippingId() { return shippingId; }
    public void setShippingId(Long shippingId) { this.shippingId = shippingId; }

    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

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

    public Integer getFlat_no() {
        return flat_no;
    }

    public void setFlat_no(Integer flat_no) {
        this.flat_no = flat_no;
    }
}
