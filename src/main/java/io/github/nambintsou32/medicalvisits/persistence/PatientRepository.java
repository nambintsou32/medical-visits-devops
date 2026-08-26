package io.github.nambintsou32.medicalvisits.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.nambintsou32.medicalvisits.domain.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public final class PatientRepository {

    private final EntityManagerFactory entityManagerFactory;

    public PatientRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(
            entityManagerFactory,
            "entityManagerFactory"
        );
    }

    public Patient create(Patient patient) {
        Objects.requireNonNull(patient, "patient");

        if (patient.getId() != null) {
            throw new IllegalArgumentException(
                "A new patient must not already have an identifier"
            );
        }

        return executeInTransaction(entityManager -> {
            entityManager.persist(patient);
            entityManager.flush();
            return patient;
        });
    }

    public Optional<Patient> findById(Long id) {
        Objects.requireNonNull(id, "id");

        return executeInTransaction(entityManager ->
            Optional.ofNullable(entityManager.find(Patient.class, id))
        );
    }

    public List<Patient> findAll() {
        return executeInTransaction(entityManager ->
            entityManager.createQuery(
                "select patient from Patient patient order by patient.id",
                Patient.class
            ).getResultList()
        );
    }

    public Patient update(Patient patient) {
        Objects.requireNonNull(patient, "patient");

        if (patient.getId() == null) {
            throw new IllegalArgumentException(
                "An existing patient must have an identifier"
            );
        }

        return executeInTransaction(entityManager -> {
            Patient updatedPatient = entityManager.merge(patient);
            entityManager.flush();
            return updatedPatient;
        });
    }

    public boolean deleteById(Long id) {
        Objects.requireNonNull(id, "id");

        return executeInTransaction(entityManager -> {
            Patient patient = entityManager.find(Patient.class, id);

            if (patient == null) {
                return false;
            }

            entityManager.remove(patient);
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
}