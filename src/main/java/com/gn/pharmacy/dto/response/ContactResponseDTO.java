package com.gn.pharmacy.dto.response;


public class ContactResponseDTO {
    private Long formId;
    private String name;
    private String email;
    private String phone;
    private String message;

    // Constructors
    public ContactResponseDTO() {}

    public ContactResponseDTO(Long formId, String name, String email, String phone, String message) {
        this.formId = formId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
    }

    // Getters and Setters
    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
