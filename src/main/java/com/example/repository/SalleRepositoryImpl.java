package com.example.repository;

import com.example.model.Salle;
import com.example.model.StatutReservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SalleRepositoryImpl implements SalleRepository {

    private final EntityManagerFactory emf;

    public SalleRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Salle> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Salle.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Salle> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Salle> findPaginated(int page, int pageSize) {
        int first = Math.max(0, (page - 1) * pageSize);
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                    .setFirstResult(first)
                    .setMaxResults(pageSize)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Salle> findAvailable(LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT s FROM Salle s " +
                                    "WHERE s.id NOT IN (" +
                                    "   SELECT r.salle.id FROM Reservation r " +
                                    "   WHERE r.statut = :statut " +
                                    "   AND (r.dateDebut < :end AND r.dateFin > :start)" +
                                    ") " +
                                    "ORDER BY s.id",
                            Salle.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Salle> search(Map<String, Object> criteres) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT s FROM Salle s ");
            boolean joinEquip = criteres.containsKey("equipement") && criteres.get("equipement") != null;
            if (joinEquip) jpql.append("JOIN s.equipements e ");

            jpql.append("WHERE 1=1 ");

            if (criteres.get("capaciteMin") != null) jpql.append("AND s.capacite >= :capMin ");
            if (criteres.get("capaciteMax") != null) jpql.append("AND s.capacite <= :capMax ");
            if (criteres.get("batiment") != null) jpql.append("AND s.batiment = :batiment ");
            if (criteres.get("etage") != null) jpql.append("AND s.etage = :etage ");
            if (joinEquip) jpql.append("AND e.id = :equipId ");

            jpql.append("ORDER BY s.id");

            TypedQuery<Salle> q = em.createQuery(jpql.toString(), Salle.class);

            if (criteres.get("capaciteMin") != null) q.setParameter("capMin", (Integer) criteres.get("capaciteMin"));
            if (criteres.get("capaciteMax") != null) q.setParameter("capMax", (Integer) criteres.get("capaciteMax"));
            if (criteres.get("batiment") != null) q.setParameter("batiment", (String) criteres.get("batiment"));
            if (criteres.get("etage") != null) q.setParameter("etage", (Integer) criteres.get("etage"));
            if (joinEquip) q.setParameter("equipId", (Long) criteres.get("equipement"));

            return q.getResultList();
        } finally {
            em.close();
        }
    }
}