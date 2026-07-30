# S01 Platform Conversion Spec

## Legacy Behavior and API Contract

This story establishes the Quarkus foundation through POM transformations. The Coolstore Cart Service is a Spring Boot 2.7.18 application that will be migrated to Quarkus 3.27.3.SP1, providing the dependency framework for all subsequent modernization stories.

### Legacy POM Configuration

**Spring Boot Parent POM** (`/projects/legacy/pom.xml:18-26`):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/>
</parent>
```

**Spring Boot Dependencies** (`/projects/legacy/pom.xml:53-72`):
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jersey</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
</dependencies>
```

**Spring Boot Build Plugin** (`/projects/legacy/pom.xml:103-106`):
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

### Dependency Management

The legacy application uses Spring Cloud dependency management (`/projects/legacy/pom.xml:35-45`) for Feign client support, with Java 11 as the target version (`/projects/legacy/pom.xml:50`). Red Hat GA repository provides enterprise dependencies.

### Integration Points

- **Feign Client Integration**: Spring Cloud OpenFeign for catalog service communication
- **Actuator Endpoints**: Spring Boot Actuator for health and management endpoints
- **Jersey Integration**: Spring Boot Starter Jersey for JAX-RS support
- **Test Framework**: Spring Boot Test with JUnit Vintage, AssertJ, and Hoverfly for integration testing

## Target Contract

The POM conversion establishes the Quarkus platform foundation while preserving the application's integration surfaces for subsequent stories:

- **Platform Foundation**: Quarkus 3.27.3.SP1 BOM provides all necessary dependency management
- **Build Tooling**: Quarkus Maven plugin enables dev mode and native compilation
- **Health Management**: Quarkus SmallRye Health replaces Spring Boot Actuator
- **Metrics**: Quarkus SmallRye Metrics replaces Micrometer
- **REST Support**: JAX-RS through Quarkus REST (replacing Spring Boot Starter Web + Jersey)
- **Test Support**: Quarkus test artifacts replace Spring Boot test dependencies

### Legacy User Interface Surface

**Waiver**: This story contains no user-facing interface changes. The application provides REST API endpoints only, with no web UI, HTML pages, or browser-facing functionality. The REST endpoints will be modernized in S02, with acceptance testing added at that time.

No behavioral changes occur in this story — only dependency and build configuration modernization. The application structure, endpoints, and business logic remain unchanged until subsequent stories.