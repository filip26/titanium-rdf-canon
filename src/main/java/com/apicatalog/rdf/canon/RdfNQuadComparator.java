package com.apicatalog.rdf.canon;

import java.util.Comparator;

import com.apicatalog.rdf.RdfNQuad;

public class RdfNQuadComparator implements Comparator<RdfNQuad> {

    protected static Comparator<RdfNQuad> INSTANCE = new RdfNQuadComparator();
    
    public static Comparator<RdfNQuad> asc() {
        return INSTANCE;
    }
    
    @Override
    public int compare(RdfNQuad o1, RdfNQuad o2) {
        int r = o1.getSubject().getValue().compareTo(o2.getSubject().getValue());
        if (r == 0) {
            r = o1.getPredicate().getValue().compareTo(o2.getPredicate().getValue());
        }
        if (r == 0) {
            r = o1.getObject().getValue().compareTo(o2.getObject().getValue());
        }
        return r;
    }
}
