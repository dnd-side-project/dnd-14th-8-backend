package com.dnd.moyeolak.test.janghh.service;

import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest.ParticipantInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class GeographicCalculator {

    public double[] calculateGeographicCenter(List<ParticipantInfo> participants) {
        double x = 0, y = 0, z = 0;

        for (ParticipantInfo p : participants) {
            double latRad = Math.toRadians(p.latitude());
            double lngRad = Math.toRadians(p.longitude());

            x += Math.cos(latRad) * Math.cos(lngRad);
            y += Math.cos(latRad) * Math.sin(lngRad);
            z += Math.sin(latRad);
        }

        int n = participants.size();
        x /= n;
        y /= n;
        z /= n;

        double centerLng = Math.atan2(y, x);
        double hyp = Math.sqrt(x * x + y * y);
        double centerLat = Math.atan2(z, hyp);

        return new double[]{
            Math.toDegrees(centerLat),
            Math.toDegrees(centerLng)
        };
    }

    public double calculateDistance(
        double lat1, double lng1,
        double lat2, double lng2
    ) {
        final double R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) *
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
