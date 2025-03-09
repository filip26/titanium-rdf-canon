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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.api.RdfQuadConsumer;
import com.apicatalog.rdf.nquads.NQuadsWriter;

/**
 * An implementation of the <em>Standard RDF Dataset Canonicalization
 * Algorithm</em> as defined by the W3C.
 * <p>
 * This class provides functionality to process and transform an RDF dataset
 * into its canonical form, ensuring a stable and deterministic representation.
 * Canonicalization is a key step in use cases such as digital signatures, data
 * comparison, and RDF graph normalization.
 *
 * @see <a href="https://www.w3.org/TR/rdf-canon/">W3C Standard RDF Dataset
 *      Canonicalization Algorithm</a>
 */
public final class RdfCanon implements RdfQuadConsumer {

    /**
     * The lower-case hexadecimal alphabet.
     */
    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final String BLANK_A = "_:a";

    private static final String BLANK_Z = "_:z";

    /** Map of blank IDs to all the quads that reference that specific blank ID. */
    private final Map<String, Collection<Quad>> blankIdToQuadSet;

    /**
     * A map of blank node identifiers to their corresponding {@link Blank} nodes,
     * used during RDF canonicalization for efficient relabeling of blank nodes.
     */
    private final Map<String, Blank> blankNodes;

    /** Issuer of canonical IDs to blank nodes. */
    private final IdentifierIssuer canonIssuer = new IdentifierIssuer("_:c14n");

    /**
     * Allows premature termination of the canonicalization process based on
     * criteria defined by the associated {@link RdfCanonTicker} instance.
     * <p>
     * The {@code ticker} can be used to monitor progress or enforce timeouts,
     * limits, or custom stopping conditions during RDF dataset canonicalization.
     */
    private final RdfCanonTicker ticker;

    /**
     * Hash to associated IRIs.
     */
    private final Map<String, Set<String>> hashToBlankId = new TreeMap<>();

    /** All the n-quads in the dataset to be processed. */
    private final Set<Quad> quads;

    /** An instance of a message digest algorithm (SHA-256, SHA-384, or custom). */
    private final MessageDigest digest;

    /** A set of non-normalized values. */
    private Set<String> nonNormalized;

    RdfCanon(Map<String, Collection<Quad>> blankIdToQuadSet, Map<String, Blank> resources, MessageDigest digest, Set<Quad> nquads, RdfCanonTicker ticker) {
        this.blankIdToQuadSet = blankIdToQuadSet;
        this.blankNodes = resources;
        this.digest = digest;
        this.quads = nquads;
        this.ticker = ticker;
    }

    /**
     * Creates a new instance of {@link RdfCanon} using the specified hash
     * algorithm.
     * <p>
     * The provided algorithm determines the cryptographic hash function used for
     * RDF canonicalization. Supported algorithms are:
     * <ul>
     * <li>{@code SHA-256}</li>
     * <li>{@code SHA-384}</li>
     * </ul>
     *
     * @param hashAlgorithm the name of the hash algorithm to use; must be either
     *                      {@code "SHA-256"} or {@code "SHA-384"}. Must not be
     *                      {@code null}.
     * @return a new {@link RdfCanon} instance configured with the specified hash
     *         algorithm.
     * @throws IllegalArgumentException if {@code hashAlgorithm} is not supported or
     *                                  {@code null}.
     */
    public static RdfCanon create(String hashAlgorithm) {
        return create(hashAlgorithm, RdfCanonTicker.EMPTY);
    }

    /**
     * Creates a new instance of {@link RdfCanon} using the specified hash algorithm
     * and {@link RdfCanonTicker}.
     * <p>
     * The provided algorithm determines the cryptographic hash function used for
     * RDF canonicalization. Supported algorithms are:
     * <ul>
     * <li>{@code SHA-256}</li>
     * <li>{@code SHA-384}</li>
     * </ul>
     * <p>
     * A {@link RdfCanonTicker} must also be provided to monitor the progress of the
     * canonicalization process or enforce custom stopping conditions. The ticker's
     * {@link RdfCanonTicker#tick()} method will be invoked periodically during
     * canonicalization. If {@code tick()} throws an {@link IllegalStateException},
     * the process will be aborted.
     *
     * @param hashAlgorithm the name of the hash algorithm to use; must be either
     *                      {@code "SHA-256"} or {@code "SHA-384"}. Must not be
     *                      {@code null}.
     * @param ticker        a non-null {@link RdfCanonTicker} used to monitor or
     *                      control the canonicalization process.
     * @return a new {@link RdfCanon} instance configured with the specified hash
     *         algorithm and ticker.
     * @throws IllegalArgumentException if {@code hashAlgorithm} is not supported or
     *                                  {@code null}.
     * @throws NullPointerException     if {@code ticker} is {@code null}.
     */
    public static RdfCanon create(String hashAlgorithm, RdfCanonTicker ticker) {
        try {
            return create(MessageDigest.getInstance(hashAlgorithm), ticker);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(hashAlgorithm + " is not available", e);
        }
    }

