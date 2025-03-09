package com.apicatalog.rdf.canon;

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

    final String subject() {
        if (blankSubject != null) {
            if (blankSubject.canon != null) {
                return blankSubject.canon;
            }
        }
        return subject;
    }

    final String object() {
        if (blankObject != null) {
            if (blankObject.canon != null) {
                return blankObject.canon;
            }
        }
        return object;
    }

    final String graph() {
        if (blankGraph != null) {
            if (blankGraph.canon != null) {
                return blankGraph.canon;
            }
        }
        return graph;
    }

    @Override
    public String toString() {
        return NQuadsWriter.nquad(subject(), predicate, object(), datatype, language, direction, graph());
    }
}
