package com.example.repository;

import com.example.model.Salle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SalleRepository {
    Optional<Salle> findById(Long id);
    List<Salle> findAll();

    long count();

    List<Salle> findPaginated(int page, int pageSize);

    List<Salle> findAvailable(LocalDateTime start, LocalDateTime end);

    List<Salle> search(Map<String, Object> criteres);
}