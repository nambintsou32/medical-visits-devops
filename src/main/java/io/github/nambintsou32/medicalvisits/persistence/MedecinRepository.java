package io.github.nambintsou32.medicalvisits.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.nambintsou32.medicalvisits.domain.Medecin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public final class MedecinRepository {

    private final EntityManagerFactory entityManagerFactory;

    public MedecinRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(
            entityManagerFactory,
            "entityManagerFactory"
        );
    }

    public Medecin create(Medecin medecin) {
        Objects.requireNonNull(medecin, "medecin");

        return executeInTransaction(entityManager -> {
            entityManager.persist(medecin);
            entityManager.flush();
            return medecin;
        });
    }

    public Optional<Medecin> findByCode(String codeMed) {
        Objects.requireNonNull(codeMed, "codeMed");

        return executeInTransaction(entityManager ->
            Optional.ofNullable(entityManager.find(Medecin.class, codeMed.trim()))
        );
    }

    public List<Medecin> findAll() {
        return executeInTransaction(entityManager ->
            entityManager.createQuery(
                "select medecin from Medecin medecin "
                    + "order by medecin.nom, medecin.prenom, medecin.codeMed",
                Medecin.class
            ).getResultList()
        );
    }

    public Medecin update(Medecin medecin) {
        Objects.requireNonNull(medecin, "medecin");

        return executeInTransaction(entityManager -> {
            Medecin updatedMedecin = entityManager.merge(medecin);
            entityManager.flush();
            return updatedMedecin;
        });
    }

    public boolean deleteByCode(String codeMed) {
        Objects.requireNonNull(codeMed, "codeMed");

        return executeInTransaction(entityManager -> {
            Medecin medecin = entityManager.find(Medecin.class, codeMed.trim());

            if (medecin == null) {
                return false;
            }

            entityManager.remove(medecin);
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
