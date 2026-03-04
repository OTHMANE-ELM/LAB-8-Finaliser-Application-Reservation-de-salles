package com.example.service;

import com.example.model.Reservation;

import java.time.LocalDateTime;

public interface ReservationService {
    Reservation createReservation(Long utilisateurId, Long salleId,
                                  LocalDateTime start, LocalDateTime end, String motif);
}