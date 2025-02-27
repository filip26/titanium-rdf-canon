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

import com.apicatalog.rdf.nquads.NQuadsAlphabet;

class Rdf {

    private Rdf() {
    }

    static final RdfNQuad createNQuad(RdfResource subject, RdfResource predicate, RdfValue object, RdfResource graphName) {
        return new ImmutableRdfNQuad(subject, predicate, object, graphName);
    }

    static RdfValue createValue(String value) {

        if (value == null) {
            throw new IllegalArgumentException();
        }

        if (BlankNode.isWellFormed(value)) {
            return createBlankNode(value);
        }

        if (UriUtils.isAbsoluteUri(value, UriValidationPolicy.Full)) {
            return createIRI(value);
        }

        return createTypedString(value, NQuadsAlphabet.XSD_STRING);
    }

    static RdfLiteral createString(String lexicalForm) {
        return createTypedString(lexicalForm, NQuadsAlphabet.XSD_STRING);
    }

    static RdfLiteral createTypedString(String lexicalForm, String datatype) {
        return new ImmutableRdfLiteral(lexicalForm, null, datatype);
    }

    static RdfLiteral createLangString(String lexicalForm, String langTag) {
        return new ImmutableRdfLiteral(lexicalForm, langTag, null);
    }

    static RdfLiteral createLangString(String lexicalForm, String language, String direction) {
        return direction != null
                ? createTypedString(lexicalForm,
                        NQuadsAlphabet.I18N_BASE
                                .concat(language)
                                .concat("_")
                                .concat(direction))
                : createLangString(lexicalForm, language);
    }

    /**
     * Create a new {@link RdfResource}.
     *
     * @param resource is an absolute IRI or blank node identifier
     * @return RDF resource
     * @throws IllegalArgumentException if the resource is not an absolute IRI or
     *                                  blank node identifier
     */
    static RdfResource createResource(String resource) {

        if (resource == null) {
            throw new IllegalArgumentException("The resource value cannot be null.");
        }

        if (BlankNode.isWellFormed(resource)) {
            return createBlankNode(resource);
        }

        if (UriUtils.isAbsoluteUri(resource, UriValidationPolicy.Full)) {
            return createIRI(resource);
        }

        throw new IllegalArgumentException("The resource must be an absolute IRI or blank node identifier, but was [" + resource + "].");
    }

    static RdfResource createBlankNode(final String value) {
        if (!value.startsWith("_:")) {
            return new ImmutableRdfResource("_:" +value, true);
        }

        return new ImmutableRdfResource(value, true);
    }

    static RdfResource createIRI(final String value) {
        return new ImmutableRdfResource(value, false);
    }
}