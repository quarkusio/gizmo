# Gizmo

A bytecode generation library.

[![Version](https://img.shields.io/maven-central/v/io.quarkus.gizmo/gizmo?logo=apache-maven&style=for-the-badge)](https://search.maven.org/artifact/io.quarkus.gizmo/gizmo)
[![GitHub Actions Status](<https://img.shields.io/github/workflow/status/QuarkusIO/gizmo/Gizmo CI?logo=GitHub&style=for-the-badge>)](https://github.com/quarkusio/gizmo/actions?query=workflow%3A%22Gizmo+CI%22)

## About

Gizmo aims at simplifying bytecode generation.
It is heavily used by [Quarkus](https://quarkus.io).
 
## Usage

For more information about how to use Gizmo, please see [USAGE.adoc](USAGE.adoc).

## Build

Gizmo is available on Maven Central but you can build it locally:

```bash
mvn clean install
```

## Release

```bash
# Bump version and create the tag
mvn release:prepare -Prelease
# Build the tag and push to OSSRH
mvn release:perform -Prelease
```

The staging repository is automatically closed. The sync with Maven Central should take ~30 minutes.
