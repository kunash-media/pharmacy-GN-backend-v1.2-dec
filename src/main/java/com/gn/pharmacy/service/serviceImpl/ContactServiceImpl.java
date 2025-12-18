package com.gn.pharmacy.service.serviceImpl;


import com.gn.pharmacy.dto.request.ContactRequestDTO;
import com.gn.pharmacy.dto.response.ContactResponseDTO;
import com.gn.pharmacy.entity.Contact;
import com.gn.pharmacy.repository.ContactRepository;
import com.gn.pharmacy.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {
    @Autowired
    private ContactRepository contactRepository;

    @Override
    public ContactResponseDTO saveContact(ContactRequestDTO contactRequestDTO) {
        Contact contact = new Contact();
        contact.setName(contactRequestDTO.getName());
        contact.setEmail(contactRequestDTO.getEmail());
        contact.setPhone(contactRequestDTO.getPhone());
        contact.setMessage(contactRequestDTO.getMessage());
        Contact savedContact = contactRepository.save(contact);
        return convertToResponseDTO(savedContact);
    }


    @Override
    public List<ContactResponseDTO> getAllContacts() {
        return contactRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    // Helper methods for mapping
    private Contact mapToEntity(ContactRequestDTO dto) {
        Contact contact = new Contact();
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setMessage(dto.getMessage());
        return contact;
    }

    @Override
    public ContactResponseDTO getContactById(Long formId) {
        Contact contact = contactRepository.findById(formId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found with id: " + formId));
        return convertToResponseDTO(contact);
    }

    @Override
    public void deleteContact(Long formId) {
        if (!contactRepository.existsById(formId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found with id: " + formId);
        }
        contactRepository.deleteById(formId);
    }

    private ContactResponseDTO convertToResponseDTO(Contact contact) {
        ContactResponseDTO responseDTO = new ContactResponseDTO();
        responseDTO.setFormId(contact.getFormId());
        responseDTO.setName(contact.getName());
        responseDTO.setEmail(contact.getEmail());
        responseDTO.setPhone(contact.getPhone());
        responseDTO.setMessage(contact.getMessage());
        return responseDTO;
    }
}
