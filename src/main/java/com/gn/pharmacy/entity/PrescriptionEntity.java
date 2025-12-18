package com.gn.pharmacy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions_orders")
@Data
public class PrescriptionEntity {

    @Id
    private String prescriptionId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "prescription_img", columnDefinition = "LONGBLOB")
    @Lob
    private byte[] prescriptionImg;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "is_approved")
    private boolean isApproved;

    @Column(name = "doctor_name")
    private String doctorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore  // Prevent infinite recursion in JSON serialization
    private UserEntity user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (orderStatus == null) {
            orderStatus = "PENDING";
        }
        if (!isApproved) {
            isApproved = false;
        }
    }

    public PrescriptionEntity() {
    }

    public PrescriptionEntity(String prescriptionId, String firstName, String lastName,
                              String mobileNumber, String email, LocalDateTime createdAt,
                              String orderStatus, byte[] prescriptionImg, String paymentMethod,
                              boolean isApproved, String doctorName, UserEntity user) {
        this.prescriptionId = prescriptionId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.prescriptionImg = prescriptionImg;
        this.paymentMethod = paymentMethod;
        this.isApproved = isApproved;
        this.doctorName = doctorName;
        this.user = user;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public byte[] getPrescriptionImg() {
        return prescriptionImg;
    }

    public void setPrescriptionImg(byte[] prescriptionImg) {
        this.prescriptionImg = prescriptionImg;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}