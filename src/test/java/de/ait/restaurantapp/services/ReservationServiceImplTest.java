package de.ait.restaurantapp.services;

import de.ait.restaurantapp.dto.ReservationFormDto;
import de.ait.restaurantapp.enums.ReservationStatus;
import de.ait.restaurantapp.exception.NoAvailableTableException;
import de.ait.restaurantapp.model.Reservation;
import de.ait.restaurantapp.model.RestaurantTable;
import de.ait.restaurantapp.repositories.ReservationRepo;
import de.ait.restaurantapp.repositories.RestaurantTableRepo;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ReservationServiceImplTest {

    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    private RestaurantTableRepo restaurantTableRepo;
    @Autowired
    private ReservationServiceImpl reservationService;

    private ReservationFormDto r1, r1sameUser, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16;

    private LocalDate baseDate;

    @BeforeEach
    void setUp() throws MessagingException {
        // Очищаем данные в таблицах
        reservationRepo.deleteAll();
        restaurantTableRepo.deleteAll();
        // Инициализируем 10 столов вместимостью от 1 до 10
        for (int capacity = 1; capacity <= 10; capacity++) {
            RestaurantTable table = RestaurantTable.builder()
                    .capacity(capacity)
                    .build();
            restaurantTableRepo.save(table);
        }
        baseDate = LocalDate.now().plusDays(1);

        // валидные в рабочее время
        r1 = new ReservationFormDto(
                "User1", "u1@example.com", 2,
                baseDate.atTime(10, 0),
                baseDate.atTime(12, 0)
        );

        r1sameUser = new ReservationFormDto(
                "User1", "u1@example.com", 2,
                baseDate.atTime(18, 0),
                baseDate.atTime(20, 0)
        );
        r2 = new ReservationFormDto(
                "User2", "u2@example.com", 4,
                baseDate.atTime(11, 0),
                baseDate.atTime(13, 0)
        );
        r3 = new ReservationFormDto(
                "User3", "u3@example.com", 6,
                baseDate.atTime(14, 0),
                baseDate.atTime(16, 0)
        );

        // до открытия и пересекает закрытие
        r4 = new ReservationFormDto(
                "User4", "u4@example.com", 2,
                baseDate.atTime(7, 0),
                baseDate.atTime(9, 0)
        );
        r5 = new ReservationFormDto(
                "User5", "u5@example.com", 2,
                baseDate.atTime(19, 0),
                baseDate.atTime(21, 0)
        );

        // overlap внутри рабочего времени
        r6 = new ReservationFormDto(
                "User6", "u6@example.com", 3,
                baseDate.atTime(9, 0),
                baseDate.atTime(11, 0)
        );
        r7 = new ReservationFormDto(
                "User7", "u7@example.com", 4,
                baseDate.atTime(11, 0),
                baseDate.atTime(13, 0)
        );
        r8 = new ReservationFormDto(
                "User8", "u8@example.com", 2,
                baseDate.atTime(12, 0),
                baseDate.atTime(14, 0)
        );

        // граничные часы (открытие и закрытие)
        r9 = new ReservationFormDto(
                "User9", "u9@example.com", 2,
                baseDate.atTime(8, 0),
                baseDate.atTime(10, 0)
        );
        r10 = new ReservationFormDto(
                "User10", "u10@example.com", 2,
                baseDate.atTime(20, 0),
                baseDate.atTime(22, 0)
        );
        // старт вчера
        r11 = new ReservationFormDto(
                "User11",
                "u11@example.com",
                2,
                LocalDate.now().minusDays(1).atTime(10, 0),
                LocalDate.now().minusDays(1).atTime(12, 0)
        );

        // два валидных бронирования на 9 гостей в рабочие часы
        r12 = new ReservationFormDto(
                "User12",
                "nineA@example.com",
                9,
                baseDate.atTime(10, 0),
                baseDate.atTime(12, 0)
        );

        r13 = new ReservationFormDto(
                "User13",
                "nineB@example.com",
                9,
                baseDate.atTime(10, 0),
                baseDate.atTime(12, 0)
        );

        r14 = new ReservationFormDto(
                "User14",
                "tenA@example.com",
                10,
                baseDate.atTime(10, 0),
                baseDate.atTime(12, 0)
        );

        // бронирование, начинающееся завтра и заканчивающееся послезавтра
        r15 = new ReservationFormDto(
                "User15",
                "span@example.com",
                3,
                baseDate.atTime(18, 0),
                baseDate.plusDays(1).atTime(10, 0)
        );

        //Время начала позже времени окончания
        r16 = new ReservationFormDto(
                "User16", "u16@example.com", 2,
                baseDate.atTime(12, 0),
                baseDate.atTime(10, 0)
        );

    }

    @Test
    @DisplayName("Seave reservation with valid data")
    void saveReservationPositive() throws MessagingException {
        reservationService.createReservation(r1);
        reservationService.createReservation(r2);
        assertEquals(2, reservationRepo.findAll().size());
        List<Integer> capacities = reservationRepo.findAll().stream()
                .map(reservation -> reservation.getRestaurantTable()
                        .getCapacity()).toList();
        List<Integer> sorted = new ArrayList<>(capacities);
        sorted.sort(Comparator.naturalOrder());
        assertEquals(2, sorted.get(0));
        assertEquals(4, sorted.get(1));
    }

    @Test
    @DisplayName("Save reservation with valid data and overlap")
    void saveReservationWithTimeOverlapPositive() throws MessagingException {
        reservationService.createReservation(r1);
        reservationService.createReservation(r2);
        reservationService.createReservation(r3);
        reservationService.createReservation(r6);
        reservationService.createReservation(r7);
        assertEquals(5, reservationRepo.findAll().size());
        List<Integer> capacities = reservationRepo.findAll().stream().map(reservation -> reservation.getRestaurantTable().getCapacity()).toList();
        List<Integer> sorted = new ArrayList<>(capacities);
        sorted.sort(Comparator.naturalOrder());
        assertEquals(2, sorted.get(0));
        assertEquals(3, sorted.get(1));
        assertEquals(4, sorted.get(2));
        assertEquals(5, sorted.get(3));
        assertEquals(6, sorted.get(4));
    }


    @Test
    @DisplayName("Not enough tables test")
    void saveReservationWithNotEnoughTables() throws MessagingException {
        reservationService.createReservation(r12);
        reservationService.createReservation(r13);
        assertThrows(NoAvailableTableException.class, () -> reservationService.createReservation(r14));
        assertEquals(2, reservationRepo.findAll().size());
        List<Integer> capacities = reservationRepo.findAll().stream().map(reservation -> reservation.getRestaurantTable().getCapacity()).toList();
        List<Integer> sorted = new ArrayList<>(capacities);
        sorted.sort(Comparator.naturalOrder());
        assertEquals(9, sorted.get(0));
        assertEquals(10, sorted.get(1));
    }

    @Test
    @DisplayName("Boundary case")
    void saveReservationWithBoundaryTime() throws MessagingException {

        ReservationFormDto event1 = new ReservationFormDto(
                "UserToday1", "uto1@example.com", 2,
                baseDate.atTime(18, 0),
                baseDate.atTime(19, 0)
        );
        ReservationFormDto event2 = new ReservationFormDto(
                "UserToday2", "uto2@example.com", 2,
                baseDate.atTime(19, 0),
                baseDate.atTime(20, 0)
        );
        reservationService.createReservation(event1);
        reservationService.createReservation(event2);
        assertEquals(2, reservationRepo.findAll().size());
        List<Integer> capacities = reservationRepo.findAll().stream()
                .map(reservation -> reservation.getRestaurantTable().getCapacity()).toList();
        assertEquals(2, capacities.get(0));
        assertEquals(2, capacities.get(1));
    }


//    //FIX//
//    @Test
//    @DisplayName("Start tomorrow, end - aftertomorrow(during the night)")
//    void reservationMoreThanOneDay() {
//        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(r15));
//        assertEquals(0, reservationRepo.findAll().size());
//    }


    @Test
    @DisplayName("Reservation by the same user in one day")
    void reservationByTheSameUserInOneDay() throws MessagingException {
        // Первая бронь должна успешно сохраниться
        reservationService.createReservation(r1);

        // Попытка второй брони тем же email в тот же день — ожидаем исключение
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(r1sameUser),
                "Ожидалось IllegalArgumentException при дублирующей брони"
        );
        assertTrue(
                ex.getMessage().contains("already has a reservation"),
                "Сообщение об ошибке должно содержать информацию о существующей брони"
        );

        // В репозитории должна быть только первая бронь
        assertEquals(1, reservationRepo.findAll().size(),
                "В репозитории должна остаться только первая бронь"
        );}


    @Test
    @DisplayName("Reservation in the past")
    void reservationInThePast() {
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(r11));

    }

