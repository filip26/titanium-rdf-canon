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
        return o1.toString().compareTo(o2.toString());
    }
}
