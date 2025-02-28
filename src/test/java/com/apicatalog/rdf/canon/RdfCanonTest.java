package com.apicatalog.rdf.canon;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.rdf.nquads.NQuadsReader;
import com.apicatalog.rdf.nquads.NQuadsWriter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser;

@DisplayName("RDFC-1.0 Test Suite")
class RdfCanonTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testCanonize(RdfCanonTestCase testCase) throws Exception {

        final RdfCanonicalizer canon = RdfCanonicalizer.newInstance();

        try (final Reader reader = new InputStreamReader(RdfCanonTest.class.getResourceAsStream(testCase.input))) {
            new NQuadsReader(reader).provide(canon);
        }

        final StringWriter writer = new StringWriter();
        canon.provide(new NQuadsWriter(writer));
        
        String result = writer.toString();
        assertNotNull(result);
        
        String expected = null;
        
        try (final InputStream is = RdfCanonTest.class.getResourceAsStream(testCase.expected)) {
            expected = isToString(is);
        }
        
        assertNotNull(expected);

        boolean match = expected.equals(result);
        
        if (!match) {
            System.out.println(testCase.id + ": " + testCase.name);
            System.out.print(testCase.type);
            if (testCase.comment != null) {
                System.out.println(" - " + testCase.comment);                
            } else {
                System.out.println();
            }
            
            System.out.println();
            System.out.println("Expected:");
            System.out.println(expected);
            System.out.println("Result:");
            System.out.println(result);
            System.out.println();
        }
        
        assertTrue(match);
        
//            System.out.println("Processing " + fileIn);
//            RdfDataset dataIn = Rdf.createReader(MediaType.N_QUADS, RdfCanonicalizerTest.class.getClassLoader().getResourceAsStream(fileIn)).readDataset();
//            RdfDataset dataOut = Rdf.createReader(MediaType.N_QUADS, RdfCanonicalizerTest.class.getClassLoader().getResourceAsStream(fileOut)).readDataset();
//
//            
//            RdfCanonicalizer can = RdfCanonicalizer.newInstance();
//
//            dataIn.toList().forEach(can::accept);
//
//            Collection<RdfNQuad> processed = can.canonize();
//            
////            Collection<RdfNQuad> processed = RdfCanonicalizer.canonize(dataIn.toList());
//
//            // processed and dataOut should be identical
//            assertEquals(dataOut.size(), processed.size(), "Datasets must be same size");
//            
//            assertTrue(checkGraph(dataOut.toList(), processed));

//            System.out.println("Processing " + fileIn + "   PASSED");
//        }
    }

    static final Stream<RdfCanonTestCase> data() throws IOException {
        return load("manifest.jsonld");
    }

    static final Stream<RdfCanonTestCase> load(String name) throws IOException {
        try (final InputStream is = RdfCanonTest.class.getResourceAsStream(name)) {
            final JsonParser parser = Json.createParser(is);

            parser.next();

            return parser
                    .getObject()
                    .getJsonArray("entries")
                    .stream()
                    .filter(v -> ValueType.OBJECT.equals(v.getValueType()))
                    .map(JsonObject.class::cast)
                    .map(RdfCanonTestCase::of);
        }
    }

    static boolean checkGraph(Collection<RdfNQuad> out, Collection<RdfNQuad> processed) {
        for (RdfNQuad t : processed) {
            if (!out.contains(t)) {
                System.err.println("Generated nquad not found in expected output:");
                System.err.println(t.toString());
                System.err.println("Possible matches:");
                for (RdfNQuad t0 : out) {
                    System.err.println(t0.toString());
                }
                return false;
            }
        }
        return true;
    }

    static final String isToString(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1;) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}