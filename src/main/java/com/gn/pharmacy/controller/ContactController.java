package com.gn.pharmacy.controller;


import com.gn.pharmacy.dto.request.ContactRequestDTO;
import com.gn.pharmacy.dto.response.ContactResponseDTO;
import com.gn.pharmacy.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @PostMapping("/submit-form")
    public ResponseEntity<String> submitForm(@Valid @RequestBody ContactRequestDTO contactRequestDTO) {
        logger.info("Received contact form submission: {}", contactRequestDTO);
        contactService.saveContact(contactRequestDTO);
        return ResponseEntity.ok("Thank you for your message!");
    }

    @GetMapping("/get-all-contact-us")
    public ResponseEntity<List<ContactResponseDTO>> getAllContacts() {
        List<ContactResponseDTO> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("get-by-formId/{formId}")
    public ResponseEntity<ContactResponseDTO> getContactById(@PathVariable Long formId) {
        ContactResponseDTO contact = contactService.getContactById(formId);
        return ResponseEntity.ok(contact);
    }

    @DeleteMapping("delete-by-formId/{formId}")
    public ResponseEntity<String> deleteContact(@PathVariable Long formId) {
        contactService.deleteContact(formId);
        return ResponseEntity.ok("Contact deleted successfully!");
    }


}
