# Gizmo

A bytecode generation library.

[![Version](https://img.shields.io/maven-central/v/io.quarkus.gizmo/gizmo?logo=apache-maven&style=for-the-badge)](https://search.maven.org/artifact/io.quarkus.gizmo/gizmo)
[![GitHub Actions Status](<https://img.shields.io/github/actions/workflow/status/quarkusio/gizmo/maven.yml?branch=main&logo=GitHub&style=for-the-badge>)](https://github.com/quarkusio/gizmo/actions?query=workflow%3A%22Gizmo+CI%22)

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

To release a new version, follow these steps:

https://github.com/smallrye/smallrye/wiki/Release-Process#releasing

The staging repository is automatically closed. The sync with Maven Central should take ~30 minutes.
