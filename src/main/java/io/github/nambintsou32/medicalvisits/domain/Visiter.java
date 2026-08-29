package io.github.nambintsou32.medicalvisits.domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "visiter")
public class Visiter {

    @EmbeddedId
    private VisiterId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("codeMed")
    @JoinColumn(name = "code_med", nullable = false)
    private Medecin medecin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("codePat")
    @JoinColumn(name = "code_pat", nullable = false)
    private Patient patient;

    protected Visiter() {
        // Required by JPA.
    }

    public Visiter(
            Medecin medecin,
            Patient patient,
            LocalDate visitDate
    ) {
        this.medecin = Objects.requireNonNull(medecin, "medecin");
        this.patient = Objects.requireNonNull(patient, "patient");

        this.id = new VisiterId(
            medecin.getCodeMed(),
            patient.getCodePat(),
            Objects.requireNonNull(visitDate, "visitDate")
        );
    }

    public VisiterId getId() {
        return id;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public Patient getPatient() {
        return patient;
    }

    public LocalDate getVisitDate() {
        return id.getVisitDate();
    }
}
