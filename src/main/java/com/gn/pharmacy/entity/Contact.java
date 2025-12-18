package com.gn.pharmacy.entity;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;

@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long formId;

    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    // Make phone optional
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    @Column(name = "phone", length = 15)
    private String phone;

    @NotBlank(message = "Message is mandatory")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    public Contact() {
    }

    public Contact(Long formId, String name, String email, String phone, String message) {
        this.formId = formId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
    }

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
