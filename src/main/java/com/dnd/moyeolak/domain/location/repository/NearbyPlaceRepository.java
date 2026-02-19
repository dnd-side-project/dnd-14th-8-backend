package com.dnd.moyeolak.domain.location.repository;

import com.dnd.moyeolak.domain.location.entity.NearbyPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface NearbyPlaceRepository extends JpaRepository<NearbyPlace, Long> {

    List<NearbyPlace> findByBaseLatitudeAndBaseLongitude(BigDecimal baseLatitude, BigDecimal baseLongitude);

    @Transactional
    void deleteByBaseLatitudeAndBaseLongitude(BigDecimal baseLatitude, BigDecimal baseLongitude);
}
