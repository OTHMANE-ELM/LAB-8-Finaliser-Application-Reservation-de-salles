package com.example.repository;

import com.example.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Optional<Reservation> findById(Long id);
    Reservation save(Reservation reservation);
    Reservation update(Reservation reservation);
    void delete(Long id);

    List<Reservation> findBySalleAndPeriod(Long salleId, LocalDateTime start, LocalDateTime end);
}