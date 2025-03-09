package com.apicatalog.rdf.canon;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.nquads.NQuadsReader;
import com.apicatalog.rdf.nquads.NQuadsReaderException;
import com.apicatalog.rdf.nquads.NQuadsWriter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser;

@DisplayName("RDFC-1.0 Test Suite")
class RdfCanonTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testCanonize(RdfCanonTestCase testCase) throws IOException {
        execute(RdfCanon.create("SHA-256", new RdfCanonTimeTicker(2500)), testCase);
    }

    static final void execute(RdfCanon canon, RdfCanonTestCase testCase) throws IOException {
        try (final Reader reader = new InputStreamReader(RdfCanonTest.class.getResourceAsStream(testCase.input))) {
            new NQuadsReader(reader).provide(canon);
        } catch (NQuadsReaderException | RdfConsumerException e) {
            e.printStackTrace();
            fail(e);
        }

        try {
            final StringWriter writer = new StringWriter();

            canon.provide(new NQuadsWriter(writer));

            String result = writer.toString();
            assertNotNull(result);

            String expected = null;

            try (final InputStream is = RdfCanonTest.class.getResourceAsStream(testCase.expected)) {
                expected = isToString(is);
            }

            assertNotNull(expected);

            switch (testCase.type) {
            case RDFC10EvalTest:
                assertEval(testCase, expected, result);
                break;

            case RDFC10MapTest:
                assertMap(testCase, canon, expected, result);
                break;

            case RDFC10NegativeEvalTest:
                fail();
                break;
            }

        } catch (RdfConsumerException | IllegalStateException  e ) {
            if (RdfCanonTestCase.Type.RDFC10NegativeEvalTest != testCase.type) {
                fail(e);
            }
        }
    }

    static final void assertMap(RdfCanonTestCase testCase, RdfCanon canon, String expected, String result) {

        final Map<String, String> resultMap = canon.mappingTable();

        boolean match = false;

        try (final JsonReader reader = Json.createReader(new StringReader(expected))) {

            final JsonObject expectedMap = reader.readObject();

            assertNotNull(expectedMap);

            match = expectedMap.size() == resultMap.size();

            if (match) {
                for (final String key : expectedMap.keySet()) {
                    match = resultMap.containsKey("_:" + key)
                            && resultMap.get("_:" + key).equals("_:" + expectedMap.getString(key));

                    if (!match) {
                        break;
                    }
                }
            }
        }

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
            System.out.println(resultMap);
            System.out.println();
        }

        assertTrue(match);
    }

    static final void assertEval(RdfCanonTestCase testCase, String expected, String result) {

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

    static final String isToString(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1;) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }
}