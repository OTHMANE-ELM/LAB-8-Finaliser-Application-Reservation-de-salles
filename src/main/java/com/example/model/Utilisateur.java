package com.example.model;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "utilisateurs")
@Cacheable
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "departement")
    private String departement;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @Version
    private Long version;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }


    // Méthodes utilitaires (important)
    public void addReservation(Reservation r) {
        reservations.add(r);
        r.setUtilisateur(this);
    }

    public void removeReservation(Reservation r) {
        reservations.remove(r);
        r.setUtilisateur(null);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    public List<Reservation> getReservations() { return reservations; }
}