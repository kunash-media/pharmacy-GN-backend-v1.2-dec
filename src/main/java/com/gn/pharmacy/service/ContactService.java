package com.gn.pharmacy.service;


import com.gn.pharmacy.dto.request.ContactRequestDTO;
import com.gn.pharmacy.dto.response.ContactResponseDTO;

import java.util.List;

public interface ContactService {
    ContactResponseDTO saveContact(ContactRequestDTO contactRequestDTO);
    List<ContactResponseDTO> getAllContacts();

    ContactResponseDTO getContactById(Long formId);
    void deleteContact(Long formId);
}
