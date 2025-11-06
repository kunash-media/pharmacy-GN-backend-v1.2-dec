package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<BannerEntity, Long> {

    Optional<BannerEntity> findByPageName(String pageName);

}
