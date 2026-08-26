package io.github.nambintsou32.medicalvisits.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import io.github.nambintsou32.medicalvisits.domain.Patient;
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

    @BeforeAll
    static void createPersistenceContext() {
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
            "create-drop",
            "hibernate.show_sql",
            "false"
        );

        entityManagerFactory =
            EntityManagerFactoryProvider.create(properties);

        patientRepository =
            new PatientRepository(entityManagerFactory);
    }

    @BeforeEach
    void deleteExistingPatients() {
        EntityManager entityManager =
            entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
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
    void createPersistsPatientInMySql() {
        Patient patient = new Patient(
            "MRN-1001",
            "Alice",
            "Rakoto",
            LocalDate.of(1998, 4, 15)
        );

        Patient createdPatient = patientRepository.create(patient);

        assertNotNull(createdPatient.getId());
        assertEquals("MRN-1001", createdPatient.getMedicalRecordNumber());
    }

    @Test
    void findByIdReadsPatientFromMySql() {
        Patient createdPatient = patientRepository.create(
            new Patient(
                "MRN-1002",
                "Bruno",
                "Rasoa",
                LocalDate.of(1989, 7, 21)
            )
        );

        Patient foundPatient = patientRepository
            .findById(createdPatient.getId())
            .orElseThrow();

        assertEquals(createdPatient.getId(), foundPatient.getId());
        assertEquals("Bruno", foundPatient.getFirstName());
        assertEquals("Rasoa", foundPatient.getLastName());
    }

    @Test
    void updateModifiesPatientInMySql() {
        Patient createdPatient = patientRepository.create(
            new Patient(
                "MRN-1003",
                "Claire",
                "Andria",
                LocalDate.of(1995, 10, 8)
            )
        );

        createdPatient.setFirstName("Clara");
        createdPatient.setPhone("+261340000003");

        Patient updatedPatient =
            patientRepository.update(createdPatient);

        Patient reloadedPatient = patientRepository
            .findById(updatedPatient.getId())
            .orElseThrow();

        assertEquals("Clara", reloadedPatient.getFirstName());
        assertEquals("+261340000003", reloadedPatient.getPhone());
    }

    @Test
    void deleteRemovesPatientFromMySql() {
        Patient createdPatient = patientRepository.create(
            new Patient(
                "MRN-1004",
                "David",
                "Rabe",
                LocalDate.of(2000, 1, 12)
            )
        );

        boolean deleted =
            patientRepository.deleteById(createdPatient.getId());

        assertTrue(deleted);
        assertTrue(
            patientRepository.findById(createdPatient.getId()).isEmpty()
        );
        assertFalse(
            patientRepository.deleteById(createdPatient.getId())
        );
    }

    @Test
    void findAllReturnsPersistedPatients() {
        patientRepository.create(
            new Patient(
                "MRN-1005",
                "Eva",
                "Rajaona",
                LocalDate.of(1993, 6, 4)
            )
        );

        patientRepository.create(
            new Patient(
                "MRN-1006",
                "Fara",
                "Ramanana",
                LocalDate.of(1987, 11, 19)
            )
        );

        List<Patient> patients = patientRepository.findAll();

        assertEquals(2, patients.size());
        assertEquals("MRN-1005", patients.get(0).getMedicalRecordNumber());
        assertEquals("MRN-1006", patients.get(1).getMedicalRecordNumber());
    }
}