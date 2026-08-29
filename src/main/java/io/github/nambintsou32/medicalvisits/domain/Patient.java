package io.github.nambintsou32.medicalvisits.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @Column(name = "code_pat", nullable = false, length = 30, updatable = false)
    private String codePat;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe", nullable = false, length = 20)
    private Sexe sexe;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    protected Patient() {
        // Required by JPA.
    }

    public Patient(
            String codePat,
            String nom,
            String prenom,
            Sexe sexe,
            String adresse
    ) {
        this.codePat = requireText(codePat, "codePat");
        this.nom = requireText(nom, "nom");
        this.prenom = requireText(prenom, "prenom");
        this.sexe = Objects.requireNonNull(sexe, "sexe");
        this.adresse = requireText(adresse, "adresse");
    }

    public String getCodePat() {
        return codePat;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = requireText(nom, "nom");
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = requireText(prenom, "prenom");
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = Objects.requireNonNull(sexe, "sexe");
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = requireText(adresse, "adresse");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.trim();
    }
}
