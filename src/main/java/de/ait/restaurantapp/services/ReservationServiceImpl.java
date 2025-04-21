package de.ait.restaurantapp.services;

import de.ait.restaurantapp.dto.EmailDto;
import de.ait.restaurantapp.dto.ReservationFormDto;
import de.ait.restaurantapp.enums.ReservationStatus;
import de.ait.restaurantapp.exception.NoAvailableTableException;
import de.ait.restaurantapp.model.Reservation;
import de.ait.restaurantapp.model.RestaurantTable;
import de.ait.restaurantapp.repositories.ReservationRepo;
import de.ait.restaurantapp.repositories.RestaurantTableRepo;
import de.ait.restaurantapp.utils.ReservationIDGenerator;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для управления резервированием столиков в ресторане.
 * Основные функции:
 * - создание нового резервирования
 * - получение списка всех резервирований
 * - отмена существующего резервирования
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepo reservationRepo;
    private final RestaurantTableRepo tableRepository;
    private final EmailService emailService;

    /**
     * Создает новое резервирование на основе данных из формы.
     * Добавлена валидация входных данных, установка флага isAdmin,
     * проверка «не более одной брони в день на один email» (исправлено)
     * и более точные исключения.
     */
    @Override
    @Transactional
    public Reservation createReservation(ReservationFormDto form) throws MessagingException {
        // --- Валидация входных параметров ---
        Objects.requireNonNull(form, "ReservationFormDto must not be null");
        LocalDateTime startDateTime = Objects.requireNonNull(form.getStartDateTime(), "startDateTime must not be null");
        LocalDateTime endDateTime   = Objects.requireNonNull(form.getEndDateTime(),   "endDateTime must not be null");

        if (!startDateTime.isBefore(endDateTime)) {
            throw new IllegalArgumentException("Start date/time must be before end date/time");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date/time must be in the future");
        }

        // --- Ограничение: не более одной резервации в один день на один email ---
        LocalDate reservationDate = startDateTime.toLocalDate();
        boolean alreadyHasBooking = reservationRepo.findAll().stream()
                .filter(r -> r.getCustomerEmail().equals(form.getCustomerEmail()))
                .anyMatch(r -> r.getStartDateTime().toLocalDate().isEqual(reservationDate));
        if (alreadyHasBooking) {
            throw new IllegalArgumentException(
                    "Email " + form.getCustomerEmail() +
                            " already has a reservation on " + reservationDate
            );
        }
        // ---------------------------------------------------------------

        // --- Ограничение по времени работы ресторана: только с 08:00 до 20:00 ---
        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(20, 0);
        LocalTime startTime   = startDateTime.toLocalTime();
        LocalTime endTime     = endDateTime.toLocalTime();
        if (startTime.isBefore(openingTime) || endTime.isAfter(closingTime)) {
            throw new IllegalArgumentException(
                    "Reservations are only allowed between " +
                            openingTime + " and " + closingTime
            );
        }

        // --- Определяем, создал ли текущий пользователь как админ ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // --- Поиск подходящих столиков по вместимости ---
        List<RestaurantTable> availableTables = tableRepository.findAll().stream()
                .filter(t -> t.getCapacity() >= form.getGuestNumber())
                .sorted(Comparator.comparingInt(RestaurantTable::getCapacity))
                .toList();

        // --- Проверка наличия свободного столика по времени ---
        for (RestaurantTable table : availableTables) {
            boolean hasConflict = reservationRepo
                    .findByRestaurantTable_IdAndReservationStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                            table.getId(),
                            ReservationStatus.CONFIRMED,
                            endDateTime,
                            startDateTime
                    )
                    .stream()
                    .findAny()
                    .isPresent();

            if (!hasConflict) {
                // Генерация кода и создание сущности с указанием isAdmin
                String reservationCode = ReservationIDGenerator.generateReservationId();
                Reservation reservation = Reservation.builder()
                        .reservationCode(reservationCode)
                        .customerName(form.getCustomerName())
                        .customerEmail(form.getCustomerEmail())
                        .guestCount(form.getGuestNumber())
                        .startDateTime(startDateTime)
                        .endDateTime(endDateTime)
                        .restaurantTable(table)
                        .reservationStatus(ReservationStatus.CONFIRMED)
                        .isAdmin(isAdmin)
                        .build();

                // Сохраняем в БД до отправки письма
                Reservation saved = reservationRepo.save(reservation);

                // Отправка email-подтверждения
                EmailDto emailClientDto = new EmailDto();
                emailClientDto.setTo(saved.getCustomerEmail());
                emailClientDto.setName(saved.getCustomerName());
                emailClientDto.setReservationCode(reservationCode);
                emailService.sendHTMLEmail(emailClientDto);

                return saved;
            }
        }

        // Бросаем специальное исключение при отсутствии свободных столиков
        throw new NoAvailableTableException(
                "No available tables for the specified time and guest count"
        );
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForTableToday(Integer tableId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay     = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        return reservationRepo
                .findByRestaurantTable_IdAndStartDateTimeGreaterThanEqualAndStartDateTimeLessThan(
                        tableId,
                        startOfDay,
                        startOfNextDay
                );
    }

    @Override
    public boolean cancelReservation(String reservationCode) {
        return reservationRepo.findByReservationCode(reservationCode)
                .map(reservation -> {
                    reservation.setReservationStatus(ReservationStatus.CANCELED);
                    reservationRepo.save(reservation);
                    return true;
                })
                .orElse(false);
    }
}
