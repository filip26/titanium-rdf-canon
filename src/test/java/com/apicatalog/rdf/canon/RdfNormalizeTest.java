package com.apicatalog.rdf.canon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.canon.RdfCanonicalizer;

/**
 * A test-suite is defined at:
 * https://json-ld.github.io/normalization/tests/index.html#manifest-urdna2015.
 *
 * @author Simon Greatrix on 05/10/2020.
 */
public class RdfNormalizeTest {

    private void checkGraph(Collection<RdfNQuad> out, Collection<RdfNQuad> processed) {
        assertEquals(out.size(), processed.size(), "Datasets must be same size");
        for (RdfNQuad t : processed) {
            if (!out.contains(t)) {
                System.err.println("Generated nquad not found in expected output: " + t.toString());
                System.err.println("Possible matches:");
                for (RdfNQuad t0 : out) {
                    System.err.println(t0.toString());
                }
                fail();
            }
        }
    }

    @Test
    public void test() throws Exception {
        for (int i = 1; i <= 62; i++) {
            String fileIn = String.format("test%03d-in.nq", i);
            String fileOut = String.format("test%03d-urdna2015.nq", i);
            System.out.println("Processing " + fileIn);
            RdfDataset dataIn = Rdf.createReader(MediaType.N_QUADS, RdfNormalizeTest.class.getClassLoader().getResourceAsStream(fileIn)).readDataset();
            RdfDataset dataOut = Rdf.createReader(MediaType.N_QUADS, RdfNormalizeTest.class.getClassLoader().getResourceAsStream(fileOut)).readDataset();

            Collection<RdfNQuad> processed = RdfCanonicalizer.canonicalize(dataIn.toList());

            // processed and dataOut should be identical
            checkGraph(dataOut.toList(), processed);

            System.out.println("Processing " + fileIn + "   PASSED");
        }
    }
}