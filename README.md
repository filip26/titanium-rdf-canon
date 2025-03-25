# Titanium RDF Dataset Canonicalization

An implementation of the [W3C Standard RDF Dataset Canonicalization Algorithm (RDFC 1.0)](https://www.w3.org/TR/rdf-canon/) in Java. 

[![Java 8 CI](https://github.com/filip26/titanium-rdf-canon/actions/workflows/java8-build.yml/badge.svg)](https://github.com/filip26/titanium-rdf-canon/actions/workflows/java8-build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8682ccd0fb314ee8a237462c25373686)](https://app.codacy.com/gh/filip26/titanium-rdf-canon/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/8682ccd0fb314ee8a237462c25373686)](https://app.codacy.com/gh/filip26/titanium-rdf-canon/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/titanium-rdfc.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.apicatalog%20AND%20a:titanium-rdfc)
[![javadoc](https://javadoc.io/badge2/com.apicatalog/titanium-rdfc/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/titanium-rdfc)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

For conformance with the specification, refer to the [RDF Dataset Canonicalization and Hash 1.0 Processor Conformance](https://w3c.github.io/rdf-canon/reports/) report.

## Examples

```javascript
// Create a new canonicalizer instance
var canon = RdfCanon.create("SHA-256");

// Feed the canonicalizer with N-Quads
(new NQuadsReader(...)).provide(canon);

// Alternatively, manually add quads
canon.quad(...).quad(..)...quad(...);

// Get the canonicalized result
canon.provide(...);

// Get the canonicalized result as N-Quads
var writer = new NQuadsWriter(...);
canon.provide(writer);

// Access data related to the canonicalization process
var mapping = canon.mapping();

```

Use `RdfCanonTicker` to prematurely terminate computation.

```javascript
// Set a 5-second timeout (in milliseconds) after which computation will be terminated
var canon = RdfCanon.create("SHA-256", new RdfCanonTimeTicker(5 * 1000));

// Alternatively, set a custom ticker
var canon = RdfCanon.create("SHA-256", () -> {
    // This will be called during computation, starting from the very beginning
});
```

## Installation

### Maven
```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>titanium-rdfc</artifactId>
    <version>2.0.0</version>
</dependency>

```

### Gradle

```gradle
implementation("com.apicatalog:titanium-rdfc:2.0.0")
```

## Contributing

All PR's welcome!

### Building

Fork and clone the project repository.

```bash
> cd titanium-rdfc
> mvn package
```

## Resources
* [W3C Standard RDF Dataset Canonicalization Algorithm](https://www.w3.org/TR/rdf-canon/)
* [Titanium JSON-LD](https://github.com/filip26/titanium-json-ld)
* [Titanium N-QUADS](https://github.com/filip26/titanium-rdf-n-quads)
* Originally a fork of [RDF-URDNA](https://github.com/setl/rdf-urdna).
