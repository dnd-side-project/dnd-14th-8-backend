package com.dnd.moyeolak.global.station.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "stations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String line;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    public double getLatitude() {
        return geom.getY();
    }

    public double getLongitude() {
        return geom.getX();
    }
}
