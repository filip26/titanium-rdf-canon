/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apicatalog.rdf.canon;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

class EarlGenerator {

    static final String FILE_NAME = "titanium-rdf-canon-earl.ttl";
    static final String VERSION = "2.0.0";
    static final String RELEASE_DATE = "2025-03-09";
    static final String REPOSITORY = "https://github.com/filip26/titanium-rdf-canon";

    public static void main(String[] args) throws IOException {
        (new EarlGenerator()).generate(Paths.get(FILE_NAME));
    }

    public void generate(final Path path) throws IOException {

        final Stream<RdfCanonTestCase> tests = RdfCanonTest.data();

        try (PrintWriter writer = new PrintWriter(path.toFile())) {

            printHeader(writer);

            tests.forEach(test -> {
                try {
                    RdfCanonTest.execute(RdfCanon.create(test.hashAlgorithm, new RdfCanonTimeTicker(300)), test);
                    printResult(writer, "https://w3c.github.io/rdf-canon/tests/manifest" + test.id, true);

                } catch (Throwable e) {
                    printResult(writer, test.id, false);
                }
            });
        }
    }

    static void printResult(PrintWriter writer, String testUri, boolean passed) {

        if (!passed) {
            System.out.println("Failed: " + testUri);
        }

        writer.println();
        writer.println("[ a earl:Assertion ;");
        writer.println("  earl:assertedBy <https://apicatalog.com> ;");
        writer.println("  earl:subject <" + REPOSITORY + "> ;");
        writer.println("  earl:test <" + testUri + "> ;");
        writer.println("  earl:result [");
        writer.println("    a earl:TestResult ;");
        writer.println("    earl:outcome " + (passed ? "earl:passed" : "earl:failed") + " ;");
        writer.println("    dc:date \"" + DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS)) + "\"^^xsd:dateTime ;");
        writer.println("  ] ;");
        writer.println("  earl:mode earl:automatic ;");
        writer.println("] .");
    }

    void printHeader(PrintWriter writer) {

        writer.println("@prefix dc: <http://purl.org/dc/terms/> .");
        writer.println("@prefix doap: <http://usefulinc.com/ns/doap#> .");
        writer.println("@prefix foaf: <http://xmlns.com/foaf/0.1/> .");
        writer.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        writer.println("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
        writer.println("@prefix earl: <http://www.w3.org/ns/earl#> .");
        writer.println();
        writer.println("<> foaf:primaryTopic <" + REPOSITORY + "> ;");
        writer.println("  dc:issued \"" + DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS)) + "\"^^xsd:dateTime ;");
        writer.println("  foaf:maker <https://apicatalog.com> .");
        writer.println();
        writer.println("<" + REPOSITORY + "> a doap:Project, earl:TestSubject, earl:Software ;");
        writer.println("  doap:name \"Titanium RDFC\" ;");
        writer.println("  doap:description \"RDF Dataset Canonicalization (RDFC 1.0) in Java\"@en ;");
        writer.println("  doap:organization <https://apicatalog.com> ;");
        writer.println("  doap:developer <https://github.com/filip26> ;");
        writer.println("  doap:homepage <" + REPOSITORY + "> ;");
        writer.println("  doap:license <" + REPOSITORY + "/blob/main/LICENSE> ;");
        writer.println("  doap:release [");
        writer.println("    doap:name \"titanium-rdfc:" + VERSION + "\" ;");
        writer.println("    doap:revision \"" + VERSION + "\" ;");
        writer.println("    doap:created \"" + RELEASE_DATE + "\"^^xsd:date ;");
        writer.println("  ] ;");
        writer.println("  doap:programming-language \"Java\" .");
        writer.println();
        writer.println("<https://apicatalog.com> a foaf:Organization, earl:Assertor ;");
        writer.println("  foaf:name \"APICatalog\" ;");
        writer.println("  foaf:homepage <apicatalog.com> .");
        writer.println();
        writer.println("<https://github.com/filip26> a foaf:Person, earl:Assertor ;");
        writer.println("  foaf:name \"Filip Kolarik\" ;");
        writer.println("  foaf:homepage <https://github.com/filip26> .");
    }
}