    /**
     * Creates a new instance of {@link RdfCanon} using the specified
     * {@link MessageDigest}.
     * <p>
     * This factory method allows the caller to provide a pre-configured
     * {@link MessageDigest} instance, offering greater flexibility and control over
     * the hashing behavior used during RDF canonicalization.
     * <p>
     * The supplied {@code digest} instance will be used internally to compute
     * hashes, and it should be properly initialized.
     *
     * @param digest a pre-configured {@link MessageDigest} instance; must not be
     *               {@code null}.
     * @return a new {@link RdfCanon} instance that uses the specified
     *         {@code digest}.
     * @throws NullPointerException if {@code digest} is {@code null}.
     */
    public static RdfCanon create(MessageDigest digest) {
        return create(digest, RdfCanonTicker.EMPTY);
    }

    /**
     * Creates a new {@link RdfCanon} instance configured with the provided
     * {@link MessageDigest} and {@link RdfCanonTicker}.
     * <p>
     * This factory method gives callers fine-grained control over the hashing
     * algorithm and behavior used during RDF dataset canonicalization by accepting
     * a pre-configured {@code MessageDigest} instance.
     * <p>
     * The supplied {@code digest} will be used internally to compute hashes for
     * canonicalizing RDF datasets. It must be properly initialized and ready for
     * use.
     * <p>
     * A {@code RdfCanonTicker} must also be provided to monitor the progress of
     * canonicalization or enforce custom stopping conditions, such as timeouts,
     * iteration limits, or external cancellation signals. The ticker’s
     * {@link RdfCanonTicker#tick()} method will be invoked periodically during the
     * canonicalization process. If it throws an {@link IllegalStateException},
     * processing will be interrupted.
     *
     * @param digest a pre-configured, non-null {@link MessageDigest} instance used
     *               for computing hashes.
     * @param ticker a non-null {@link RdfCanonTicker} that controls or monitors the
     *               canonicalization process.
     * @return a new {@link RdfCanon} instance configured with the specified
     *         {@code digest} and {@code ticker}.
     * @throws NullPointerException if {@code digest} or {@code ticker} is
     *                              {@code null}.
     */
    public static RdfCanon create(MessageDigest digest, RdfCanonTicker ticker) {
        return newInstance(new LinkedHashSet<>(), digest, ticker);
    }

    /**
     * Emits canonical RDF quads to the given consumer. This method generates RDF
     * quads in a canonical form and supplies them to the provided
     * {@link RdfQuadConsumer}.
     *
     * @param consumer the {@link RdfQuadConsumer} that will receive the canonical
     *                 RDF quads
     * @throws RdfConsumerException  if an error occurs while processing or
     *                               consuming RDF quads
     * @throws IllegalStateException if the computation is terminated prematurely
     */
    public void provide(final RdfQuadConsumer consumer) throws RdfConsumerException {

        ticker.tick();

        // Step 3:
        setNonNormalized();

        // Steps 4 and 5:
        issueSimpleIds();

        // Step 6:
        issueNDegreeIds();

        // Step 7:
        makeCanonQuads(consumer);
    }

    public Map<String, String> mappingTable() {
        return canonIssuer.mappingTable();
    }

    @Override
    public RdfQuadConsumer quad(String subject, String predicate, String object, String datatype, String language, String direction, String graph) {

        Quad quad = new Quad();
        setResource(Position.SUBJECT, quad, subject);
        quad.predicate = predicate;

        if (RdfQuadConsumer.isLiteral(datatype, language, direction)) {
            quad.object = object;
            quad.datatype = datatype;
            quad.language = language;
            quad.direction = direction;

        } else {
            setResource(Position.OBJECT, quad, object);
        }

        setResource(Position.GRAPH, quad, graph);
        quad.init();

        quads.add(quad);
        return this;
    }

    static RdfCanon newInstance(
            final Set<Quad> nquads,
            final MessageDigest digest,
            final RdfCanonTicker ticker) {
        return new RdfCanon(
                new HashMap<>(),
                new HashMap<>(),
                digest,
                nquads,
                ticker);
    }

    String forBlank(Quad q0, String blankNodeId) {

        String subject = q0.subject;
        if (q0.blankSubject != null) {
            // A blank node is always a resource
            subject = subject.equals(blankNodeId) ? BLANK_A : BLANK_Z;
        }

        String object = q0.object;
        if (q0.blankObject != null) {
            object = object.equals(blankNodeId) ? BLANK_A : BLANK_Z;
        }

        String graph = q0.graph;
        if (q0.blankGraph != null) {
            graph = graph.equals(blankNodeId) ? BLANK_A : BLANK_Z;
        }

        return NQuadsWriter.nquad(
                subject,
                q0.predicate,
                object,
                q0.datatype,
                q0.language,
                q0.direction,
                graph);
    }

    void setNonNormalized() {
        nonNormalized = new HashSet<>(blankIdToQuadSet.keySet());
    }

