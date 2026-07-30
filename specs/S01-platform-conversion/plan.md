# S01 Platform Conversion Plan

## Platform Foundation (Extensions → Models → Resources → Config → Tests)

The POM conversion story establishes the Quarkus foundation through mechanical dependency and build tool modernizations. All transformations are deterministic POM changes that prepare the codebase for subsequent code modernization stories.

### S01.1 Platform BOM Conversion

**Transform**: Replace Spring Boot parent with Quarkus platform BOM  
**Class**: rewrite  
**Source**: `/projects/legacy/pom.xml:18-26` → Quarkus 3.27.3.SP1 foundation  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: Spring Boot 2.7.18 parent → Quarkus BOM with Red Hat platform coordinates  
**Rationale**: Platform BOM provides unified dependency management across all Quarkus extensions

### S01.2 Maven Plugin Modernization

**Transform**: Replace Spring Boot Maven plugin with Quarkus Maven plugin  
**Class**: rewrite  
**Source**: `/projects/legacy/pom.xml:103-106` → Quarkus development and build tooling  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: spring-boot-maven-plugin → quarkus-maven-plugin with platform version  
**Rationale**: Quarkus plugin enables dev mode, hot reload, and native compilation capabilities

### S01.3 Standard Maven Plugin Alignment

**Transform**: Update Maven Compiler, Surefire, and Failsafe plugins to Quarkus-compatible versions  
**Class**: rewrite  
**Source**: Implicit Spring Boot plugin management → Explicit Quarkus-aligned plugin configuration  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: Spring-managed plugin versions → Platform-managed plugin versions  
**Rationale**: Consistent plugin versions across development, test, and build lifecycle

### S01.4 Spring Boot Actuator Replacement

**Transform**: Replace Spring Boot Actuator with Quarkus SmallRye Health  
**Class**: rewrite  
**Source**: `/projects/legacy/pom.xml:65-67` → Quarkus health and metrics extensions  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: spring-boot-starter-actuator → quarkus-smallrye-health  
**Rationale**: Health endpoints are essential for container orchestration and monitoring

### S01.5 Metrics Framework Modernization

**Transform**: Replace Micrometer metrics with Quarkus SmallRye Metrics  
**Class**: rewrite  
**Source**: Implicit Micrometer through Spring Boot → Explicit SmallRye Metrics  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: Spring-managed metrics → Quarkus-managed MicroProfile metrics  
**Rationale**: Metrics standardization for cloud-native observability

### S01.6 JAX-RS Dependency Update

**Transform**: Replace Spring Boot Jersey with Quarkus REST (JAX-RS) support  
**Class**: rewrite  
**Source**: `/projects/legacy/pom.xml:59-62` → Quarkus REST extension  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: spring-boot-starter-jersey → quarkus-rest-jackson  
**Rationale**: Direct JAX-RS support through Quarkus eliminates Spring-specific overhead

### S01.7 Feign Client Migration Path

**Transform**: Prepare for Feign → Quarkus REST client migration  
**Class**: rewrite  
**Source**: `/projects/legacy/pom.xml:69-72` → Maintain during platform conversion  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: spring-cloud-starter-openfeign → retained for S03 migration  
**Rationale**: Feign client modernization deferred to service integration story (S03)

### S01.8 Test Framework Modernization

**Transform**: Replace Spring Boot test dependencies with Quarkus test artifacts  
**Class**: rewrite  
**Source**: `/projects/legacy/pom.xml:74-98` → Quarkus test framework  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: spring-boot-starter-test → quarkus-junit5, assertj, test dependencies  
**Rationale**: Quarkus-native testing provides faster startup and better integration

### S01.9 Native Build Profile

**Transform**: Add Quarkus native build profile configuration  
**Class**: rewrite  
**Source**: No native configuration → Native compilation profile  
**Target**: `/projects/modernized/pom.xml`  
**Mapping**: Standard JAR packaging → Native executable profile  
**Rationale**: Native compilation support essential for serverless and microservices deployment

### S01.10 Dependency Validation

**Transform**: Verify all Spring dependencies removed, Quarkus dependencies aligned  
**Class**: infer  
**Source**: POM dependency audit → Build verification  
**Target**: `/projects/modernized/pom.xml` + `mvn clean compile`  
**Validation**: Zero Spring Boot dependencies, all Quarkus extensions properly versioned  
**Rationale**: Ensures clean platform foundation for subsequent code transformation stories