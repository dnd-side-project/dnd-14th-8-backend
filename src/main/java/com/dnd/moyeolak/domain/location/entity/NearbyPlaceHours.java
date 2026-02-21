package com.dnd.moyeolak.domain.location.entity;

import com.dnd.moyeolak.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NearbyPlaceHours extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nearby_place_hours_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearby_place_id", nullable = false)
    private NearbyPlace nearbyPlace;

    @Column(comment = "영업 시작 요일 (0=일, 1=월, ..., 6=토)", nullable = false)
    private Integer openDay;

    @Column(comment = "영업 시작 시 (0~23)", nullable = false)
    private Integer openHour;

    @Column(comment = "영업 시작 분 (0~59)", nullable = false)
    private Integer openMinute;

    @Column(comment = "영업 종료 요일", nullable = false)
    private Integer closeDay;

    @Column(comment = "영업 종료 시 (0~23)", nullable = false)
    private Integer closeHour;

    @Column(comment = "영업 종료 분 (0~59)", nullable = false)
    private Integer closeMinute;

    public static NearbyPlaceHours of(
            NearbyPlace nearbyPlace,
            Integer openDay, Integer openHour, Integer openMinute,
            Integer closeDay, Integer closeHour, Integer closeMinute
    ) {
        return NearbyPlaceHours.builder()
                .nearbyPlace(nearbyPlace)
                .openDay(openDay)
                .openHour(openHour)
                .openMinute(openMinute)
                .closeDay(closeDay)
                .closeHour(closeHour)
                .closeMinute(closeMinute)
                .build();
    }
}
