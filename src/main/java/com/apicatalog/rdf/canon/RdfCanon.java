package com.apicatalog.rdf.canon;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.api.RdfQuadConsumer;

/**
 * A Standard RDF Dataset Canonicalization Algorithm
 * 
 * @see <a href="https://www.w3.org/TR/rdf-canon/">W3C Standard RDF Dataset
 *      Canonicalization Algorithm</a>
 */
public class RdfCanon implements RdfQuadConsumer {

    interface Ticker {
        void tick() throws IllegalStateException;
    }

    /**
     * The lower-case hexadecimal alphabet.
     */
    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final RdfResource BLANK_A = Rdf.createBlankNode("_:a");

    private static final RdfResource BLANK_Z = Rdf.createBlankNode("_:z");

    /** Map of blank IDs to all the quads that reference that specific blank ID. */
    protected final Map<String, Collection<RdfNQuad>> blankIdToQuadSet;

    protected final Map<String, RdfResource> resources;

    /** Issuer of canonical IDs to blank nodes. */
    private final IdentifierIssuer canonIssuer = new IdentifierIssuer("_:c14n");

    /**
     * Hash to associated IRIs.
     */
    private final Map<String, Set<String>> hashToBlankId = new TreeMap<>();

    /** All the n-quads in the dataset to be processed. */
    private final List<RdfNQuad> nquads;

    /** An instance of the SHA-256 message digest algorithm. */
    private final MessageDigest sha256;

    /** A set of non-normalized values. */
    private HashSet<String> nonNormalized;

    protected RdfCanon(Map<String, Collection<RdfNQuad>> blankIdToQuadSet, Map<String, RdfResource> resources, MessageDigest sha256, List<RdfNQuad> nquads) {
        this.blankIdToQuadSet = blankIdToQuadSet;
        this.resources = resources;
        this.sha256 = sha256;
        this.nquads = nquads;
    }

    public void provide(RdfQuadConsumer consumer) throws RdfConsumerException {

        System.out.println(nquads);

        // Step 3:
        setNonNormalized();

        // Steps 4 and 5:
        issueSimpleIds();

        // Step 6:
        issueNDegreeIds();

        // Step 7:
        makeCanonQuads(consumer);
    }

//    public void accept(final RdfNQuad nquad) {
//
//        MutableBlankNode[] blankNodes = new MutableBlankNode[4];
//        boolean newNQuad = false;
//
//        for (Position position : Position.CAN_BE_BLANK) {
//            if (position.isBlank(nquad)) {
//                String blankNodeId = position.get(nquad);
//                RdfValue blankNode = resources.get(blankNodeId);
//                if (!(blankNode instanceof MutableBlankNode)) {
//                    blankNodes[position.index()] = getMutableBlankNode(blankNode.getValue());
//                    newNQuad = true;
//
//                } else {
//                    blankNodes[position.index()] = (MutableBlankNode) blankNode;
//                }
//            }
//        }
//
//        RdfNQuad clone = newNQuad
//                ? Rdf.createNQuad(
//                        blankNodes[0] != null
//                                ? blankNodes[0]
//                                : nquad.getSubject(),
//                        nquad.getPredicate(),
//                        blankNodes[2] != null
//                                ? blankNodes[2]
//                                : nquad.getObject(),
//                        blankNodes[3] != null
//                                ? blankNodes[3]
//                                : nquad.getGraphName().orElse(null))
//                : nquad;
//
//        for (MutableBlankNode blankNode : blankNodes) {
//            if (blankNode != null) {
//                blankIdToQuadSet.computeIfAbsent(
//                        blankNode.getValue(),
//                        k -> new LinkedList<>()).add(clone);
//            }
//        }
//
//        nquads.add(clone);
//    }

    public static RdfCanon create() {
        return newInstance(new ArrayList<>());
    }

    protected static RdfCanon newInstance(List<RdfNQuad> nquads) {
        try {
            return new RdfCanon(
                    new HashMap<>(),
                    new HashMap<>(),
                    MessageDigest.getInstance("SHA-256"),
                    nquads);

        } catch (NoSuchAlgorithmException e) {
            // The Java specification requires SHA-256 is included, so this should never
            // happen.
            throw new InternalError("SHA-256 is not available", e);
        }
    }

