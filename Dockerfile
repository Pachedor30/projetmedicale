# Build the WAR and fetch the PostgreSQL driver used by WildFly's datasource.
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml ./pom.xml
COPY src ./src

RUN mvn -B -DskipTests package \
    org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies \
    -DincludeArtifactIds=postgresql \
    -DoutputDirectory=target/docker-dependencies \
    -Dmdep.stripVersion=true

# Run the WAR on the exact WildFly/JDK versions used by the local project.
FROM quay.io/wildfly/wildfly:41.0.1.Final-jdk21

USER root

RUN mkdir -p \
    /opt/jboss/wildfly/modules/org/postgresql/main \
    /opt/jboss/wildfly/modules/com/mycompany/projetmedical/security/main

COPY --from=build /build/target/Projetmedical-1.0-SNAPSHOT.war \
    /opt/jboss/wildfly/standalone/deployments/Projetmedical-1.0-SNAPSHOT.war

COPY --from=build /build/target/docker-dependencies/postgresql.jar \
    /opt/jboss/wildfly/modules/org/postgresql/main/postgresql.jar
COPY docker/postgresql-module.xml \
    /opt/jboss/wildfly/modules/org/postgresql/main/module.xml

COPY --from=build /build/target/Projetmedical-1.0-SNAPSHOT-wildfly-jaas.jar \
    /opt/jboss/wildfly/modules/com/mycompany/projetmedical/security/main/projetmedical-wildfly-jaas.jar
COPY --from=build /build/target/Projetmedical-1.0-SNAPSHOT/WEB-INF/lib/jbcrypt-0.4.jar \
    /opt/jboss/wildfly/modules/com/mycompany/projetmedical/security/main/jbcrypt-0.4.jar
COPY wildfly-jaas/module.xml \
    /opt/jboss/wildfly/modules/com/mycompany/projetmedical/security/main/module.xml
COPY wildfly-jaas/clinique-jaas.conf \
    /opt/jboss/wildfly/standalone/configuration/clinique-jaas.conf
COPY docker/standalone.xml \
    /opt/jboss/wildfly/standalone/configuration/standalone.xml

RUN chown -R jboss:root /opt/jboss/wildfly/modules/org/postgresql \
    /opt/jboss/wildfly/modules/com/mycompany/projetmedical/security \
    /opt/jboss/wildfly/standalone/deployments/Projetmedical-1.0-SNAPSHOT.war \
    /opt/jboss/wildfly/standalone/configuration/standalone.xml \
    /opt/jboss/wildfly/standalone/configuration/clinique-jaas.conf

USER jboss

# Render supplies PORT (10000 by default); standalone.xml binds WildFly to it.
EXPOSE 10000
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
