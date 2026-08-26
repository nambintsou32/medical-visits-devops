package io.github.nambintsou32.medicalvisits.persistence;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class EntityManagerFactoryProvider {

    public static final String PERSISTENCE_UNIT = "medical-visits";

    private EntityManagerFactoryProvider() {
    }

    public static EntityManagerFactory createFromEnvironment() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(
            "jakarta.persistence.jdbc.driver",
            "com.mysql.cj.jdbc.Driver"
        );
        properties.put(
            "jakarta.persistence.jdbc.url",
            requiredEnvironmentVariable("DB_URL")
        );
        properties.put(
            "jakarta.persistence.jdbc.user",
            requiredEnvironmentVariable("DB_USER")
        );
        properties.put(
            "jakarta.persistence.jdbc.password",
            requiredEnvironmentVariable("DB_PASSWORD")
        );
        properties.put(
            "hibernate.hbm2ddl.auto",
            environmentVariableOrDefault("HIBERNATE_DDL_AUTO", "validate")
        );

        return create(properties);
    }

    public static EntityManagerFactory create(
            Map<String, ?> overrideProperties
    ) {
        return Persistence.createEntityManagerFactory(
            PERSISTENCE_UNIT,
            overrideProperties
        );
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required environment variable is missing: " + name
            );
        }

        return value;
    }

    private static String environmentVariableOrDefault(
            String name,
            String defaultValue
    ) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}