    protected String forBlank(RdfNQuad q0, String blankNodeId) {

        RdfResource subject = q0.getSubject();
        if (subject.isBlankNode()) {
            // A blank node is always a resource
            subject = subject.getValue().equals(blankNodeId) ? BLANK_A : BLANK_Z;
        }

        RdfValue object = q0.getObject();
        if (object.isBlankNode()) {
            object = object.getValue().equals(blankNodeId) ? BLANK_A : BLANK_Z;
        }

        Optional<RdfResource> graph = q0.getGraphName();
        if (graph.isPresent()) {
            RdfResource g = graph.get();
            if (g.isBlankNode()) {
                graph = Optional.of(g.getValue().equals(blankNodeId) ? BLANK_A : BLANK_Z);
            }
        }

        // TODO
//        NQuadsWriter.nquad(subject, q0.getPredicate(), object, graph.orElse(null));

        return Rdf.createNQuad(subject, q0.getPredicate(), object, graph.orElse(null)).toString() + '\n';
    }

    protected void setNonNormalized() {
        nonNormalized = new HashSet<>(blankIdToQuadSet.keySet());
    }

    protected String hashFirstDegree(final String blankNodeId) {

        Collection<RdfNQuad> related = blankIdToQuadSet.get(blankNodeId);
        String[] nQuads = new String[related.size()];
        int i = 0;

        // Convert the NQuads to a consistent set by replacing the reference with _:a
        // and all others with _:z, and then sorting
        for (RdfNQuad q0 : related) {
            nQuads[i] = forBlank(q0, blankNodeId);
            i++;
        }

        // Sort the nQuads
        Arrays.sort(nQuads);

        // Create the hash
        sha256.reset();
        for (String s : nQuads) {
            sha256.update(s.getBytes(StandardCharsets.UTF_8));
        }
        return hex(sha256.digest());
    }

