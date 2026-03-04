package com.example.util;

import com.example.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Random;

public class DataInitializer {

    private final EntityManagerFactory emf;
    private final Random random = new Random();

    public DataInitializer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initializeData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // (Optionnel) si tu relances plusieurs fois : vider d'abord
            // clearAll(em);

            System.out.println("==> Création des équipements...");
            Equipement[] equipements = createEquipements(em);

            System.out.println("==> Création des utilisateurs...");
            Utilisateur[] utilisateurs = createUtilisateurs(em);

            System.out.println("==> Création des salles...");
            Salle[] salles = createSalles(em, equipements);

            System.out.println("==> Création des réservations...");
            createReservations(em, utilisateurs, salles);

            em.getTransaction().commit();
            System.out.println("succès !");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println(" Erreur");
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /* ------------------------------------------------------------
       1) EQUIPEMENTS
       ------------------------------------------------------------ */

    private Equipement[] createEquipements(EntityManager em) {
        Equipement[] equipements = new Equipement[10];

        equipements[0] = new Equipement("Projecteur HD", "Projecteur haute définition 4K — Amphis FST");
        equipements[0].setReference("FST-PROJ-001");

        equipements[1] = new Equipement("Écran interactif", "Écran tactile 65 pouces — Salles TP");
        equipements[1].setReference("FST-ECRAN-001");

        equipements[2] = new Equipement("Système de visioconférence", "Système Teams/Zoom — Salle de réunion");
        equipements[2].setReference("FST-VISIO-001");

        equipements[3] = new Equipement("Tableau blanc", "Tableau magnétique 2m x 1m — Salles TD");
        equipements[3].setReference("FST-TB-001");

        equipements[4] = new Equipement("Système audio", "Sonorisation 4 haut-parleurs — Amphis");
        equipements[4].setReference("FST-AUDIO-001");

        equipements[5] = new Equipement("Microphones sans fil", "Set 4 micros — Soutenances et conférences");
        equipements[5].setReference("FST-MIC-001");

        equipements[6] = new Equipement("Ordinateur fixe", "PC Windows 11 + suite Office — Salles TP Info");
        equipements[6].setReference("FST-PC-001");

        equipements[7] = new Equipement("WiFi haut débit", "WiFi 6 réseau UCA — 1 Gbps");
        equipements[7].setReference("FST-WIFI-001");

        equipements[8] = new Equipement("Climatisation", "Climatisation réversible — Salles équipées");
        equipements[8].setReference("FST-CLIM-001");

        equipements[9] = new Equipement("Prises électriques", "10 prises réparties — Salles TP");
        equipements[9].setReference("FST-PRISES-001");

        for (Equipement e : equipements) {
            em.persist(e);
        }

        return equipements;
    }

    /* ------------------------------------------------------------
       2) UTILISATEURS
       ------------------------------------------------------------ */

    private Utilisateur[] createUtilisateurs(EntityManager em) {
        Utilisateur[] utilisateurs = new Utilisateur[20];

        String[] noms = {
                "Matlini", "marbouh", "mahdi", "boukhriss", "bounaga",
                "janati", "jmjami", "khay", "sane", "mane",
                "Kettani", "Lahlou", "Mernissi", "Naciri", "Ouazzani",
                "Rahmani", "Sefrioui", "Tazi", "Wahbi", "Zniber"
        };

        String[] prenoms = {
                "Mohammed", "Fatima", "Othmane", "Malak", "Omar",
                "Aicha", "Abdo", "Zineb", "Khalid", "Meryem",
                "Ouadoud", "Nadia", "Oualid", "Samira", "Karim",
                "Rayan", "Amine", "Loubna", "Soufiane", "Imane"
        };

        String[] departements = {
                "Informatique", "Mathématiques", "Physique", "Chimie",
                "Biologie", "Géologie", "Electronique", "Génie Civil",
                "Administration", "Scolarité"
        };

        for (int i = 0; i < utilisateurs.length; i++) {
            String nom = noms[i];
            String prenom = prenoms[i];
            String email = prenom.toLowerCase() + "." + nom.toLowerCase() + "@uca.ac.ma";

            // Assure-toi d’avoir : new Utilisateur(String nom, String prenom, String email)
            Utilisateur u = new Utilisateur(nom, prenom, email);

            u.setTelephone("06" + (10000000 + random.nextInt(90000000)));
            u.setDepartement(departements[i % departements.length]);

            em.persist(u);
            utilisateurs[i] = u;
        }

        return utilisateurs;
    }

    /* ------------------------------------------------------------
       3) SALLES
       ------------------------------------------------------------ */

