package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SalleServiceImpl implements SalleService {

    private final SalleRepository salleRepository;

    public SalleServiceImpl(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Intervalle invalide (dateFin doit être après dateDebut)");
        }
        return salleRepository.findAvailable(start, end);
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteres) {
        return salleRepository.search(criteres);
    }

    @Override
    public List<Salle> getPaginatedRooms(int page, int pageSize) {
        return salleRepository.findPaginated(page, pageSize);
    }

    @Override
    public int getTotalPages(int pageSize) {
        long total = salleRepository.count();
        return (int) Math.ceil((double) total / pageSize);
    }

    @Override
    public long countRooms() {
        return salleRepository.count();
    }
}