    String hashFirstDegree(final String blankNodeId) {

        Collection<Quad> related = blankIdToQuadSet.get(blankNodeId);
        String[] nQuads = new String[related.size()];
        int i = 0;

        // Convert the NQuads to a consistent set by replacing the reference with _:a
        // and all others with _:z, and then sorting
        for (Quad q0 : related) {
            ticker.tick();
            nQuads[i] = forBlank(q0, blankNodeId);
            i++;
        }

        // Sort the nQuads
        Arrays.sort(nQuads);

        // Create the hash
        digest.reset();
        for (String s : nQuads) {
            digest.update(s.getBytes(StandardCharsets.UTF_8));
        }
        return hex(digest.digest());
    }

    void issueSimpleIds() {
        boolean simple = true;
        while (simple) {
            ticker.tick();
            simple = false;
            hashToBlankId.clear();
            for (String value : nonNormalized) {
                String hash = hashFirstDegree(value);
                hashToBlankId.computeIfAbsent(hash, k -> new HashSet<>()).add(value);
            }

            Iterator<Entry<String, Set<String>>> iterator = hashToBlankId.entrySet().iterator();
            while (iterator.hasNext()) {
                ticker.tick();
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

    void issueNDegreeIds() {

        for (Entry<String, Set<String>> entry : hashToBlankId.entrySet()) {
            List<NDegreeResult> hashPathList = new ArrayList<>();
            for (String id : entry.getValue()) {
                ticker.tick();
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

            hashPathList.sort(Comparator.naturalOrder());
            for (NDegreeResult result : hashPathList) {
                ticker.tick();
                result.getIssuer().assign(canonIssuer);
            }
        }
    }

    void makeCanonQuads(RdfQuadConsumer consumer) throws RdfConsumerException {

        // relabel blank nodes
        blankNodes.entrySet().forEach(entry -> {
            entry.getValue().normalized = canonIssuer.getIfExists(entry.getKey());
        });

        final List<Quad> sorted = new ArrayList<>(quads);

        // sort quads
        Collections.sort(sorted, QuadComparator.asc());

        for (final Quad quad : sorted) {
            consumer.quad(
                    quad.subject(),
                    quad.predicate,
                    quad.object(),
                    quad.datatype,
                    quad.language,
                    quad.direction,
                    quad.graph());
        }
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

    NDegreeResult hashNDegreeQuads(String id, IdentifierIssuer issuer) {
        return new HashNDegreeQuads().hash(id, issuer);
    }

    void setResource(final Position position, final Quad quad, final String name) {

        Blank blank = null;

        if (RdfQuadConsumer.isBlank(name)) {
            blank = blankNodes.computeIfAbsent(name, arg0 -> new Blank());
            blankIdToQuadSet.computeIfAbsent(name, k -> new LinkedList<>()).add(quad);
        }

        position.set(quad, name, blank);
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

        /**
         * Append an ID to the hash path.
         *
         * @param related       the ID to append
         * @param pathBuilder   the path to append to
         * @param issuerCopy    the identifier issuer
         * @param recursionList the node recursion list
         */
        private void appendToPath(String related, StringBuilder pathBuilder, IdentifierIssuer issuerCopy, List<String> recursionList) {
            if (canonIssuer.hasId(related)) {
                // 5.4.4.1: Already has a canonical ID so we just use it.
                pathBuilder.append(canonIssuer.getId(related));
            } else {
                // 5.4.4.2: Need to try an ID, and possibly recurse
                if (!issuerCopy.hasId(related)) {
                    recursionList.add(related);
                }
                pathBuilder.append(issuerCopy.getId(related));
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
            Collection<Quad> refer = blankIdToQuadSet.get(id);
            for (Quad quad : refer) {
                ticker.tick();
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

            ticker.tick();

            // 5.4.1 to 5.4.3 : initialize variables
            IdentifierIssuer issuerCopy = issuer.copy();
            StringBuilder pathBuilder = new StringBuilder();
            List<String> recursionList = new ArrayList<>();

            // 5.4.4: for every resource in the this permutation of the resources
            for (final String relatedId : permutation) {
                ticker.tick();
                appendToPath(relatedId, pathBuilder, issuerCopy, recursionList);

                // 5.4.4.3: Is this path better than our chosen path?
                if (chosenPath.length() > 0 && pathBuilder.toString().compareTo(chosenPath.toString()) > 0) {
                    // This is permutation is not going to make the best path, so skip the rest of
                    // it
                    return;
                }
            }

            // 5.4.5: Process the recursion list
            for (String related : recursionList) {
                ticker.tick();
                NDegreeResult result = hashNDegreeQuads(related, issuerCopy);

                pathBuilder
                        .append(issuerCopy.getId(related))
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
                    ticker.tick();
                    doPermutation(
                            permutator.next(),
                            issuer);
                }

                // 5.5: Append chosen path to the hash
                dataToHash.append(chosenPath);
                issuer = chosenIssuer;
            }

            digest.reset();
            String hash = hex(digest.digest(dataToHash.toString().getBytes(StandardCharsets.UTF_8)));
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
                Quad quad,
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
            digest.reset();
            digest.update(position.tag());
            if (position != Position.GRAPH) {
                digest.update(NQuadsWriter.resource(quad.predicate).getBytes(StandardCharsets.UTF_8));
            }
            digest.update(id.getBytes(StandardCharsets.UTF_8));
            return hex(digest.digest());
        }
    }
}
