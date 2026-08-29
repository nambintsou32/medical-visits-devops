package io.github.nambintsou32.medicalvisits.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import io.github.nambintsou32.medicalvisits.domain.Medecin;
import io.github.nambintsou32.medicalvisits.domain.Patient;
import io.github.nambintsou32.medicalvisits.domain.Sexe;
import io.github.nambintsou32.medicalvisits.domain.Visiter;
import io.github.nambintsou32.medicalvisits.domain.VisiterId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@Testcontainers
class PatientRepositoryIT {

    @Container
    private static final MySQLContainer MYSQL =
            new MySQLContainer("mysql:8.4.11")
                    .withDatabaseName("medical_visits")
                    .withUsername("medical_visits")
                    .withPassword("integration-test-password");

    private static EntityManagerFactory entityManagerFactory;
    private static PatientRepository patientRepository;
    private static MedecinRepository medecinRepository;
    private static VisiterRepository visiterRepository;

    @BeforeAll
    static void createPersistenceContext() {
        DatabaseMigrator.migrate(
                MYSQL.getJdbcUrl(),
                MYSQL.getUsername(),
                MYSQL.getPassword());

        Map<String, Object> properties = Map.of(
                "jakarta.persistence.jdbc.driver",
                MYSQL.getDriverClassName(),
                "jakarta.persistence.jdbc.url",
                MYSQL.getJdbcUrl(),
                "jakarta.persistence.jdbc.user",
                MYSQL.getUsername(),
                "jakarta.persistence.jdbc.password",
                MYSQL.getPassword(),
                "hibernate.hbm2ddl.auto",
                "validate",
                "hibernate.show_sql",
                "false");

        entityManagerFactory = EntityManagerFactoryProvider.create(properties);
        patientRepository = new PatientRepository(entityManagerFactory);
        medecinRepository = new MedecinRepository(entityManagerFactory);
        visiterRepository = new VisiterRepository(entityManagerFactory);
    }

    @BeforeEach
    void deleteExistingData() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.createQuery("delete from Visiter").executeUpdate();
            entityManager.createQuery("delete from Medecin").executeUpdate();
            entityManager.createQuery("delete from Patient").executeUpdate();
            transaction.commit();
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw exception;
        } finally {
            entityManager.close();
        }
    }

    @AfterAll
    static void closePersistenceContext() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Test
    void flywayAppliesBothMigrationsExactlyOnce() {
        MigrateResult secondMigrationResult = DatabaseMigrator.migrate(
                MYSQL.getJdbcUrl(),
                MYSQL.getUsername(),
                MYSQL.getPassword());

        assertEquals(0, secondMigrationResult.migrationsExecuted);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            Object rawResult = entityManager.createNativeQuery(
                    """
                    SELECT COUNT(*)
                    FROM flyway_schema_history
                    WHERE version IN ('1', '2')
                      AND success = TRUE
                    """).getSingleResult();

            assertEquals(2L, ((Number) rawResult).longValue());
        } finally {
            entityManager.close();
        }
    }

    @Test
    void createAndFindPatientByCode() {
        patientRepository.create(new Patient(
                "PAT-1001",
                "Rakoto",
                "Alice",
                Sexe.FEMININ,
                "Antananarivo"));

        Patient patient = patientRepository
                .findByCode("PAT-1001")
                .orElseThrow();

        assertEquals("Rakoto", patient.getNom());
        assertEquals("Alice", patient.getPrenom());
        assertEquals(Sexe.FEMININ, patient.getSexe());
    }

    @Test
    void searchPatientsByNom() {
        patientRepository.create(new Patient(
                "PAT-1002",
                "Rasoa",
                "Bruno",
                Sexe.MASCULIN,
                "Fianarantsoa"));

        patientRepository.create(new Patient(
                "PAT-1003",
                "Rakoto",
                "Claire",
                Sexe.FEMININ,
                "Antsirabe"));

        List<Patient> patients = patientRepository
                .findByNomContaining("rak");

        assertEquals(1, patients.size());
        assertEquals("PAT-1003", patients.get(0).getCodePat());
    }

    @Test
    void updatePatient() {
        Patient patient = patientRepository.create(new Patient(
                "PAT-1004",
                "Rabe",
                "David",
                Sexe.MASCULIN,
                "Mahajanga"));

        patient.setAdresse("Toamasina");
        patient.setSexe(Sexe.FEMININ);

        patientRepository.update(patient);

        Patient reloadedPatient = patientRepository
                .findByCode("PAT-1004")
                .orElseThrow();

        assertEquals("Toamasina", reloadedPatient.getAdresse());
        assertEquals(Sexe.FEMININ, reloadedPatient.getSexe());
    }

    @Test
    void deletePatient() {
        patientRepository.create(new Patient(
                "PAT-1005",
                "Rajaona",
                "Eva",
                Sexe.FEMININ,
                "Antananarivo"));

        assertTrue(patientRepository.deleteByCode("PAT-1005"));
        assertTrue(patientRepository.findByCode("PAT-1005").isEmpty());
        assertFalse(patientRepository.deleteByCode("PAT-1005"));
    }

    @Test
    void createUpdateAndDeleteMedecin() {
        Medecin medecin = medecinRepository.create(new Medecin(
                "MED-1001",
                "Andria",
                "Jean",
                "Cardiologue"));

        medecin.setGrade("Professeur");
        medecinRepository.update(medecin);

        assertEquals(
                "Professeur",
                medecinRepository
                        .findByCode("MED-1001")
                        .orElseThrow()
                        .getGrade());

        assertTrue(medecinRepository.deleteByCode("MED-1001"));
        assertTrue(medecinRepository.findByCode("MED-1001").isEmpty());
    }

    @Test
    void createReplaceAndDeleteVisite() {
        medecinRepository.create(new Medecin(
                "MED-1002",
                "Ranaivo",
                "Marie",
                "Generaliste"));

        patientRepository.create(new Patient(
                "PAT-1006",
                "Randria",
                "Fara",
                Sexe.FEMININ,
                "Toliara"));

        LocalDate firstDate = LocalDate.of(2026, 8, 29);

        visiterRepository.create("MED-1002", "PAT-1006", firstDate);

        List<Visiter> visits = visiterRepository.findAll();

        assertEquals(1, visits.size());
        assertEquals("MED-1002", visits.get(0).getMedecin().getCodeMed());
        assertEquals("PAT-1006", visits.get(0).getPatient().getCodePat());

        VisiterId firstId = new VisiterId(
                "MED-1002",
                "PAT-1006",
                firstDate);
        LocalDate updatedDate = LocalDate.of(2026, 8, 30);

        visiterRepository.replace(
                firstId,
                "MED-1002",
                "PAT-1006",
                updatedDate);

        VisiterId updatedId = new VisiterId(
                "MED-1002",
                "PAT-1006",
                updatedDate);

        assertTrue(visiterRepository.findById(firstId).isEmpty());
        assertTrue(visiterRepository.findById(updatedId).isPresent());
        assertTrue(visiterRepository.deleteById(updatedId));
        assertFalse(visiterRepository.deleteById(updatedId));
    }
}
