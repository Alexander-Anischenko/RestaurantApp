package de.ait.restaurantapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailDto {
    private String to;
    private String reservationCode;
    private String name;

    private final String subject = "Подтверждение брони";

    public EmailDto(String to, String restaurantCode, String name) {
        this.to = to;
        this.reservationCode = restaurantCode;
        this.name = name;
    }
}
