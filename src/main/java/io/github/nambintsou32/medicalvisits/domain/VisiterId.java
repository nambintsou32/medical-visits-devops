package io.github.nambintsou32.medicalvisits.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VisiterId implements Serializable {

    @Column(name = "code_med", nullable = false, length = 30)
    private String codeMed;

    @Column(name = "code_pat", nullable = false, length = 30)
    private String codePat;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    protected VisiterId() {
        // Required by JPA.
    }

    public VisiterId(
            String codeMed,
            String codePat,
            LocalDate visitDate
    ) {
        this.codeMed = Objects.requireNonNull(codeMed, "codeMed");
        this.codePat = Objects.requireNonNull(codePat, "codePat");
        this.visitDate = Objects.requireNonNull(visitDate, "visitDate");
    }

    public String getCodeMed() {
        return codeMed;
    }

    public String getCodePat() {
        return codePat;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof VisiterId)) {
            return false;
        }

        VisiterId that = (VisiterId) other;

        return codeMed.equals(that.codeMed)
            && codePat.equals(that.codePat)
            && visitDate.equals(that.visitDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeMed, codePat, visitDate);
    }
}
