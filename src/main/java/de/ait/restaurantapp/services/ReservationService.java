package de.ait.restaurantapp.services;
import de.ait.restaurantapp.dto.ReservationFormDto;
import de.ait.restaurantapp.model.Reservation;
import jakarta.mail.MessagingException;

import java.util.List;

public interface ReservationService {

    Reservation createReservation(ReservationFormDto form) throws MessagingException;

    List<Reservation> getAllReservations();


    boolean cancelReservation(String reservationCode);

    List<Reservation> getReservationsForTableToday(Integer tableId);


    
}
