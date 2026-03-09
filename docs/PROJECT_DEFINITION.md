# InnKeeper — Project Definition

## Table of Contents

- [Overview](#overview)
- [Description](#description)
- [Core Concepts](#core-concepts)
- [Architecture Breakdown](#architecture-breakdown)
  - [1. High-Level System Architecture](#1-high-level-system-architecture)
  - [2. Custom Resource Lifecycle State Machines](#2-custom-resource-lifecycle-state-machines)
  - [3. Event-Driven Architecture](#3-event-driven-architecture)
  - [4. Guest Reconciliation Sequence](#4-guest-reconciliation-sequence)
  - [5. Build Pipeline Flow](#5-build-pipeline-flow)
  - [6. Extension Framework Class Diagram](#6-extension-framework-class-diagram)
  - [7. REST API and Security Layer](#7-rest-api-and-security-layer)
  - [8. Kubernetes Deployment Topology](#8-kubernetes-deployment-topology)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)

---

## Overview

**InnKeeper** is a Kubernetes-native CI/CD operator written in Java. It automates the full application lifecycle inside a Kubernetes cluster — building container images from Git sources, creating Deployments, exposing Services, and configuring ingress routing — all driven by declarative Custom Resource Definitions (CRDs).

The operator is built on the **Java Operator SDK (JOSDK)** for reconciliation loops and **Spring Boot** for its REST API surface.

---

## Description

InnKeeper acts as an "innkeeper" for applications running in a Kubernetes cluster. A user declares their desired state through a single **Guest** custom resource, which describes:

- **What to build** — Git repository URL, branch, Dockerfile path, and container registry push targets
- **What to deploy** — Container images, replica counts, resource limits, and volume mounts
- **What to expose** — Kubernetes Service ports, protocols, and selectors
- **What to extend** — Istio HTTP routes, TCP backend proxies, and gateway configurations

The operator watches these custom resources and continuously reconciles declared state against actual cluster state, creating, updating, or removing Kubernetes-native resources (Jobs, Deployments, Services, Gateways) as needed.

### Key Capabilities

| Capability | Description |
|---|---|
| **Automated Builds** | Monitors Git repositories for changes and triggers container image builds via Kubernetes Jobs using a dedicated builder image |
| **Deployment Management** | Creates and patches Kubernetes Deployments with configurable containers, replicas, image pull secrets, and volumes |
| **Service Exposure** | Generates Kubernetes Services with configurable port mappings and selectors |
| **Extension Framework** | Pluggable architecture supporting Istio HTTP routing, TCP backend proxies, and custom gateway management |
| **REST API** | Full CRUD API with OAuth2 (Google) and Bearer token authentication, Swagger documentation, and WebSocket support |
| **Account Management** | Role-based access control with a hierarchical permission tree model stored as Kubernetes custom resources |
| **Self-Bootstrapping** | Automatically registers its own CRDs on startup if they do not already exist in the cluster |

---

## Core Concepts

All custom resources belong to the API group `cicd.innkeeper.run/v1`.

| Resource | Description |
|---|---|
| **Guest** | Top-level orchestration resource. Declares the builds, deployments, services, and extensions for a single application. The GuestReconciler coordinates all child resources. |
| **Build** | Represents a container image build from a Git source. Managed as a Kubernetes Job using the `git-builder` image. Tracks state through a multi-step state machine. |
| **Deployment** | Represents a Kubernetes Deployment. Spec includes containers (with references to Build names), replicas, and volumes. |
| **Service** | Represents a Kubernetes Service with port definitions and a deployment selector. |
| **SimpleExtension** | Represents a pluggable extension. The `type` field routes to a specific handler (e.g., `BackendProxy`, `IstioHttpRoute`). |
| **Account** | Represents a user account with an email and a set of permissions. The first user to authenticate becomes the admin. |

---

## Architecture Breakdown

### 1. High-Level System Architecture

This diagram shows the four major layers of the system and how data flows between them.

```mermaid
graph TB
    subgraph External["External Interfaces"]
        Browser["Browser / Web UI"]
        CLI["CLI / SDK"]
        Git["Git Repositories"]
        Registry["Container Registry<br/>(GHCR)"]
    end

    subgraph APILayer["REST API Layer — Spring Boot :8081"]
        OAuth["OAuth2 Security<br/>(Google OIDC)"]
        Token["Bearer Token Auth"]
        Endpoints["REST Controllers<br/>Guest · Build · Deployment<br/>Service · Account · Extension · Pod"]
        Swagger["Swagger UI — /docs"]
        WebSocket["WebSocket Relay<br/>/oauth/stomp"]
    end

    subgraph OperatorLayer["Operator Layer — JOSDK Reconcilers"]
        GR["GuestReconciler<br/>(3s interval)"]
        BR["BuildReconciler"]
        DR["DeploymentReconciler"]
        SR["ServiceReconciler"]
        AR["AccountReconciler"]
        ER["SimpleExtensionReconciler"]
    end

    subgraph OrchestrationLayer["Orchestration Layer — EventBus + Controllers"]
        EB["EventBus<br/>(Reflection-based dispatcher)"]
        BCon["BuildController"]
        DCon["DeploymentController"]
        SCon["ServiceController"]
        GCon["GuestController"]
        ECon["ExtensionController"]
    end

    subgraph IntegrationLayer["Kubernetes Integration Layer"]
        K8s["K8sService<br/>(Fabric8 Client Singleton)"]
        BB["BuildBus / JobBus"]
        DB["DeploymentBus"]
        SB["ServiceBus"]
        GitB["GitBus"]
        ExtB["ExtensionBus"]
    end

    subgraph ClusterResources["Kubernetes Cluster"]
        CRDs["Custom Resources<br/>Guest · Build · Deployment<br/>Service · Extension · Account"]
        Jobs["Build Jobs"]
        K8sDeploy["K8s Deployments"]
        K8sSvc["K8s Services"]
        Gateways["Gateways / HTTP & TCP Routes"]
    end

    Browser --> OAuth
    CLI --> Token
    OAuth --> Endpoints
    Token --> Endpoints
    Endpoints --> K8s
    WebSocket --> K8s

    GR --> EB
    BR --> EB
    DR --> EB
    SR --> EB
    ER --> EB

    EB --> BCon
    EB --> DCon
    EB --> SCon
    EB --> GCon
    EB --> ECon

    BCon --> BB
    DCon --> DB
    SCon --> SB
    GCon --> GitB
    ECon --> ExtB

    BB --> K8s
    DB --> K8s
    SB --> K8s
    GitB --> K8s
    ExtB --> K8s

    K8s --> CRDs
    K8s --> Jobs
    K8s --> K8sDeploy
    K8s --> K8sSvc
    K8s --> Gateways

    Git -.->|polled by| GitB
    BB -.->|pushes images| Registry
```

---

### 2. Custom Resource Lifecycle State Machines

Each custom resource follows a state machine that the corresponding reconciler drives forward on every reconciliation cycle.

```mermaid
stateDiagram-v2
    state "Build Lifecycle" as BuildSM {
        [*] --> WAITING
        WAITING --> GIT_CHECK : Reconcile triggered
        GIT_CHECK --> NEED_TO_BUILD : Changes detected
        GIT_CHECK --> WAITING : No changes
        NEED_TO_BUILD --> BUILDING : K8s Job created
        BUILDING --> WAITING : Build succeeded
        BUILDING --> BUILD_FAILED : Job error
        BUILD_FAILED --> GIT_CHECK : Next reconcile
    }

    state "Deployment Lifecycle" as DeploySM {
        [*] --> NEED_TO_DEPLOY
        NEED_TO_DEPLOY --> DEPLOYED : K8s Deployment created
        DEPLOYED --> REDEPLOY : Spec changed
        REDEPLOY --> DEPLOYED : K8s Deployment patched
    }

    state "Service Lifecycle" as ServiceSM {
        [*] --> SVC_NEED_TO_CREATE
        SVC_NEED_TO_CREATE --> CREATED : K8s Service created
        CREATED --> RECREATE : Spec changed
        RECREATE --> CREATED : K8s Service updated
    }

    state "Extension Lifecycle" as ExtSM {
        [*] --> EXT_NEED_TO_CREATE
        EXT_NEED_TO_CREATE --> UP_TO_DATE : Extension applied
        UP_TO_DATE --> NEED_TO_UPDATE : Config changed
        NEED_TO_UPDATE --> UP_TO_DATE : Extension patched
    }
```

---

### 3. Event-Driven Architecture

The EventBus is a reflection-based dispatcher. On startup it scans the `run.innkeeper.controllers` package for methods annotated with `@Trigger(EventClass.class)` and builds a dispatch table. When a reconciler fires an event, the bus routes it to all matching handlers.

```mermaid
flowchart LR
    subgraph Producers["Event Producers (Reconcilers)"]
        GR["GuestReconciler"]
        BR["BuildReconciler"]
        DR["DeploymentReconciler"]
        SR["ServiceReconciler"]
    end

    subgraph Events["Event Types"]
        direction TB
        E1["CheckGuestBuildChanges"]
        E2["CheckGuestDeploymentChanges"]
        E3["CheckGuestServiceChanges"]
        E4["CheckGuestExtensionChanges"]
        E5["StartBuild / MonitorBuild"]
        E6["CreateDeployment / UpdateDeployment"]
        E7["CreateService / UpdateService"]
    end

    subgraph Bus["EventBus"]
        Dispatch["fire(event)<br/>↓<br/>@Trigger method lookup<br/>↓<br/>Method.invoke()"]
    end

    subgraph Consumers["Event Consumers (Controllers)"]
        BC["BuildController<br/>• startBuild()<br/>• monitorBuild()<br/>• checkGitBuild()"]
        DC["DeploymentController<br/>• createDeployment()<br/>• updateDeployment()"]
        SC["ServiceController<br/>• createService()<br/>• updateService()"]
        GC["GuestController<br/>• checkIfBuildUpdated()<br/>• checkIfDeployUpdated()"]
        EC["ExtensionController<br/>• createExtension()<br/>• updateExtension()"]
    end

    GR --> E1 & E2 & E3 & E4
    BR --> E5
    DR --> E6
    SR --> E7

    E1 & E2 & E3 & E4 --> Dispatch
    E5 & E6 & E7 --> Dispatch

    Dispatch --> BC & DC & SC & GC & EC
```

**Event Hierarchy:**

```mermaid
classDiagram
    class Event {
        <<abstract>>
    }
    class GuestEvent {
        +Guest guest
    }
    class BuildEvent {
        +Build build
    }
    class DeploymentEvent {
        +Deployment deployment
    }
    class ServiceEvent {
        +Service service
    }
    class ExtensionEvent {
        +SimpleExtension extension
    }

    Event <|-- GuestEvent
    Event <|-- BuildEvent
    Event <|-- DeploymentEvent
    Event <|-- ServiceEvent
    Event <|-- ExtensionEvent
    Event <|-- ServerStarted

    GuestEvent <|-- CheckGuestBuildChanges
    GuestEvent <|-- CheckGuestDeploymentChanges
    GuestEvent <|-- CheckGuestServiceChanges
    GuestEvent <|-- CheckGuestExtensionChanges
    GuestEvent <|-- DeleteGuestBuild
    GuestEvent <|-- DeleteGuestDeployment
    GuestEvent <|-- DeleteGuestService

    BuildEvent <|-- StartBuild
    BuildEvent <|-- MonitorBuild
    BuildEvent <|-- MonitorGit
    BuildEvent <|-- CheckGitBuild
    BuildEvent <|-- FailedBuild
    BuildEvent <|-- BuildFinished
    BuildEvent <|-- UpdateBuild

    DeploymentEvent <|-- CreateDeployment
    DeploymentEvent <|-- UpdateDeployment
    DeploymentEvent <|-- CheckDeployment

    ServiceEvent <|-- CreateService
    ServiceEvent <|-- UpdateService
```

---

### 4. Guest Reconciliation Sequence

This diagram shows the end-to-end flow when a user creates a Guest resource.

```mermaid
sequenceDiagram
    actor User
    participant K8sAPI as Kubernetes API Server
    participant GR as GuestReconciler
    participant EB as EventBus
    participant GC as GuestController
    participant BC as BuildController
    participant DC as DeploymentController
    participant SC as ServiceController
    participant EC as ExtensionController
    participant Buses as Buses (Build/Deploy/Svc)
    participant K8s as K8sService

    User->>K8sAPI: kubectl apply -f guest.yaml
    K8sAPI-->>GR: Watch event — Guest created

    Note over GR: Iterates over spec.builds[]
    GR->>EB: fire(CheckGuestBuildChanges)
    EB->>GC: checkIfBuildUpdated()
    GC->>GC: JSON Patch diff (old vs new)

    alt Build is new or changed
        GC->>K8s: Create/update Build CRD
        K8s->>K8sAPI: Build resource persisted
    end

    Note over GR: Iterates over spec.deployments[]
    GR->>EB: fire(CheckGuestDeploymentChanges)
    EB->>GC: checkIfDeploymentUpdated()
    alt Deployment is new or changed
        GC->>K8s: Create/update Deployment CRD
    end

    Note over GR: Iterates over spec.services[]
    GR->>EB: fire(CheckGuestServiceChanges)
    EB->>GC: checkIfServiceUpdated()
    alt Service is new or changed
        GC->>K8s: Create/update Service CRD
    end

    Note over GR: Iterates over spec.ext[]
    GR->>EB: fire(CheckGuestExtensionChanges)
    EB->>GC: checkIfExtensionUpdated()

    Note over K8sAPI: Child reconcilers now run

    K8sAPI-->>BC: BuildReconciler — reconcile()
    BC->>Buses: BuildBus.create() → K8s Job
    Buses->>K8sAPI: Job created (git-builder image)

    K8sAPI-->>DC: DeploymentReconciler — reconcile()
    DC->>Buses: DeploymentBus.create() → K8s Deployment
    Buses->>K8sAPI: Deployment created

    K8sAPI-->>SC: ServiceReconciler — reconcile()
    SC->>Buses: ServiceBus.create() → K8s Service
    Buses->>K8sAPI: Service created
```

---

### 5. Build Pipeline Flow

This diagram details how a Build resource progresses from Git check to published container image.

```mermaid
flowchart TB
    subgraph Trigger["1 — Trigger"]
        Guest["Guest Resource<br/>spec.builds[]"]
        BuildCRD["Build CRD Created"]
    end

    subgraph GitPhase["2 — Git Check"]
        GitBus["GitBus"]
        Clone["Clone / Fetch Repo"]
        Diff{"Changes<br/>Detected?"}
    end

    subgraph BuildPhase["3 — Build Execution"]
        JobBus["BuildBus (extends JobBus)"]
        Job["Kubernetes Job"]
        Builder["Builder Image<br/>ghcr.io/innkeeperdevops/<br/>git-builder:28"]
        SecCtx["Security Context<br/>privileged: true"]
        Volumes["Volumes<br/>• EmptyDir (workspace)<br/>• Secret (SSH key)<br/>• Secret (registry auth)"]
    end

    subgraph Publish["4 — Publish"]
        Image["Container Image Built"]
        Push["Push to Registry"]
        UpdateStatus["Update Build Status<br/>→ WAITING"]
    end

    Guest --> BuildCRD
    BuildCRD -->|state: GIT_CHECK| GitBus
    GitBus --> Clone
    Clone --> Diff
    Diff -->|No| Wait["Status → WAITING<br/>(wait for next cycle)"]
    Diff -->|Yes| JobBus

    JobBus --> Job
    Job --> Builder
    Job --> SecCtx
    Job --> Volumes
    Builder --> Image
    Image --> Push
    Push --> UpdateStatus
```

**Environment variables injected into build Jobs:**

| Variable | Source |
|---|---|
| `GIT_URL` | BuildSettings.git.uri |
| `GIT_BRANCH` | BuildSettings.git.branch |
| `DOCKER_FILE` | BuildSettings.docker.file |
| `DOCKER_WORKDIR` | BuildSettings.docker.workdir |
| `PUBLISH_REGISTRY` | BuildSettings.publish.registry |
| `PUBLISH_TAG` | BuildSettings.publish.tag |

---

### 6. Extension Framework Class Diagram

The extension framework uses an `@Extension` annotation to mark pluggable handlers. The `ExtensionBus` discovers them via reflection and routes based on the `type` field in the `SimpleExtensionSpec`.

```mermaid
classDiagram
    class SimpleExtension {
        +SimpleExtensionSpec spec
        +SimpleExtensionStatus status
    }

    class SimpleExtensionSpec {
        +String name
        +String namespace
        +String type
        +Object settings
    }

    class SimpleExtensionStatus {
        +SimpleExtensionState state
    }

    class SimpleExtensionReconciler {
        +reconcile(SimpleExtension, Context)
        +cleanup(SimpleExtension, Context)
    }

    class ExtensionBus {
        +create(spec, namespace)
        +update(spec, namespace)
        +delete(name, namespace)
    }

    class Extension {
        <<annotation>>
        +String value()
    }

    class BackendProxy {
        +create(config)
        +update(config)
        +delete(name)
    }

    class IstioHttpRoute {
        +create(config)
        +update(config)
        +delete(name)
    }

    class KubernetesResources {
        Gateway
        TCPRoute
        HTTPRoute
    }

    SimpleExtension --> SimpleExtensionSpec
    SimpleExtension --> SimpleExtensionStatus
    SimpleExtensionReconciler --> SimpleExtension : watches
    SimpleExtensionReconciler --> ExtensionBus : delegates

    ExtensionBus ..> BackendProxy : type = BackendProxy
    ExtensionBus ..> IstioHttpRoute : type = IstioHttpRoute

    BackendProxy ..|> Extension
    IstioHttpRoute ..|> Extension

    BackendProxy --> KubernetesResources : creates Gateway + TCPRoute
    IstioHttpRoute --> KubernetesResources : creates HTTPRoute
```

---

### 7. REST API and Security Layer

This diagram shows the authentication pipeline and how requests reach the operator layer.

```mermaid
flowchart TB
    subgraph Clients["Clients"]
        Browser["Browser"]
        CLI["CLI / HTTP Client"]
        WSClient["WebSocket Client"]
    end

    subgraph AuthPipeline["Authentication Pipeline"]
        OAuthFilter["OAuth2 Login Filter<br/>→ /oauth2/authorize/google"]
        TokenFilter["Bearer Token Filter<br/>→ Authorization header"]
        NoAuth["NO_AUTH env var<br/>(dev mode only)"]
        AuthCheck{"Authenticated?"}
        PermCheck["@UserAuthorized<br/>aspect checks<br/>AccountService.hasPermission()"]
    end

    subgraph Endpoints["API Endpoints (port 8081)"]
        subgraph ResourceCRUD["Resource CRUD"]
            GuestEP["/oauth/guest · /token/guest<br/>list · get · update · builds<br/>deployments · services · extensions"]
            BuildEP["/oauth/build · /token/build<br/>list · get"]
            DeployEP["/oauth/deployment · /token/deployment<br/>list · get · pods"]
            AcctEP["/oauth/account · /token/account<br/>list · get · edit · grant · revoke"]
        end
        subgraph Utility["Utility"]
            Swagger["/docs — Swagger UI"]
            TS["/ts — TypeScript client gen"]
            PodEP["/oauth/pod — logs & info"]
        end
        subgraph Realtime["Realtime"]
            STOMP["/oauth/stomp — SockJS/STOMP"]
            Connect["/api/connect — auth handshake"]
            Relay["/api/relay — command relay"]
        end
    end

    subgraph Backend["Operator Backend"]
        K8sSvc["K8sService"]
        AcctSvc["AccountService"]
    end

    Browser --> OAuthFilter
    CLI --> TokenFilter
    WSClient --> STOMP

    OAuthFilter --> AuthCheck
    TokenFilter --> AuthCheck
    NoAuth -.-> AuthCheck

    AuthCheck -->|Yes| PermCheck
    AuthCheck -->|No| Reject["401 Unauthorized"]
    PermCheck -->|Allowed| Endpoints
    PermCheck -->|Denied| Forbidden["403 Forbidden"]

    GuestEP --> K8sSvc
    BuildEP --> K8sSvc
    DeployEP --> K8sSvc
    AcctEP --> AcctSvc
    PodEP --> K8sSvc
```

**Permission Model:**
- Permissions are stored in the `Account` CRD spec as a list of strings (e.g., `guest.build.list`, `user.**`)
- The `PermissionTree` class implements hierarchical matching — `**` acts as a wildcard granting all sub-permissions
- The first user to log in is automatically granted admin (`**`) permissions
- The `CheckUserAuthorized` Spring AOP aspect intercepts every `@UserAuthorized` method call and validates permissions before execution

---

### 8. Kubernetes Deployment Topology

This diagram shows how the operator itself is deployed and what cluster resources it manages.

```mermaid
graph TB
    subgraph Internet["Internet"]
        Client["External Client"]
    end

    subgraph Cluster["Kubernetes Cluster"]
        subgraph IngressLayer["Ingress Layer"]
            IstioGW["Istio Gateway<br/>(innkeeper-gw)"]
            HTTPRoute["HTTPRoute<br/>→ innkeeper-svc:8081"]
        end

        subgraph OperatorNamespace["Namespace: innkeeper"]
            SA["ServiceAccount<br/>(innkeeper)"]
            CRB["ClusterRoleBinding<br/>→ cluster-admin"]
            OperatorDeploy["Deployment<br/>(innkeeper, 1 replica)"]
            OperatorSvc["Service<br/>(:8081)"]
            OperatorPod["Operator Pod<br/>Java 19 / Spring Boot"]
        end

        subgraph ManagedResources["Managed Resources (any namespace)"]
            GuestCRs["Guest CRs"]
            BuildCRs["Build CRs"]
            BuildJobs["Build Jobs<br/>(git-builder)"]
            AppDeploys["Application<br/>Deployments"]
            AppSvcs["Application<br/>Services"]
            AppRoutes["Application<br/>Gateways & Routes"]
        end

        subgraph ClusterCRDs["Cluster-Scoped CRDs"]
            CRD1["guests.cicd.innkeeper.run"]
            CRD2["builds.cicd.innkeeper.run"]
            CRD3["deployments.cicd.innkeeper.run"]
            CRD4["services.cicd.innkeeper.run"]
            CRD5["simpleextensions.cicd.innkeeper.run"]
            CRD6["accounts.cicd.innkeeper.run"]
        end
    end

    Client --> IstioGW
    IstioGW --> HTTPRoute
    HTTPRoute --> OperatorSvc
    OperatorSvc --> OperatorPod
    OperatorPod --> SA
    SA --> CRB

    OperatorPod -->|watches & reconciles| ClusterCRDs
    OperatorPod -->|creates & manages| GuestCRs
    OperatorPod -->|creates & manages| BuildCRs
    OperatorPod -->|creates| BuildJobs
    OperatorPod -->|creates| AppDeploys
    OperatorPod -->|creates| AppSvcs
    OperatorPod -->|creates| AppRoutes
```

The operator requires `cluster-admin` privileges because it manages CRDs, Jobs, Deployments, Services, and Gateway API resources across all namespaces.

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 19 |
| **Build System** | Gradle | Groovy DSL |
| **Operator Framework** | Java Operator SDK (JOSDK) | 4.2.8 |
| **Kubernetes Client** | Fabric8 | 6.7.1 |
| **Web Framework** | Spring Boot | 3.0.6 |
| **Security** | Spring Security + OAuth2 (Google) | 6.1.0 |
| **API Documentation** | SpringDoc OpenAPI / Swagger UI | 2.0.4 |
| **Service Mesh** | Istio Client | 1.7.7.1 |
| **CRD Generation** | Fabric8 CRD Generator APT | 6.5.1 |
| **Container Runtime** | Docker (OpenJDK 19 on OracleLinux 8) | — |
| **CI/CD** | GitHub Actions | — |
| **Container Registry** | GitHub Container Registry (GHCR) | — |
| **Testing** | JUnit | 5.9.2 |

---

## Project Structure

```
java-operator/
├── src/main/java/run/innkeeper/
│   ├── Main.java                        # Entry point — registers CRDs, boots EventBus
│   ├── api/                             # REST API layer
│   │   ├── annotations/                 #   @UserAuthorized, @WebSocketUserAuthorized
│   │   ├── config/                      #   Security, WebSocket, Swagger config
│   │   ├── dto/                         #   API response DTOs
│   │   ├── endpoints/                   #   REST controllers (Guest, Build, Deploy, etc.)
│   │   └── services/                    #   WebSocket session storage
│   ├── v1/                              # CRD definitions + Reconcilers
│   │   ├── guest/                       #   Guest CRD, spec, status, reconciler
│   │   ├── build/                       #   Build CRD, spec, status, reconciler
│   │   ├── deployment/                  #   Deployment CRD, spec, status, reconciler
│   │   ├── service/                     #   Service CRD, spec, status, reconciler
│   │   ├── simpleExtensions/            #   SimpleExtension CRD, spec, status, reconciler
│   │   └── account/                     #   Account CRD, spec, status, reconciler
│   ├── buses/                           # Service buses — Kubernetes resource managers
│   │   ├── EventBus.java               #   Reflection-based event dispatcher
│   │   ├── BuildBus.java               #   Build job orchestration (extends JobBus)
│   │   ├── GitBus.java                 #   Git change detection (extends JobBus)
│   │   ├── JobBus.java                 #   Abstract Kubernetes Job manager
│   │   ├── DeploymentBus.java          #   K8s Deployment CRUD
│   │   ├── ServiceBus.java             #   K8s Service CRUD
│   │   └── ExtensionBus.java           #   Extension routing
│   ├── controllers/                     # Event handlers (annotated with @Trigger)
│   │   ├── BuildController.java
│   │   ├── DeploymentController.java
│   │   ├── GuestController.java
│   │   ├── ServiceController.java
│   │   └── SimpleExtensionController.java
│   ├── events/                          # Event class definitions
│   │   ├── structure/                   #   Base classes, @Trigger annotation
│   │   ├── actions/                     #   Action events (StartBuild, CreateDeployment, etc.)
│   │   └── builds/, deployments/, ...   #   Domain-specific events
│   ├── extensions/                      # Extension implementations
│   │   └── gateway/                     #   BackendProxy, IstioHttpRoute
│   ├── services/                        # Core singletons
│   │   ├── K8sService.java             #   Kubernetes client wrapper
│   │   └── AccountService.java         #   User + permission management
│   ├── permission/                      # Permission tree model
│   └── utilities/                       # Logging, hashing, build monitoring
├── src/main/resources/
│   ├── application.yaml                 # Spring Boot config (port, OAuth2, Swagger)
│   └── logback.xml                      # Logging config
├── build.gradle                         # Gradle build script
├── Dockerfile                           # Multi-stage Docker build
├── testfiles/
│   ├── innkeeper.yaml                   # Example Guest resource
│   └── operator.yml                     # K8s deployment manifest for the operator
└── .github/workflows/
    └── docker-image.yml                 # CI — build & push to GHCR
```
