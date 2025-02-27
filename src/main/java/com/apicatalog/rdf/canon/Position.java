package com.apicatalog.rdf.canon;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Enumeration of positions in an RDF quad.
 *
 * @author Simon Greatrix on 07/10/2020.
 */
enum Position {
    /** The subject of the quad. */
    SUBJECT('s', 0) {
        @Override
        public boolean isBlank(RdfNQuad quad) {
            return quad.getSubject().isBlankNode();
        }

        @Override
        String get(RdfNQuad quad) {
            return quad.getSubject().getValue();
        }

    },

    /** The object of the quad. */
    OBJECT('o', 2) {
        @Override
        String get(RdfNQuad quad) {
            return quad.getObject().getValue();
        }

        @Override
        public boolean isBlank(RdfNQuad quad) {
            return quad.getObject().isBlankNode();
        }
    },

    /** The graph the quad belongs to. */
    GRAPH('g', 3) {
        @Override
        String get(RdfNQuad quad) {
            return quad.getGraphName().map(RdfResource::getValue).orElse(null);
        }

        @Override
        public boolean isBlank(RdfNQuad quad) {
            return quad.getGraphName().filter(RdfValue::isBlankNode).isPresent();
        }
    },

    /**
     * The predicate of the quad. Note the predicate is not used by the URDNA-2015
     * algorithm.
     */
    PREDICATE('p', 1) {
        @Override
        String get(RdfNQuad quad) {
            return quad.getPredicate().getValue();
        }

        @Override
        public boolean isBlank(RdfNQuad quad) {
            // predicates cannot be blank
            return false;
        }
    };

    /**
     * Set of positions in a quad which can be blank.
     */
    public static final Set<Position> CAN_BE_BLANK = Collections.unmodifiableSet(EnumSet.of(SUBJECT, OBJECT, GRAPH));

    /**
     * The tag used to represent the position in hashes.
     */
    private final byte tag;
    private final int index;

    Position(char ch, int index) {
        this.tag = (byte) ch;
        this.index = index;
    }

    /**
     * Get the value at this position in the quad.
     *
     * @param quad the quad
     *
     * @return the value at this position
     */
    abstract String get(RdfNQuad quad);

    /**
     * Is the value at this position in the quad a blank node identifier?.
     *
     * @param quad the quad
     *
     * @return true if this position holds a blank node identifier.
     */
    abstract boolean isBlank(RdfNQuad quad);

    /**
     * Get the tag to include in hashes to represent this position.
     *
     * @return the tag
     */
    public byte tag() {
        return tag;
    }

    /**
     * Get the position index in an nquad array. [subject, predicate, object, graph]
     * 
     * @return the index
     */
    public int index() {
        return index;
    }
}
