# Titanium RDF Dataset Canonicalization

An implementation of the [W3C Standard RDF Dataset Canonicalization Algorithm](https://www.w3.org/TR/rdf-canon/) in Java. Originally a fork of [RDF-URDNA](https://github.com/setl/rdf-urdna).

[![Java 11 CI](https://github.com/filip26/titanium-rdfc/actions/workflows/java11-build.yml/badge.svg?branch=main)](https://github.com/filip26/titanium-rdfc/actions/workflows/java11-build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8682ccd0fb314ee8a237462c25373686)](https://app.codacy.com/gh/filip26/titanium-rdfc/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/8682ccd0fb314ee8a237462c25373686)](https://app.codacy.com/gh/filip26/titanium-rdfc/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Examples

```javascript

// Simple canonicalization
Collection<RdfNQuad> norm = RdfCanonicalizer.canonicalize(Collection<RdfNQuad>);

// Get an access to data related to canonicalization process.
var canon = RdfCanonicalizer.canonicalize(Collection<RdfNQuad>);
var norm = canon.canonicalize();
var labels = canon.issuer().mappingTable();
```

## Installation

### Maven
```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>titanium-rdfc</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>titanium-json-ld</artifactId>
    <version>1.4.1</version>
</dependency>

<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.json</artifactId>
    <version>2.0.1</version>
</dependency>
```


## Documentation

[![javadoc](https://javadoc.io/badge2/com.apicatalog/titanium-rdfc/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/titanium-rdfc)

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

