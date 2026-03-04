# LAB-8-Finaliser-Application-Reservation-de-salles

# Réservation de Salles — JPA/Hibernate

TP Capstone — FST Marrakech, Université Cadi Ayyad  
Département Informatique — Architectures Réparties — S6

---

## Technologies

- Java 11, Hibernate 5.6.5, JPA 2.2, MySQL 8, EhCache, Maven

---

## Installation

**1. Créer la base de données**
```sql
CREATE DATABASE tp_capstone_db CHARACTER SET utf8mb4;
```
Puis exécuter `migration_v2.sql`.

**2. Configurer `persistence.xml`**
```xml
<property name="javax.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/tp_capstone_db?useSSL=false&amp;serverTimezone=UTC"/>
<property name="javax.persistence.jdbc.user"     value="root"/>
<property name="javax.persistence.jdbc.password" value=""/>
```

**3. Lancer**
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.example.App"
```

---

## Menu

| Choix | Action |
|---|---|
| 1 | Initialiser les données (salles, utilisateurs, réservations) |
| 2 | Exécuter les scénarios de test |
| 3 | Lancer la migration BDD |
| 4 | Générer le rapport de performance |
| 5 | Quitter |

---

## Vidéo de démonstration


https://github.com/user-attachments/assets/5736f05c-e842-4ecb-9e7b-1fc7e55c44e3



---

**Auteur** : Othmane EL MATLINI — FST Marrakech 2025/2026
