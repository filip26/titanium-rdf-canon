package com.apicatalog.rdf.canon;

import java.util.Objects;

import com.apicatalog.rdf.nquads.NQuadsWriter;

final class Quad {

    Blank blankSubject;
    Blank blankObject;
    Blank blankGraph;

    String subject;
    String predicate;
    String object;
    String datatype;
    String language;
    String direction;
    String graph;

    String nquad;

    void init() {
        this.nquad = NQuadsWriter.nquad(subject(), predicate, object(), datatype, language, direction, graph());
    }

    void update() {
        if ((blankSubject != null && blankSubject.normalized != null)
                || (blankObject != null && blankObject.normalized != null)
                || (blankGraph != null && blankGraph.normalized != null)) {
            this.nquad = NQuadsWriter.nquad(subject(), predicate, object(), datatype, language, direction, graph());
        }
    }

    final String subject() {
        if (blankSubject != null && blankSubject.normalized != null) {
            return blankSubject.normalized;
        }
        return subject;
    }

    final String object() {
        if (blankObject != null && blankObject.normalized != null) {
            return blankObject.normalized;
        }
        return object;
    }

    final String graph() {
        if (blankGraph != null && blankGraph.normalized != null) {
            return blankGraph.normalized;
        }
        return graph;
    }

    @Override
    public String toString() {
        return nquad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nquad);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Quad)) {
            return false;
        }
        Quad other = (Quad) obj;
        return Objects.equals(nquad, other.nquad);
    }
}
