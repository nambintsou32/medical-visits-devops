FROM maven:3.9.16-eclipse-temurin-21-alpine@sha256:65353f527c86cb23187c8233475713e15067e8d36220d18863c379680698fe85 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM tomcat:11.0.25-jre21-temurin-noble@sha256:c949189cc9d868547408e6b1b9911545e2a72c6088af7905866ee332d06f44f3 AS runtime

ARG APP_UID=10001
ARG APP_GID=10001

RUN groupadd --gid "${APP_GID}" medical-visits \
    && useradd \
        --uid "${APP_UID}" \
        --gid "${APP_GID}" \
        --home-dir "${CATALINA_HOME}" \
        --shell /usr/sbin/nologin \
        --no-create-home \
        medical-visits \
    && rm -rf "${CATALINA_HOME}/webapps/"* \
    && chown -R "${APP_UID}:${APP_GID}" "${CATALINA_HOME}"

COPY --from=build \
    --chown=${APP_UID}:${APP_GID} \
    /workspace/target/medical-visits.war \
    ${CATALINA_HOME}/webapps/ROOT.war

USER ${APP_UID}:${APP_GID}

EXPOSE 8080

CMD ["catalina.sh", "run"]
