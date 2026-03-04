package com.example.service;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.StatutReservation;
import com.example.model.Utilisateur;
import com.example.repository.ReservationRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(EntityManagerFactory emf, ReservationRepository reservationRepository) {
        this.emf = emf;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation createReservation(Long utilisateurId, Long salleId, LocalDateTime start, LocalDateTime end, String motif) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Intervalle invalide (dateFin doit être après dateDebut)");
        }

        // Vérifier disponibilité
        if (!reservationRepository.findBySalleAndPeriod(salleId, start, end).isEmpty()) {
            throw new IllegalStateException("Salle non disponible sur ce créneau");
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Utilisateur u = em.find(Utilisateur.class, utilisateurId);
            Salle s = em.find(Salle.class, salleId);

            if (u == null) throw new IllegalArgumentException("Utilisateur introuvable: " + utilisateurId);
            if (s == null) throw new IllegalArgumentException("Salle introuvable: " + salleId);

            Reservation r = new Reservation(start, end, motif);
            r.setStatut(StatutReservation.CONFIRMEE);

            // relations
            r.setUtilisateur(u);
            r.setSalle(s);

            em.persist(r);
            em.getTransaction().commit();
            return r;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}