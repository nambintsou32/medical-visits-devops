package io.github.nambintsou32.medicalvisits.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.nambintsou32.medicalvisits.domain.Medecin;
import io.github.nambintsou32.medicalvisits.domain.Patient;
import io.github.nambintsou32.medicalvisits.domain.Visiter;
import io.github.nambintsou32.medicalvisits.domain.VisiterId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public final class VisiterRepository {

    private final EntityManagerFactory entityManagerFactory;

    public VisiterRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(
            entityManagerFactory,
            "entityManagerFactory"
        );
    }

    public Visiter create(
            String codeMed,
            String codePat,
            java.time.LocalDate visitDate
    ) {
        return executeInTransaction(entityManager -> {
            Medecin medecin = entityManager.getReference(
                Medecin.class,
                requireText(codeMed, "codeMed")
            );
            Patient patient = entityManager.getReference(
                Patient.class,
                requireText(codePat, "codePat")
            );

            Visiter visiter = new Visiter(medecin, patient, visitDate);
            entityManager.persist(visiter);
            entityManager.flush();
            return visiter;
        });
    }

    public Optional<Visiter> findById(VisiterId id) {
        Objects.requireNonNull(id, "id");

        return executeInTransaction(entityManager ->
            Optional.ofNullable(entityManager.find(Visiter.class, id))
        );
    }

    public List<Visiter> findAll() {
        return executeInTransaction(entityManager ->
            entityManager.createQuery(
                """
                select visiter
                from Visiter visiter
                join fetch visiter.medecin
                join fetch visiter.patient
                order by visiter.id.visitDate desc,
                         visiter.id.codeMed,
                         visiter.id.codePat
                """,
                Visiter.class
            ).getResultList()
        );
    }

    public Visiter replace(
            VisiterId currentId,
            String newCodeMed,
            String newCodePat,
            java.time.LocalDate newVisitDate
    ) {
        Objects.requireNonNull(currentId, "currentId");

        return executeInTransaction(entityManager -> {
            Visiter existing = entityManager.find(Visiter.class, currentId);

            if (existing == null) {
                throw new IllegalArgumentException("Visit does not exist");
            }

            entityManager.remove(existing);
            entityManager.flush();

            Medecin medecin = entityManager.getReference(
                Medecin.class,
                requireText(newCodeMed, "newCodeMed")
            );
            Patient patient = entityManager.getReference(
                Patient.class,
                requireText(newCodePat, "newCodePat")
            );

            Visiter replacement = new Visiter(
                medecin,
                patient,
                newVisitDate
            );
            entityManager.persist(replacement);
            entityManager.flush();
            return replacement;
        });
    }

    public boolean deleteById(VisiterId id) {
        Objects.requireNonNull(id, "id");

        return executeInTransaction(entityManager -> {
            Visiter visiter = entityManager.find(Visiter.class, id);

            if (visiter == null) {
                return false;
            }

            entityManager.remove(visiter);
            entityManager.flush();
            return true;
        });
    }

    private <T> T executeInTransaction(
            Function<EntityManager, T> operation
    ) {
        EntityManager entityManager =
            entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            T result = operation.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.trim();
    }
}
