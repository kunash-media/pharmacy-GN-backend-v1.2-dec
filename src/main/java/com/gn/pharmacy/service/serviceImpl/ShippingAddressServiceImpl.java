package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.request.ShippingAddressDTO;
import com.gn.pharmacy.entity.ShippingAddressEntity;
import com.gn.pharmacy.entity.UserEntity;
import com.gn.pharmacy.repository.ShippingAddressRepository;
import com.gn.pharmacy.repository.UserRepository;
import com.gn.pharmacy.service.ShippingAddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository addressRepository;
    private final UserRepository userRepository;

    public ShippingAddressServiceImpl(ShippingAddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ShippingAddressDTO createAddress(Long userId, ShippingAddressDTO addressDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ShippingAddressEntity addressEntity = new ShippingAddressEntity();
        BeanUtils.copyProperties(addressDTO, addressEntity);
        addressEntity.setUser(user);

        addressEntity = addressRepository.save(addressEntity);

        addressDTO.setShippingId(addressEntity.getShippingId());
        return addressDTO;
    }
    @Override
    public List<ShippingAddressDTO> getAddressesByUserId(Long userId) {
        System.out.println("=== DEBUG: getAddressesByUserId called ===");
        System.out.println("User ID: " + userId);

        // Check if user exists
        boolean userExists = userRepository.existsById(userId);
        System.out.println("User exists: " + userExists);

        if (!userExists) {
            System.out.println("ERROR: User not found with ID: " + userId);
            return new ArrayList<>();
        }

        // Get addresses using different methods to test
        List<ShippingAddressEntity> addresses = addressRepository.findByUserUserId(userId);
        System.out.println("Addresses found with findByUserUserId: " + addresses.size());

        // Alternative approach - check if this works
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<ShippingAddressEntity> addressesAlt = addressRepository.findByUser(user);
            System.out.println("Addresses found with findByUser: " + addressesAlt.size());

            // Also try manual query
            List<ShippingAddressEntity> addressesManual = addressRepository.findAll().stream()
                    .filter(addr -> addr.getUser() != null && addr.getUser().getUserId().equals(userId))
                    .collect(Collectors.toList());
            System.out.println("Addresses found manually: " + addressesManual.size());
        }

        // Log the actual address entities
        if (!addresses.isEmpty()) {
            System.out.println("Address details:");
            for (ShippingAddressEntity addr : addresses) {
                System.out.println(" - ID: " + addr.getShippingId() + ", User: " +
                        (addr.getUser() != null ? addr.getUser().getUserId() : "null"));
            }
        }

        return addresses.stream()
                .map(addressEntity -> {
                    ShippingAddressDTO dto = new ShippingAddressDTO();
                    BeanUtils.copyProperties(addressEntity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ShippingAddressDTO> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(addressEntity -> {
                    ShippingAddressDTO dto = new ShippingAddressDTO();
                    BeanUtils.copyProperties(addressEntity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ShippingAddressDTO updateAddress(Long userId, Long shippingId, ShippingAddressDTO addressDTO) {
        ShippingAddressEntity addressEntity = addressRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!addressEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }

        // Update only non-null fields from the DTO
        if (addressDTO.getCustomerPhone() != null) {
            addressEntity.setCustomerPhone(addressDTO.getCustomerPhone());
        }
        if (addressDTO.getCustomerEmail() != null) {
            addressEntity.setCustomerEmail(addressDTO.getCustomerEmail());
        }
        if (addressDTO.getShippingAddress() != null) {
            addressEntity.setShippingAddress(addressDTO.getShippingAddress());
        }
        if (addressDTO.getShippingCity() != null) {
            addressEntity.setShippingCity(addressDTO.getShippingCity());
        }
        if (addressDTO.getShippingState() != null) {
            addressEntity.setShippingState(addressDTO.getShippingState());
        }
        if (addressDTO.getShippingPincode() != null) {
            addressEntity.setShippingPincode(addressDTO.getShippingPincode());
        }
        if (addressDTO.getFlat_no() != null) {
            addressEntity.setFlat_no(addressDTO.getFlat_no());
        }

        if (addressDTO.getNearBy() != null) {
            addressEntity.setNearBy(addressDTO.getNearBy());
        }
        if (addressDTO.getLandmark() != null) {
            addressEntity.setLandmark(addressDTO.getLandmark());
        }

        addressEntity = addressRepository.save(addressEntity);

        ShippingAddressDTO updatedDTO = new ShippingAddressDTO();
        BeanUtils.copyProperties(addressEntity, updatedDTO);
        return updatedDTO;
    }


    @Override
    public void deleteAddress(Long userId, Long shippingId) {
        ShippingAddressEntity addressEntity = addressRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!addressEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }

        addressRepository.delete(addressEntity);
    }
}