package com.example;

import com.example.repository.*;
import com.example.service.*;
import com.example.test.TestScenarios;
import com.example.util.DataInitializer;
import com.example.util.DatabaseMigrationTool;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        System.out.println("=== APPLICATION DE RÉSERVATION DE SALLES ===");
        System.out.println("=== Faculté des Sciences et Techniques - Marrakech ===");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations");

        try {
            SalleRepository salleRepository = new SalleRepositoryImpl(emf);
            SalleService salleService = new SalleServiceImpl(salleRepository);

            ReservationRepository reservationRepository = new ReservationRepositoryImpl(emf);
            ReservationService reservationService = new ReservationServiceImpl(emf, reservationRepository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n=== MENU PRINCIPAL ===");
                System.out.println("1. Initialiser les données de test");
                System.out.println("2. Exécuter les scénarios de test");
                System.out.println("3. Exécuter le script de migration");
                System.out.println("4. Générer un rapport de performance");
                System.out.println("5. Quitter");
                System.out.print("Votre choix: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        new DataInitializer(emf).initializeData();
                        break;

                    case 2:
                        new TestScenarios(emf, salleService, reservationService).runAllTests();
                        break;

                    case 3:
                        System.out.println("Cette fonctionnalité nécessite une base de données externe.");
                        System.out.print("Voulez-vous continuer avec une simulation? (o/n): ");
                        String confirm = scanner.nextLine();
                        if (confirm.equalsIgnoreCase("o")) {
                            DatabaseMigrationTool tool = new DatabaseMigrationTool(
                                    "jdbc:mysql://localhost:3306/tp_capstone_db?useSSL=false&serverTimezone=UTC",
                                    "root",
                                    ""
                            );
                            tool.executeMigration();
                        }
                        break;

                    case 4:
                        new PerformanceReport(emf).runPerformanceTests();
                        break;

                    case 5:
                        exit = true;
                        System.out.println("Au revoir !");
                        break;

                    default:
                        System.out.println("Choix invalide. Veuillez réessayer.");
                }
            }

        } finally {
            emf.close();
        }
    }
}