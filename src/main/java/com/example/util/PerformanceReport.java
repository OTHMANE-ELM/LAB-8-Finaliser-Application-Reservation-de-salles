package com.example.util;

import com.example.model.Salle;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PerformanceReport {

    private final EntityManagerFactory EMFACTORY;
    private final Map<String, TestResult> results = new HashMap<>();

    public PerformanceReport(EntityManagerFactory emf) {
        this.EMFACTORY = emf;
    }

    public void runPerformanceTests() {
        System.out.println("=== FST MARRAKECH — RAPPORT DE PERFORMANCE JPA/HIBERNATE ===");
        System.out.println("Démarrage des tests...");

        resetStatistics();

        testPerformance("Recherche de salles disponibles", () -> {
            EntityManager em = EMFACTORY.createEntityManager();
            try {
                LocalDateTime start = LocalDateTime.now().plusDays(1);
                LocalDateTime end = start.plusHours(2);
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN " +
                                        "(SELECT r.salle.id FROM Reservation r " +
                                        "WHERE (r.dateDebut <= :end AND r.dateFin >= :start))")
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        testPerformance("Recherche multi-critères", () -> {
            EntityManager em = EMFACTORY.createEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s JOIN s.equipements e " +
                                        "WHERE s.capacite >= :capacite AND s.batiment = :batiment AND e.id = :equipementId")
                        .setParameter("capacite", 30)
                        .setParameter("batiment", "Bloc B")
                        .setParameter("equipementId", 1L)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        testPerformance("Pagination", () -> {
            EntityManager em = EMFACTORY.createEntityManager();
            try {
                return em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                        .setFirstResult(0)
                        .setMaxResults(10)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        testPerformance("Accès répété avec cache", () -> {
            Object result = null;
            for (int i = 0; i < 100; i++) {
                EntityManager em = EMFACTORY.createEntityManager();
                try {
                    result = em.find(Salle.class, 1L);
                } finally {
                    em.close();
                }
            }
            return result;
        });

        testPerformance("Requête avec JOIN FETCH", () -> {
            EntityManager em = EMFACTORY.createEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s LEFT JOIN FETCH s.equipements WHERE s.capacite > 20",
                                Object.class)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        generateReport();
    }

    private void testPerformance(String testName, Supplier<?> testFunction) {
        System.out.println("  -> Test en cours : " + testName);

        resetStatistics();

        long startTime = System.currentTimeMillis();
        Object result = testFunction.get();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        Session session = EMFACTORY.createEntityManager().unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();

        TestResult testResult = new TestResult();
        testResult.executionTime = executionTime;
        testResult.queryCount = stats.getQueryExecutionCount();
        testResult.entityLoadCount = stats.getEntityLoadCount();
        testResult.cacheHitCount = stats.getSecondLevelCacheHitCount();
        testResult.cacheMissCount = stats.getSecondLevelCacheMissCount();
        testResult.resultSize = (result instanceof java.util.Collection) ?
                ((java.util.Collection<?>) result).size() : (result != null ? 1 : 0);

        results.put(testName, testResult);

        System.out.println("     Terminé en " + executionTime + " ms");
    }

    private void resetStatistics() {
        Session session = EMFACTORY.createEntityManager().unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.clear();
    }

    private void generateReport() {
        System.out.println("\nGénération du rapport en cours...");

        String fileName = "rapport_performance_FST_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            writer.println("================================================================");
            writer.println("   UNIVERSITÉ CADI AYYAD — MARRAKECH");
            writer.println("   Faculté des Sciences et Techniques");
            writer.println("   Département Informatique");
            writer.println("   TP CAPSTONE — JPA / HIBERNATE");
            writer.println("   Rapport d'Analyse des Performances v2.0");
            writer.println("================================================================");
            writer.println("Date d'exécution : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("================================================================\n");

            for (Map.Entry<String, TestResult> entry : results.entrySet()) {
                writer.println("Scénario testé : " + entry.getKey());
                writer.println("  Durée d'exécution        : " + entry.getValue().executionTime + " ms");
                writer.println("  Requêtes SQL émises       : " + entry.getValue().queryCount);
                writer.println("  Entités chargées          : " + entry.getValue().entityLoadCount);
                writer.println("  Accès réussis au cache    : " + entry.getValue().cacheHitCount);
                writer.println("  Accès manqués au cache    : " + entry.getValue().cacheMissCount);
                writer.println("  Volume des résultats      : " + entry.getValue().resultSize + " élément(s)");

                long totalCacheAccess = entry.getValue().cacheHitCount + entry.getValue().cacheMissCount;
                double cacheHitRatio = totalCacheAccess > 0 ?
                        (double) entry.getValue().cacheHitCount / totalCacheAccess : 0;
                writer.println("  Taux d'efficacité du cache : " + String.format("%.2f", cacheHitRatio * 100) + "%");
                writer.println("  ---------------------------------------------------------------\n");
            }

            writer.println("================================================================");
            writer.println("   ANALYSE ET RECOMMANDATIONS");
            writer.println("================================================================\n");

            boolean needsCacheOptimization = results.values().stream()
                    .anyMatch(r -> r.cacheHitCount < r.cacheMissCount && r.queryCount > 5);

            boolean needsQueryOptimization = results.values().stream()
                    .anyMatch(r -> r.queryCount > 10 || r.executionTime > 500);

            if (needsCacheOptimization) {
                writer.println("1. Amélioration du cache de second niveau :");
                writer.println("   - Le taux d'échec du cache est élevé, une révision de sa configuration s'impose.");
                writer.println("   - Il est conseillé d'ajuster les stratégies de mise en cache pour les entités");
                writer.println("     sollicitées fréquemment, telles que Salle et Equipement.");
                writer.println("   - L'activation du cache de requêtes pour les recherches répétitives");
                writer.println("     permettrait de réduire considérablement la charge sur la base de données.");
                writer.println();
            }

            if (needsQueryOptimization) {
                writer.println("2. Optimisation des requêtes JPQL :");
                writer.println("   - Certaines requêtes génèrent un nombre excessif d'appels SQL (problème N+1).");
                writer.println("     L'utilisation de JOIN FETCH est fortement recommandée.");
                writer.println("   - Des index supplémentaires sur les colonnes de filtrage (statut, dates)");
                writer.println("     contribueraient à améliorer les temps de réponse.");
                writer.println("   - Les requêtes dont la durée dépasse 500 ms méritent une attention particulière.");
                writer.println();
            }

            writer.println("3. Bonnes pratiques générales :");
            writer.println("   - Il est recommandé de surveiller régulièrement les performances");
            writer.println("     à l'aide d'outils spécialisés tels que JProfiler ou VisualVM.");
            writer.println("   - La mise en place d'un système de supervision en environnement de production");
            writer.println("     permettrait de détecter rapidement toute dégradation des performances.");
            writer.println("   - L'adoption d'un pool de connexions performant comme HikariCP");
            writer.println("     est vivement conseillée pour les déploiements à charge élevée.");
            writer.println();
            writer.println("================================================================");
            writer.println("   Fin du rapport — FST Marrakech — Département Informatique");
            writer.println("================================================================");

            System.out.println("Rapport généré avec succès : " + fileName);

        } catch (IOException e) {
            System.err.println("Une erreur est survenue lors de la génération du rapport : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class TestResult {
        long executionTime;
        long queryCount;
        long entityLoadCount;
        long cacheHitCount;
        long cacheMissCount;
        int resultSize;
    }
}