package com.apicatalog.rdf.canon;

import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;

/**
 * An issuer of counted identifiers to map identifiers from one naming scheme to
 * another.
 *
 * @author Simon Greatrix on 06/10/2020.
 */
public class IdentifierIssuer {

    /** Identifiers that have already been issued. */
    private final Map<MutableBlankNode, RdfResource> existing;

    /** The prefix for new identifiers. */
    private final String prefix;

    /** Counter for creating new identifiers. */
    private int counter;

    /**
     * Create a new instance.
     *
     * @param prefix the prefix for new identifiers.
     */
    public IdentifierIssuer(String prefix) {
        this(prefix, new LinkedHashMap<>(), 0);
    }

    public IdentifierIssuer(String prefix, Map<MutableBlankNode, RdfResource> mapping, int counter) {
        this.prefix = prefix;
        this.existing = mapping;
        this.counter = counter;
    }

    /**
     * Create a mapping in another issuer for all identifiers issued by this, in the
     * same order that they were issued by this.
     *
     * @param other the other identifier issuer.
     */
    public void assign(IdentifierIssuer other) {
        existing.forEach((k, v) -> other.getId(k));
    }

    /**
     * Create a copy of this issuer.
     *
     * @return the issuer to copy
     */
    public IdentifierIssuer copy() {
        IdentifierIssuer newIssuer = new IdentifierIssuer(prefix);
        newIssuer.existing.putAll(existing);
        newIssuer.counter = counter;
        return newIssuer;
    }

    private RdfResource getForBlank(MutableBlankNode value) {
        if (hasId(value)) {
            return getId(value);
        }
        return value;
    }

    /**
     * Get or allocate a new ID for the specified old ID.
     *
     * @param id the old ID
     *
     * @return the new ID
     */
    public RdfResource getId(MutableBlankNode id) {
        return existing.computeIfAbsent(id, k -> Rdf.createBlankNode(prefix + (counter++)));
    }

    /**
     * Get the resource replaced by a proper blank identifier if appropriate.
     *
     * @param value the resource to check
     *
     * @return the value or the replaced value
     */
    public RdfResource getIfExists(MutableBlankNode value) {
        return (value != null && value.isBlankNode()) ? getForBlank(value) : value;
    }

    /**
     * Get the resource replaced by a proper blank identifier if appropriate.
     *
     * @param value the resource to check
     *
     * @return the value or the replaced value
     */
    RdfValue getIfExists(RdfValue value) {
        return (value != null && value.isBlankNode()) ? getForBlank((MutableBlankNode) value) : value;
    }

    /**
     * Does an old ID have an allocated new ID?.
     *
     * @param id the old ID
     *
     * @return true of a new ID has been allocated for this old ID.
     */
    public boolean hasId(RdfResource id) {
        return existing.containsKey(id);
    }

    /**
     * Get blank nodes mapping table.
     * 
     * @return a mapping table
     */
    public Map<MutableBlankNode, RdfResource> mappingTable() {
        return existing;
    }
}
