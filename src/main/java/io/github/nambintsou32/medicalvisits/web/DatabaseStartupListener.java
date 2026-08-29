package io.github.nambintsou32.medicalvisits.web;

import org.flywaydb.core.api.output.MigrateResult;

import io.github.nambintsou32.medicalvisits.persistence.DatabaseMigrator;
import io.github.nambintsou32.medicalvisits.persistence.EntityManagerFactoryProvider;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class DatabaseStartupListener
        implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        String jdbcUrl = requiredEnvironmentVariable("DB_URL");
        String username = requiredEnvironmentVariable("DB_USER");
        String password = requiredEnvironmentVariable("DB_PASSWORD");

        MigrateResult result = DatabaseMigrator.migrate(
            jdbcUrl,
            username,
            password
        );

        EntityManagerFactory entityManagerFactory =
            EntityManagerFactoryProvider.createFromEnvironment();

        entityManagerFactory.close();

        event.getServletContext().log(
            "Database migration completed: "
                + result.migrationsExecuted
                + " migration(s) executed"
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
}
