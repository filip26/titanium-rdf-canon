package com.apicatalog.rdf.canon;

import java.util.Objects;

import com.apicatalog.rdf.RdfResource;

class MutableBlankNode implements RdfResource {

    private String value;

    public MutableBlankNode(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() == obj.getClass()) {
            MutableBlankNode other = (MutableBlankNode) obj;
            return Objects.equals(value, other.value);
        }        

        if (!(obj instanceof RdfResource)) {
            return false;
        }
        RdfResource other = (RdfResource) obj;
        return other.isBlankNode() && Objects.equals(value, other.getValue());
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public boolean isBlankNode() {
        return true;
    }
}
