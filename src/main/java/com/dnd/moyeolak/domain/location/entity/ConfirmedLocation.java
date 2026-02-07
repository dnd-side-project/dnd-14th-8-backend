package com.dnd.moyeolak.domain.location.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class ConfirmedLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_poll_id")
    private LocationPoll locationPoll;

    @Column(comment = "확정된 장소")
    private String confirmedLocation;

    @Column(comment = "확정된 장소 위도", precision = 10, scale = 7)
    private BigDecimal confirmedLat;

    @Column(comment = "확정된 장소 경도", precision =  10, scale = 7)
    private BigDecimal confirmedLng;
}
