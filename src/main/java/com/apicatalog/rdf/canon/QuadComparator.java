package com.apicatalog.rdf.canon;

import java.util.Comparator;

class QuadComparator implements Comparator<Quad> {

    protected static Comparator<Quad> INSTANCE = new QuadComparator();
    
    public static Comparator<Quad> asc() {
        return INSTANCE;
    }
    
    @Override
    public int compare(Quad o1, Quad o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