//    //FIX//
//    @Test
//    @DisplayName("Overlaps opening and closing time")
//    void reservationIsTooEarlyAndTooLate() {
//        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(r4));
//        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(r5));
//    }
//
//    //FIX//
//    @Test
//    @DisplayName("Overlaps opening and closing time")
//    void reservationAfterClosing() {
//        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(r10));
//        assertEquals(0, reservationRepo.findAll().size());
//    }

    @Test
    @DisplayName("Start time after end time")
    void reservationWhereStartTimeAfterEndTime() {
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(r16));
    }

    @Test
    @DisplayName("Negative is admin flag test")
    void isAdminFlagTestNegative() throws MessagingException {
        Reservation reservation = reservationService.createReservation(r1);
        assertFalse(reservation.isAdmin());
    }

    @Test
    @DisplayName("Is admin flag test")
    void reservationCreatedByAdmin() throws Exception {
        Authentication auth = new TestingAuthenticationToken("admin", "adminpass", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);
        Reservation reservation = reservationService.createReservation(r1);
        assertTrue(reservation.isAdmin());
    }

    @Test
    @DisplayName("Get all reservations test")
    void getAllReservationsTest() throws MessagingException {
        assertEquals(0, reservationService.getAllReservations().size());
        reservationService.createReservation(r1);
        reservationService.createReservation(r2);
        reservationService.createReservation(r3);
        assertEquals(3, reservationService.getAllReservations().size());
        List<Integer> capacities = reservationService.getAllReservations().stream()
                .map(reservation -> reservation.getRestaurantTable().getCapacity()).toList();
        List<Integer> sorted = new ArrayList<>(capacities);
        sorted.sort(Comparator.naturalOrder());
        assertEquals(2, sorted.get(0));
        assertEquals(4, sorted.get(1));
        assertEquals(6, sorted.get(2));
    }

