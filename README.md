# 🌈 Bifröst

**A smart platform for building, deploying, and operating services — without Kubernetes-driven complexity.**

## 🚧 The Problem

Modern deployment tooling assumes every developer wants to become a **cluster engineer**.

In reality:

* Most applications do **not** need Kubernetes
* Most teams do **not** want to manage YAML
* Most developers want:

    * predictable builds
    * simple deployments
    * clear environments
    * observable services

**Docker already solves the majority of deployment needs.**
What’s missing is a **developer-friendly platform** that understands *applications*, *environments*, and *clusters* — without exposing unnecessary infrastructure details.

Kubernetes is powerful, but:

* it introduces operational and cognitive overhead
* it shifts focus away from application design
* it forces verbose, error-prone configuration

**Bifröst puts Docker first, hides Kubernetes when possible, and replaces YAML with structured, explicit configuration.**

## 🧠 What is Bifröst?

**Bifröst** is a **unified platform** for building, deploying, and operating applications across Docker and Kubernetes clusters — using:

* a **CLI**
* a **Web UI**
* a **REST API**

Bifröst is built around **three core concepts**:

* Clusters
* Applications
* Environments

These concepts separates the infrastructure (Clusters) from applications and deployments (environments).

## 🧩 Core Concepts

### 🖥️ Clusters

A **cluster** represents a deployment target.

Supported cluster types:

* Docker (local or remote)
* Kubernetes (optional, when required)

Clusters can be:

* local
* remote
* accessed through secure SSH tunnels, when needed

A cluster defines:

* container runtime
* networking capabilities
* resource constraints
* authentication and connectivity

Clusters are **infrastructure concerns**, not developer concerns.

### 📦 Applications

An **application** is a **logical definition** of a system made up of multiple services.

An application:

* consists of one or more services
* defines service requirements and dependencies
* is **environment-agnostic**
* is described in **JSON or XML**

Applications are **registered**, not deployed.

#### Application Registration

Applications can be registered by:

* register it with `bifrost` CLI
* uploading through the Web UI

The CLI can discover, list and describe registered applications.

```bash
bifrost app list
bifrost app describe my-app
```

This makes application definitions:

* versionable
* portable
* easy to review
* familiar to Java-centric teams

### 🌍 Environments

An **environment** represents a **deployment of an application**.

An environment:

* binds **one application** to **one cluster**
* defines environment-specific configuration
* is deployable, updatable, and observable

Examples:

* `dev`
* `staging`
* `prod`
* `customer-a`

An application can have **multiple environments**, but **each environment always targets a single cluster**.

```bash
bifrost env create dev --app my-app --cluster local-docker
```

## 🔁 Lifecycle Model

```
Application (definition)
        ↓
Environment (deployment)
        ↓
Cluster (execution)
```

This separation allows:

* safe reuse of application definitions
* consistent deployments across environments
* clear ownership boundaries

## 🚀 Key Features

### 🔧 Build & Package Services

* Java-first (Spring Boot, Micronaut, Quarkus)
* Language-agnostic by design
* Opinionated defaults with full override support
* Container images pushed to:

    * public registries (Docker Hub)
    * private registries

An Apache Maven plugin is available for Java projects to package and deploy.

### 📦 Deploy Applications (Not Containers)

Deploy **applications**, not individual containers.

```bash
bifrost deploy dev
```

Bifröst handles:

* service ordering
* dependencies
* restarts
* updates
* rollbacks (future)

No Compose files. No Helm charts. No YAML.

### 📊 Monitor from the CLI

Operational visibility without dashboards-first design.

```bash
bifrost status dev
bifrost logs dev service-a
bifrost metrics dev service-b
```

Includes:

* health checks
* container state
* restart history
* resource usage

### 🌉 Unified Docker & Kubernetes Platform

Bifröst treats Docker and Kubernetes as **execution backends**, not programming models:
* Same CLI commands.
* Same configuration model.
* Different runtime.

Developers do not need to know:

* Pods
* Deployments
* Services
* Helm
* CRDs

Unless absolutely necessary.

### 🔌 REST API for Automation & Integration

Everything exposed via:

* CLI
* Web UI
* REST API

Enables:

* CI/CD integration
* custom tooling
* third-party platforms
* internal automation

## 🧭 Philosophy

* **Docker first**
* **Kubernetes only when required**
* **No YAML**
* **Explicit configuration (JSON / XML)**
* **CLI-driven workflows**
* **Applications over infrastructure**

> Developers should build software.
> Bifröst handles the bridge.

## 📜 Name Origin

In Norse mythology, **Bifröst** is the bridge connecting Midgard and Asgard.

This project serves the same purpose:

> Bridging developers and infrastructure — safely, visibly, and without forcing them to live in the clouds.

## 🚧 Project Status

Bifröst is in **early development**.

Planned milestones:

* [ ] Core domain model (clusters, applications, environments)
* [ ] CLI discovery & registration
* [ ] Docker execution backend
* [ ] Application lifecycle management
* [ ] Monitoring primitives
* [ ] REST API
* [ ] Web UI
* [ ] Kubernetes backend (optional)

## 🤝 Contributing

Bifröst is opinionated by design. Contributions should reinforce:

* simplicity
* clarity
* developer-centric workflows