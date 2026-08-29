package io.github.nambintsou32.medicalvisits.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medecins")
public class Medecin {

    @Id
    @Column(name = "code_med", nullable = false, length = 30, updatable = false)
    private String codeMed;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "grade", nullable = false, length = 100)
    private String grade;

    protected Medecin() {
        // Required by JPA.
    }

    public Medecin(String codeMed, String nom, String prenom, String grade) {
        this.codeMed = requireText(codeMed, "codeMed");
        this.nom = requireText(nom, "nom");
        this.prenom = requireText(prenom, "prenom");
        this.grade = requireText(grade, "grade");
    }

    public String getCodeMed() {
        return codeMed;
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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = requireText(grade, "grade");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.trim();
    }
}
