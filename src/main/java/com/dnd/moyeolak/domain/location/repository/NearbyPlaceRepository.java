package com.dnd.moyeolak.domain.location.repository;

import com.dnd.moyeolak.domain.location.entity.NearbyPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface NearbyPlaceRepository extends JpaRepository<NearbyPlace, Long> {

    @Query("SELECT DISTINCT np FROM NearbyPlace np LEFT JOIN FETCH np.nearbyPlaceHours WHERE np.baseLatitude = :lat AND np.baseLongitude = :lng")
    List<NearbyPlace> findByBaseLatitudeAndBaseLongitude(@Param("lat") BigDecimal baseLatitude, @Param("lng") BigDecimal baseLongitude);

    @Transactional
    void deleteByBaseLatitudeAndBaseLongitude(BigDecimal baseLatitude, BigDecimal baseLongitude);
}
