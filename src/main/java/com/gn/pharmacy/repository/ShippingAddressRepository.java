package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.ShippingAddressEntity;
import com.gn.pharmacy.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddressEntity, Long> {

    List<ShippingAddressEntity> findByUserUserId(Long userId);

    List<ShippingAddressEntity> findByUser(UserEntity user);
}