    private Salle[] createSalles(EntityManager em, Equipement[] equipements) {
        Salle[] salles = new Salle[15];

        // Assure-toi d’avoir : new Salle(String nom, Integer capacite)
        // + méthodes utilitaires addEquipement

        // Bâtiment A - Salles réunion standard (A1..A5)
        for (int i = 0; i < 5; i++) {
            Salle s = new Salle("Amphi A" + (i + 1), 10 + i * 2);
            s.setDescription("Amphi des cours");
            s.setBatiment("Bloc A");
            s.setEtage((i % 3) + 1);
            s.setNumero("A" + (i + 1));

            // équipements de base
            s.addEquipement(equipements[3]); // tableau blanc
            s.addEquipement(equipements[7]); // wifi
            s.addEquipement(equipements[9]); // prises

            // optionnels
            if (i % 2 == 0) s.addEquipement(equipements[0]); // projecteur
            if (i % 3 == 0) s.addEquipement(equipements[4]); // audio

            em.persist(s);
            salles[i] = s;
        }

        // Bâtiment B - Salles formation (B1..B5)
        for (int i = 5; i < 10; i++) {
            Salle s = new Salle("Salle 25" + (i - 4), 20 + (i - 5) * 5);
            s.setDescription("Salle de TD");
            s.setBatiment("Bloc X");
            s.setEtage((i % 4) + 1);
            s.setNumero("B" + (i - 4));

            s.addEquipement(equipements[0]); // projecteur
            s.addEquipement(equipements[3]); // tableau
            s.addEquipement(equipements[6]); // PC
            s.addEquipement(equipements[7]); // wifi
            s.addEquipement(equipements[9]); // prises

            if (i % 2 == 0) s.addEquipement(equipements[1]); // écran interactif

            em.persist(s);
            salles[i] = s;
        }

        // Bâtiment C - Salles conférence (C1..C5)
        for (int i = 10; i < 15; i++) {
            Salle s = new Salle("Salle I7" + (i - 9), 50 + (i - 10) * 20);
            s.setDescription("Salle Informatique");
            s.setBatiment("Bâtiment INFO");
            s.setEtage((i % 3) + 1);
            s.setNumero("C" + (i - 9));

            s.addEquipement(equipements[0]); // projecteur
            s.addEquipement(equipements[2]); // visio
            s.addEquipement(equipements[4]); // audio
            s.addEquipement(equipements[5]); // micros
            s.addEquipement(equipements[7]); // wifi
            s.addEquipement(equipements[8]); // clim
            s.addEquipement(equipements[9]); // prises

            em.persist(s);
            salles[i] = s;
        }

        return salles;
    }

    /* ------------------------------------------------------------
       4) RESERVATIONS (réaliste + évite chevauchement)
       ------------------------------------------------------------ */

    private void createReservations(EntityManager em, Utilisateur[] utilisateurs, Salle[] salles) {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        String[] motifs = {
                "Cours Magistral", "TD Informatique", "TP Base de données",
                "Soutenance de stage", "Réunion département", "Examen partiel",
                "Conférence scientifique", "Atelier de recherche",
                "Séminaire doctoral", "Conseil pédagogique", "Jury de thèse"
        };

        int created = 0;
        int target = 100;

        // On essaie jusqu’à créer 100 réservations valides
        // (on limite les essais pour éviter boucle infinie si trop de conflits)
        int maxAttempts = 2000;
        int attempts = 0;

        while (created < target && attempts < maxAttempts) {
            attempts++;

            int jourOffset = random.nextInt(90);         // sur 90 jours
            int heureDebut = 8 + random.nextInt(9);      // 8h..16h
            int duree = 1 + random.nextInt(3);           // 1..3h

            LocalDateTime dateDebut = now.plusDays(jourOffset).withHour(heureDebut);
            LocalDateTime dateFin = dateDebut.plusHours(duree);

            Utilisateur utilisateur = utilisateurs[random.nextInt(utilisateurs.length)];
            Salle salle = salles[random.nextInt(salles.length)];

            //  vérifier qu'il n'y a pas chevauchement sur la même salle
            if (hasConflict(em, salle.getId(), dateDebut, dateFin)) {
                continue;
            }

            // Assure-toi d’avoir : new Reservation(LocalDateTime debut, LocalDateTime fin, String motif)
            Reservation r = new Reservation(dateDebut, dateFin, motifs[random.nextInt(motifs.length)]);

            // Statut : 80% confirmées, 10% en attente, 10% annulées
            int statutRandom = random.nextInt(10);
            if (statutRandom < 8) r.setStatut(StatutReservation.CONFIRMEE);
            else if (statutRandom < 9) r.setStatut(StatutReservation.EN_ATTENTE);
            else r.setStatut(StatutReservation.ANNULEE);

            //  relations (utilitaires)
            utilisateur.addReservation(r);
            salle.addReservation(r);

            em.persist(r);
            created++;
        }

        System.out.println("Réservations bien créées: " + created + " (tentatives: " + attempts + ")");

        if (created < target) {
            System.out.println(" Pas pu atteindre " + target + " réservations à cause des conflits (normal si planning chargé).");
        }
    }


    private boolean hasConflict(EntityManager em, Long salleId, LocalDateTime debut, LocalDateTime fin) {
        TypedQuery<Long> q = em.createQuery(
                "select count(r) " +
                        "from Reservation r " +
                        "where r.salle.id = :salleId " +
                        "and r.statut <> :annulee " +
                        "and r.dateDebut < :fin " +
                        "and r.dateFin > :debut",
                Long.class
        );

        Long count = q.setParameter("salleId", salleId)
                .setParameter("annulee", StatutReservation.ANNULEE)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getSingleResult();

        return count != null && count > 0;
    }

    /* ------------------------------------------------------------
       (Optionnel) Clear DB si tu veux rerun
       ------------------------------------------------------------ */

    @SuppressWarnings("unused")
    private void clearAll(EntityManager em) {
        // Ordre important à cause des FK
        em.createQuery("delete from Reservation").executeUpdate();
        em.createQuery("delete from Salle").executeUpdate();
        em.createQuery("delete from Utilisateur").executeUpdate();
        em.createQuery("delete from Equipement").executeUpdate();
    }
}