package com.gn.pharmacy.dto.request;

import com.gn.pharmacy.entity.UserEntity;

public class UserDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String addressLandmark;
    private String addressArea;
    private String addressCity;
    private String addressPincode;
    private String addressState;
    private String addressType;



    // Constructor from UserEntity
    public UserDTO(UserEntity user) {
        this.userId = user.getUserId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.addressLandmark = user.getAddressLandmark();
        this.addressArea = user.getAddressArea();
        this.addressCity = user.getAddressCity();
        this.addressPincode = user.getAddressPincode();
        this.addressState = user.getAddressState();
        this.addressType = user.getAddressType();
    }

    // Getters and Setters


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddressLandmark() { return addressLandmark; }
    public void setAddressLandmark(String addressLandmark) { this.addressLandmark = addressLandmark; }

    public String getAddressArea() { return addressArea; }
    public void setAddressArea(String addressArea) { this.addressArea = addressArea; }

    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }

    public String getAddressPincode() { return addressPincode; }
    public void setAddressPincode(String addressPincode) { this.addressPincode = addressPincode; }

    public String getAddressState() { return addressState; }
    public void setAddressState(String addressState) { this.addressState = addressState; }


    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }
}