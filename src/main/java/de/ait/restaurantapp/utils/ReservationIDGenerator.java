package de.ait.restaurantapp.utils;

import java.util.UUID;

public class ReservationIDGenerator {
    public static String generateReservationId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis()); // Timestamp do ID
        return "RES-" + timestamp + "-" + uuid.substring(0, 8);
    }
}