//    @Test
//    @DisplayName("Get reservations for table today")
//    void getReservationsForTableToday () throws MessagingException {
//        LocalDate today = LocalDate.now();
//        ReservationFormDto today1 = new ReservationFormDto(
//                "UserToday1", "uto1@example.com", 2,
//                today.atTime(18, 0),
//                today.atTime(19, 0)
//        );
//        ReservationFormDto today2 = new ReservationFormDto(
//                "UserToday2", "uto2@example.com", 2,
//                today.atTime(19, 0),
//                today.atTime(20, 0)
//        );
//        int id1 = reservationService.createReservation(today1).getRestaurantTable().getId();
//        int id2 = reservationService.createReservation(today2).getRestaurantTable().getId();
//        assertEquals(id1, id2);
//        assertEquals(2, reservationService.getReservationsForTableToday(id1).size());
//    }

    @Test
    @DisplayName("Cancel reservation test")
    void cancelReservationTest() throws MessagingException {
        Reservation reservation1 = reservationService.createReservation(r1);
        Reservation reservation2 = reservationService.createReservation(r9);
        assertEquals(2, reservationRepo.findAll().size());
        String codeToDelete = reservation1.getReservationCode();
        String codeToSave = reservation2.getReservationCode();
        reservationService.cancelReservation(codeToDelete);
        List<ReservationStatus> statusList = reservationRepo.findAll().stream()
                .map(reservation -> reservation.getReservationStatus())
                .filter(status -> status.equals(ReservationStatus.CANCELED)).toList();
        assertEquals(1, statusList.size());
        assertEquals(ReservationStatus.CANCELED, statusList.get(0));
        assertEquals(codeToSave, reservationRepo.findAll().stream()
                .filter(reservation -> reservation.getReservationStatus() == ReservationStatus.CONFIRMED)
                .toList().get(0).getReservationCode());
    }
}
