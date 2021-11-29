# Gizmo

A bytecode generation library...

![](https://github.com/quarkusio/gizmo/workflows/Gizmo%20CI/badge.svg)
 
## Build

`mvn clean install`

## Release

```bash
# Bump version and create the tag
mvn release:prepare -Prelease
# Build the tag and push to OSSRH
mvn release:perform -Prelease
```

The staging repository is automatically closed. The sync with Maven Central should take ~30 minutes.
