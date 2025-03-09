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
    SUBJECT('s') {
        @Override
        String get(Quad quad) {
            return quad.subject();
        }

        @Override
        void set(Quad quad, String value, Blank blank) {
            quad.subject = value;
            quad.blankSubject = blank;
        }

        @Override
        public boolean isBlank(Quad quad) {
            return quad.blankSubject != null;
        }
    },

    /** The object of the quad. */
    OBJECT('o') {
        @Override
        String get(Quad quad) {
            return quad.object();
        }

        @Override
        public boolean isBlank(Quad quad) {
            return quad.blankObject != null;
        }

        @Override
        void set(Quad quad, String value, Blank blank) {
            quad.object = value;
            quad.blankObject = blank;
        }
    },

    /** The graph the quad belongs to. */
    GRAPH('g') {
        @Override
        String get(Quad quad) {
            return quad.graph();
        }

        @Override
        public boolean isBlank(Quad quad) {
            return quad.graph != null;
        }

        void set(Quad quad, String value, Blank blank) {
            quad.graph = value;
            quad.blankGraph = blank;
        }
    },

    /**
     * The predicate of the quad. Note the predicate is not used by the URDNA-2015
     * algorithm.
     */
    PREDICATE('p') {
        @Override
        String get(Quad quad) {
            return quad.predicate;
        }

        @Override
        public boolean isBlank(Quad quad) {
            // predicates cannot be blank
            return false;
        }

        void set(Quad quad, String value, Blank blank) {
            assert (blank == null);
            quad.predicate = value;
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

    Position(char ch) {
        this.tag = (byte) ch;
    }

    /**
     * Get the value at this position in the quad.
     *
     * @param quad the quad
     *
     * @return the value at this position
     */
    abstract String get(Quad quad);

    /**
     * Is the value at this position in the quad a blank node identifier?.
     *
     * @param quad the quad
     *
     * @return true if this position holds a blank node identifier.
     */
    abstract boolean isBlank(Quad quad);

    abstract void set(Quad quad, String value, Blank blank);

    /**
     * Get the tag to include in hashes to represent this position.
     *
     * @return the tag
     */
    public byte tag() {
        return tag;
    }
}
