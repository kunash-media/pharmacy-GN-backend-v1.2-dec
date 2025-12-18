package com.gn.pharmacy.repository;


import com.gn.pharmacy.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
