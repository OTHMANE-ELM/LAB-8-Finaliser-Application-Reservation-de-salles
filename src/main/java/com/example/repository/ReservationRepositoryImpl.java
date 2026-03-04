package com.example.repository;

import com.example.model.Reservation;
import com.example.model.StatutReservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationRepositoryImpl implements ReservationRepository {

    private final EntityManagerFactory emf;

    public ReservationRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Reservation.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation update(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation merged = em.merge(reservation);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation r = em.find(Reservation.class, id);
            if (r != null) em.remove(r);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> findBySalleAndPeriod(Long salleId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r " +
                                    "WHERE r.salle.id = :salleId " +
                                    "AND r.statut = :statut " +
                                    "AND (r.dateDebut < :end AND r.dateFin > :start)",
                            Reservation.class)
                    .setParameter("salleId", salleId)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}