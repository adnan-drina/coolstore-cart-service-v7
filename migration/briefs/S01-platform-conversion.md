# S01: Platform and BOM conversion

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Establish the Quarkus foundation by converting the Spring Boot POM to Quarkus platform BOM and updating all Maven plugins and dependencies. This story provides the necessary dependency framework for all subsequent modernization stories, as POM-based transformations must complete before any code changes.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `pom.xml` — Spring Boot parent POM and dependency configuration
  ```xml
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.12</version>
    <relativePath/>
  </parent>
  ```
  ```xml
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
  </dependencies>
  ```

## Out of scope

No application code changes in this story. All source files remain in their legacy Spring Boot structure until subsequent stories. The tree must stay buildable with the new Quarkus dependencies.

## Class roles & target contract (from architecture-profile §7)

N/A - this story deals only with POM transformations, not application classes.

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Story ordering:** extensions and BOM first, then models, then resources,
then config keys, then tests (`extensions → models → resources → config →
tests`).

Platform POM conversion (javaee-pom-to-quarkus-00010): Replace Spring Boot parent with Quarkus platform BOM:
```xml
<parent>
  <groupId>com.redhat.quarkus.platform</groupId>
  <artifactId>quarkus-bom</artifactId>
  <version>3.27.3.SP1</version>
</parent>
```

Maven plugin updates (javaee-pom-to-quarkus-00020, 00030, 00040, 00050, 00060): Replace Spring Boot plugin with Quarkus Maven plugin:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>${quarkus.platform.group-id}</groupId>
      <artifactId>quarkus-maven-plugin</artifactId>
      <version>${quarkus.platform.version}</version>
    </plugin>
  </plugins>
</build>
```

Quarkus dependency replacements (springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100): Replace Spring Boot dependencies with Quarkus extensions:
```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-smallrye-metrics</artifactId>
</dependency>
```

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080
  - springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000
  - springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100

- **Preserve**: none - this story establishes the foundation, no legacy contracts to preserve yet.

- **Behavioral pins**: none - POM changes don't affect runtime behavior.

- **Forbidden**: none relevant to this story.

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- `mvn clean compile` succeeds with Quarkus BOM dependencies
- Quarkus Maven plugin available for subsequent stories
- No Spring Boot dependencies remain in pom.xml
- All 10 required findings resolved (no longer fire on re-analysis)