    protected void issueSimpleIds() {
        boolean simple = true;
        while (simple) {
            simple = false;
            hashToBlankId.clear();
            for (String value : nonNormalized) {
                String hash = hashFirstDegree(value);
                hashToBlankId.computeIfAbsent(hash, k -> new HashSet<>()).add(value);
            }

            Iterator<Entry<String, Set<String>>> iterator = hashToBlankId.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Set<String>> entry = iterator.next();
                Set<String> values = entry.getValue();
                if (values.size() == 1) {
                    String id = values.iterator().next();
                    // allocate a new id
                    canonIssuer.getId(id);
                    nonNormalized.remove(id);
                    iterator.remove();
                    simple = true;
                }
            }
        }
    }

    protected void issueNDegreeIds() {

        int counter = 0;

        for (Entry<String, Set<String>> entry : hashToBlankId.entrySet()) {
            List<NDegreeResult> hashPathList = new ArrayList<>();
            for (String id : entry.getValue()) {
                if (counter > 10) {
                    throw new IllegalStateException();
                }
                counter++;
                // if we've already assigned a canonical ID for this node, skip it
                if (canonIssuer.hasId(id)) {
                    continue;
                }

                // Create a new blank ID issuer and assign it's first ID to the reference id
                IdentifierIssuer blankIssuer = new IdentifierIssuer("_:b");
                blankIssuer.getId(id);

                NDegreeResult path = hashNDegreeQuads(id, blankIssuer);
                hashPathList.add(path);
            }

//            hashPathList.sort(Comparator.naturalOrder());
//            for (NDegreeResult result : hashPathList) {
//                if (counter > 1000) {
//                    throw new IllegalStateException();
//                }
//                counter++;
//
//                result.getIssuer().assign(canonIssuer);
//            }
        }
    }

    protected void makeCanonQuads(RdfQuadConsumer consumer) throws RdfConsumerException {
        System.out.println(nquads);
        // relabel blank nodes
        for (String blankNodeId : blankIdToQuadSet.keySet()) {

            MutableBlankNode blankNode = (MutableBlankNode) resources.get(blankNodeId);

            blankNode.setValue(canonIssuer.getIfExists(blankNodeId));
        }

        Collections.sort(nquads, RdfNQuadComparator.asc());

        for (RdfNQuad nquad : nquads) {

            if (nquad.getObject().isLiteral()) {

            } else {
                consumer.quad(nquad.getSubject().getValue(), nquad.getPredicate().getValue(), nquad.getObject().getValue(), nquad.getGraphName().map(RdfResource::getValue).orElse(null));
            }
        }
        ;
    }

    /**
     * Convert bytes to hexadecimal.
     *
     * @param data the bytes
     *
     * @return the data represented in hexadecimal.
     */
    static String hex(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 2);
        for (byte b : data) {
            builder.append(HEX[(b & 0xf0) >> 4]).append(HEX[b & 0xf]);
        }
        return builder.toString();
    }

    private NDegreeResult hashNDegreeQuads(String id, IdentifierIssuer issuer) {
        return new HashNDegreeQuads().hash(id, issuer);
    }

    /**
     * The state information for the hash n-degree quads algorithm.
     */
    private class HashNDegreeQuads {

        /** The data which will go into the hash. */
        final StringBuilder dataToHash = new StringBuilder();

        /** The currently chosen identifier issuer. */
        IdentifierIssuer chosenIssuer = null;

        /** The currently chosen path. */
        StringBuilder chosenPath = null;

        int counter = 0;

        /**
         * Append an ID to the hash path.
         *
         * @param related       the ID to append
         * @param pathBuilder   the path to append to
         * @param issuerCopy    the identifier issuer
         * @param recursionList the node recursion list
         */
        private void appendToPath(RdfResource related, StringBuilder pathBuilder, IdentifierIssuer issuerCopy, List<MutableBlankNode> recursionList) {
            if (canonIssuer.hasId(related.getValue())) {
                // 5.4.4.1: Already has a canonical ID so we just use it.
                pathBuilder.append(canonIssuer.getId(related.getValue()));
            } else {
                // 5.4.4.2: Need to try an ID, and possibly recurse
                if (!issuerCopy.hasId(related.getValue())) {
                    recursionList.add((MutableBlankNode) related);
                }
                pathBuilder.append(issuerCopy.getId(related.getValue()));
            }
        }

        /**
         * Implementation of steps 1 to 3 of the Hash N-Degree Quads algorithm.
         *
         * @param id     the ID of the blank node to process related nodes for
         * @param issuer the ID issuer currently being used.
         *
         * @return the required mapping
         */
        private SortedMap<String, Set<String>> createHashToRelated(String id, IdentifierIssuer issuer) {
            SortedMap<String, Set<String>> hashToRelated = new TreeMap<>();
            // quads that refer to the blank node.
            Collection<RdfNQuad> refer = blankIdToQuadSet.get(id);
            for (RdfNQuad quad : refer) {
                // find all the blank nodes that refer to this node by a quad
                for (Position position : Position.CAN_BE_BLANK) {
                    if (position.isBlank(quad) && !id.equals(position.get(quad))) {
                        String related = position.get(quad);
                        String hash = hashRelatedBlankNode(related, quad, issuer, position);
                        hashToRelated.computeIfAbsent(hash, h -> new HashSet<>()).add(related);
                    }
                }
            }
            return hashToRelated;
        }

        /**
         * Process one possible permutation of the blank nodes.
         *
         * @param permutation the permutation
         * @param issuer      the identifier issuer
         */
        private void doPermutation(String[] permutation, IdentifierIssuer issuer) {

            if (counter > 100) {
                throw new IllegalStateException();
            }
            counter++;

            // 5.4.1 to 5.4.3 : initialize variables
            IdentifierIssuer issuerCopy = issuer.copy();
            StringBuilder pathBuilder = new StringBuilder();
            List<MutableBlankNode> recursionList = new ArrayList<>();

            // 5.4.4: for every resource in the this permutation of the resources
            for (final String relatedId : permutation) {

                RdfResource related = resources.get(relatedId);

                appendToPath(related, pathBuilder, issuerCopy, recursionList);

                // 5.4.4.3: Is this path better than our chosen path?
                if (chosenPath.length() > 0 && pathBuilder.toString().compareTo(chosenPath.toString()) > 0) {
                    // This is permutation is not going to make the best path, so skip the rest of
                    // it
                    return;
                }
            }

            // 5.4.5: Process the recursion list
            for (MutableBlankNode related : recursionList) {
                NDegreeResult result = hashNDegreeQuads(related.getValue(), issuerCopy);

                pathBuilder
                        .append(issuerCopy.getId(related.getValue()))
                        .append('<')
                        .append(result.getHash())
                        .append('>');
                issuerCopy = result.getIssuer();

                if (chosenPath.length() > 0 && pathBuilder.toString().compareTo(chosenPath.toString()) > 0) {
                    // This is permutation is not going to make the best path, so skip the rest of
                    // it
                    return;
                }
            }

            // 5.4.6: Do we have a new chosen path?
            if (chosenPath.length() == 0 || pathBuilder.toString().compareTo(chosenPath.toString()) < 0) {
                chosenPath.setLength(0);
                chosenPath.append(pathBuilder);
                chosenIssuer = issuerCopy;
            }
        }

        /**
         * Calculate the hash from the N-Degree nodes.
         *
         * @param bid           the blank node starting ID
         * @param defaultIssuer the identifier issuer
         *
         * @return the result
         */
        NDegreeResult hash(final String id, final IdentifierIssuer defaultIssuer) {

            IdentifierIssuer issuer = defaultIssuer;

            SortedMap<String, Set<String>> hashToRelated = createHashToRelated(id, defaultIssuer);

            for (Entry<String, Set<String>> entry : hashToRelated.entrySet()) {
                // 5.1 to 5.3: Append the hash for the related item to the hash we are building
                // and initialise variables
                dataToHash.append(entry.getKey());
                chosenPath = new StringBuilder();
                chosenIssuer = null;

                // 5.4: For every possible permutation of the blank node list...
                Permutator permutator = new Permutator(entry.getValue().toArray(new String[entry.getValue().size()]));
                while (permutator.hasNext()) {
                    doPermutation(
                            permutator.next(),
                            issuer);
                }

                // 5.5: Append chosen path to the hash
                dataToHash.append(chosenPath);
                issuer = chosenIssuer;
            }

            sha256.reset();
            String hash = hex(sha256.digest(dataToHash.toString().getBytes(StandardCharsets.UTF_8)));
            return new NDegreeResult(hash, issuer);
        }

        /**
         * Create a hash of the related blank nodes, as described in the specification.
         *
         * @param related  the ID nodes are related to
         * @param quad     the quad to process
         * @param issuer   the identifier issuer
         * @param position the position in the quad
         *
         * @return the hash
         */
        private String hashRelatedBlankNode(
                String related,
                RdfNQuad quad,
                IdentifierIssuer issuer,
                Position position) {
            // Find an ID for the blank ID
            String id;
            if (canonIssuer.hasId(related)) {
                id = canonIssuer.getId(related);
            } else if (issuer.hasId(related)) {
                id = issuer.getId(related);
            } else {
                id = hashFirstDegree(related);
            }

            // Create the hash of position, predicate and ID.
            sha256.reset();
            sha256.update(position.tag());
            if (position != Position.GRAPH) {
                sha256.update(quad.getPredicate().toString().getBytes(StandardCharsets.UTF_8));
            }
            sha256.update(id.getBytes(StandardCharsets.UTF_8));
            return hex(sha256.digest());
        }
    }

    @Override
    public RdfQuadConsumer quad(String subject, String predicate, String object, String graph) {
        System.out.println("> " + subject + ", " + predicate + ", " + object + ", " + graph);

        RdfResource rdfSubject = getResource(subject);
        RdfResource rdfObject = getResource(object);
        RdfResource rdfGraph = graph != null ? getResource(graph) : null;

        RdfNQuad quad = Rdf.createNQuad(
                rdfSubject,
                getResource(predicate),
                rdfObject,
                rdfGraph);

        if (rdfSubject.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfSubject.getValue(), k -> new LinkedList<>()).add(quad);
        }

        if (rdfObject.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfObject.getValue(), k -> new LinkedList<>()).add(quad);
        }

        if (rdfGraph != null && rdfGraph.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfGraph.getValue(), k -> new LinkedList<>()).add(quad);
        }

        nquads.add(quad);

        return this;
    }

    @Override
    public RdfQuadConsumer quad(String subject, String predicate, String literal, String datatype, String graph) {
        RdfResource rdfSubject = getResource(subject);
        RdfResource rdfGraph = graph != null ? getResource(graph) : null;

        RdfNQuad quad = Rdf.createNQuad(
                rdfSubject,
                getResource(predicate),
                Rdf.createTypedString(literal, datatype),
                rdfGraph);

        if (rdfSubject.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfSubject.getValue(), k -> new LinkedList<>()).add(quad);
        }

        if (rdfGraph != null && rdfGraph.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfGraph.getValue(), k -> new LinkedList<>()).add(quad);
        }

        nquads.add(quad);

        return this;
    }

    @Override
    public RdfQuadConsumer quad(String subject, String predicate, String literal, String language, String direction, String graph) {
        RdfResource rdfSubject = getResource(subject);
        RdfResource rdfGraph = graph != null ? getResource(graph) : null;

        RdfNQuad quad = Rdf.createNQuad(
                rdfSubject,
                getResource(predicate),
                Rdf.createLangString(literal, language, direction),
                rdfGraph);

        if (rdfSubject.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfSubject.getValue(), k -> new LinkedList<>()).add(quad);
        }

        if (rdfGraph != null && rdfGraph.isBlankNode()) {
            blankIdToQuadSet.computeIfAbsent(rdfGraph.getValue(), k -> new LinkedList<>()).add(quad);
        }

        nquads.add(quad);

        return this;
    }

    protected final RdfResource getResource(final String name) {
        return resources.computeIfAbsent(name, arg0 -> name.startsWith("_:")
                ? new MutableBlankNode(name)
                : Rdf.createIRI(name));
    }

    protected final MutableBlankNode getMutableBlankNode(final String value) {
        return (MutableBlankNode) resources.computeIfAbsent(value, arg0 -> new MutableBlankNode(value));
    }

    public Map<String, String> mappingTable() {
        return canonIssuer.mappingTable();
    }
}
