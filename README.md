# JobConnect Backend

[![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![Keycloak](https://img.shields.io/badge/Security-Keycloak-gold?style=for-the-badge&logo=keycloak)](https://www.keycloak.org/)
[![Kubernetes](https://img.shields.io/badge/Orchestration-Kubernetes-326ce5?style=for-the-badge&logo=kubernetes)](https://kubernetes.io/)


> Eine cloud-native Backend-Referenzimplementierung für eine Job-Vermittlungsplattform.
> Entwickelt im Rahmen des Studiums an der Hochschule Karlsruhe.

## 📋 Projektübersicht

JobConnect ist ein beispielhaft implementiertes Backend-System, das moderne **Enterprise-Patterns** mit **Cloud-Native-Technologien** verbindet. Das System ermöglicht die Verwaltung von Stellenangeboten und Bewerbungsprozessen über eine gesicherte RESTful API.

Der Fokus dieses Projekts lag nicht nur auf funktionaler Korrektheit, sondern auf **Nicht-funktionalen Anforderungen** wie:
* **Wartbarkeit:** Durch strikte Schichtenarchitektur und DTO-Pattern.
* **Sicherheit:** Durchgängige OAuth2/OIDC-Implementierung.
* **Datenintegrität:** Vermeidung von Lost Updates und Race Conditions.
* **Betriebssicherheit:** Infrastructure-as-Code und Observability.

---

## 🏗️ Systemarchitektur

Das System folgt einer **Layered Architecture** (Controller, Service, Repository) und integriert einen externen Identity Provider (Keycloak) für das Access Management.

```mermaid
graph TD
    Client((REST Client))

    subgraph "Infrastructure"
        KC[Keycloak IAM]
    end

    subgraph "Application Layer (Spring Boot)"
        SEC[Spring Security / OAuth2]
        JC[JobOfferController]
        JS[JobOfferService]
        JR[JobOfferRepository]
    end

    subgraph "Persistence Layer"
        DB[(PostgreSQL DB)]
    end

    Client -- 1. Auth (Login) --> KC
    KC -- 2. JWT Token --> Client
    Client -- 3. Request + Bearer Token --> SEC
    SEC -- 4. Validate Token --> JC
    JC --> JS
    JS --> JR
    JR -- JDBC/Spring Data --> DB

    %% Clean Business Style
    style JC fill:#e1f5fe,stroke:#01579b,stroke-width:2px,color:#000
    style JS fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,color:#000
    style JR fill:#e0f2f1,stroke:#00695c,stroke-width:2px,color:#000
    style DB fill:#f5f5f5,stroke:#616161,stroke-width:2px,color:#000
    style Client fill:#ffffff,stroke:#333,stroke-width:2px,color:#000
    style KC fill:#eeeeee,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5,color:#000
    style SEC fill:#ffcdd2,stroke:#c62828,stroke-width:2px,color:#000

```

### Datenmodell (Entity Relationship)

Das Domänenmodell zentriert sich um das Aggregate Root `JobOffer`.

```mermaid
classDiagram
    class JobOffer {
        +UUID id
        +String company
        +LocalDate publicationDate
        +JobOfferStatus status
        +Long version
    }

    class JobDescription {
        +String title
        +BigDecimal salary
        +EmploymentType employment
    }

    class Application {
        +UUID id
        +String applicant
        +ApplicationStatus status
    }

    JobOffer "1" *-- "1" JobDescription : contains
    JobOffer "1" *-- "0..*" Application : receives

```

---

## 🛠️ Technologie-Stack

Das Projekt nutzt aktuelle Standards und Preview-Features des Java-Ökosystems.

| Bereich | Technologie / Tool | Beschreibung |
| --- | --- | --- |
| **Core** | **Java 25** | Nutzung von Preview Features für modernen Code. |
| **Framework** | **Spring Boot 3.x** | WebMVC, Data JPA, Validation, Mail. |
| **Security** | **Keycloak & OAuth2** | OIDC Provider, JWT Handling, Resource Server. |
| **Database** | **PostgreSQL & Flyway** | Relationale DB mit versionierten Schema-Migrationen. |
| **API Docs** | **OpenAPI (Swagger)** | Automatische Generierung der API-Spezifikation. |
| **Testing** | **JUnit 5 & Mockito** | Unit-Tests, Integrationstests (SpringBootTest). |
| **Quality** | **SpotBugs, PMD, Checkstyle** | Statische Codeanalyse & NullAway (Null-Safety). |
| **Observability** | **Micrometer & Zipkin** | Distributed Tracing und Prometheus Metriken. |

### Infrastructure as Code (IaC)

Die Infrastruktur ist vollständig deklarativ definiert und befindet sich im Verzeichnis `extras/`.

| Tool | Verwendungszweck |
| --- | --- |
| **Docker Compose** | Lokale Entwicklungsumgebung (DB, Keycloak, App). |
| **Kubernetes (K8s)** | Deployment-Manifeste, Services und Ingress-Konfiguration. |
| **Helm Charts** | Paketierung der Anwendung für K8s-Cluster. |
| **Terraform** | Provisionierung der Cloud-Ressourcen. |
| **Pulumi** | Alternative Infrastruktur-Definition (TypeScript/Java). |

---

## Design-Entscheidungen & Patterns

* **Model View Controller (MVC):** Strukturierung der Software in drei logische Einheiten.
* **Optimistic Locking:** Verwendung von `@Version` Feldern in JPA-Entitäten, um *Lost Updates* bei parallelen Zugriffen zu verhindern.
* **DTO Pattern:** Strikte Trennung von Persistenzschicht und API durch Data Transfer Objects (DTOs) und Mapper (MapStruct).
* **Fail-Fast Validierung:** Eingangsdaten werden direkt am Controller mittels Jakarta Validation geprüft.
* **Pagination:** Performance-Optimierung durch Paging bei Listen-Endpunkten.
* **Lazy Logging:** Ressourcenschonendes Logging nur bei Bedarf.

---

## Installation & Start

### Voraussetzungen

* **JDK 25** (Preview Features müssen aktiviert sein)
* **Docker Desktop** (oder kompatible Runtime)
* **Maven** (Wrapper `mvnw` liegt bei)

### Lokales Deployment

1. **Repository klonen**
```bash
git clone [https://github.com/niko1405/jobconnect-backend.git](https://github.com/niko1405/jobconnect-backend.git)
cd jobconnect-backend

```


2. **Infrastruktur starten** (PostgreSQL & Keycloak)
```bash
cd extras/compose
docker-compose up -d

```


3. **Anwendung starten**
```bash
cd ../..
./mvnw spring-boot:run

```



**Zugriffspunkte:**

* API: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui.html`
* Keycloak Admin: `http://localhost:8081`

---

## 📄 Lizenz & Copyright

Copyright (C) 2016 - present Jürgen Zimmermann, Hochschule Karlsruhe.
Dieses Projekt steht unter der **GNU General Public License v3.0**.

```

```
