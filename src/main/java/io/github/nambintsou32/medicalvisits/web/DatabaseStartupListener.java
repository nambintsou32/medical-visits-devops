package io.github.nambintsou32.medicalvisits.web;

import org.flywaydb.core.api.output.MigrateResult;

import io.github.nambintsou32.medicalvisits.persistence.DatabaseMigrator;
import io.github.nambintsou32.medicalvisits.persistence.EntityManagerFactoryProvider;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class DatabaseStartupListener
        implements ServletContextListener {

    public static final String ENTITY_MANAGER_FACTORY_ATTRIBUTE =
            DatabaseStartupListener.class.getName()
                    + ".entityManagerFactory";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String jdbcUrl = System.getenv("DB_URL");

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            servletContext.log(
                "Database migration skipped: DB_URL is not configured"
            );
            return;
        }

        String username = requiredEnvironmentVariable("DB_USER");
        String password = requiredEnvironmentVariable("DB_PASSWORD");

        MigrateResult result = DatabaseMigrator.migrate(
            jdbcUrl,
            username,
            password
        );

        EntityManagerFactory entityManagerFactory =
                EntityManagerFactoryProvider.createFromEnvironment();

        servletContext.setAttribute(
            ENTITY_MANAGER_FACTORY_ATTRIBUTE,
            entityManagerFactory
        );

        servletContext.log(
            "Database migration completed: "
                + result.migrationsExecuted
                + " migration(s) executed"
        );
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Object attribute = event.getServletContext().getAttribute(
            ENTITY_MANAGER_FACTORY_ATTRIBUTE
        );

        if (attribute instanceof EntityManagerFactory entityManagerFactory) {
            entityManagerFactory.close();
        }
    }

    public static EntityManagerFactory getEntityManagerFactory(
            ServletContext servletContext
    ) {
        Object attribute = servletContext.getAttribute(
            ENTITY_MANAGER_FACTORY_ATTRIBUTE
        );

        if (attribute instanceof EntityManagerFactory entityManagerFactory) {
            return entityManagerFactory;
        }

        throw new IllegalStateException(
            "Database is not configured for this application"
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