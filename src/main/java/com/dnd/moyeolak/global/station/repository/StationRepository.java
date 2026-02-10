package com.dnd.moyeolak.global.station.repository;

import com.dnd.moyeolak.global.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {

    /**
     * 무게중심 좌표에서 반경 내 가장 가까운 지하철역 검색 (PostGIS)
     */
    @Query(value = """
            SELECT s.*
            FROM stations s
            WHERE ST_DWithin(
                s.geom::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY ST_Distance(
                s.geom::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            ) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Station> findNearbyStations(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") int radiusMeters,
            @Param("limit") int limit
    );

    /**
     * 여러 좌표의 무게중심 계산 (PostGIS ST_Centroid + ST_Collect)
     *
     * @param wktPoints WKT 형식의 POINT 문자열 배열 (e.g., "SRID=4326;POINT(126.9 37.5)")
     * @return [latitude, longitude] Object 배열
     */
    @Query(value = """
            SELECT ST_Y(centroid) AS lat, ST_X(centroid) AS lng
            FROM (
                SELECT ST_Centroid(ST_Collect(geom)) AS centroid
                FROM unnest(CAST(:wktPoints AS geometry[])) AS geom
            ) sub
            """, nativeQuery = true)
    Object[] calculateCentroid(@Param("wktPoints") String[] wktPoints);
}
