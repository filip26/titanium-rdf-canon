package com.apicatalog.rdf.canon;

import java.util.Comparator;

final class QuadComparator implements Comparator<Quad> {

    protected static Comparator<Quad> INSTANCE = new QuadComparator();
    
    public static Comparator<Quad> asc() {
        return INSTANCE;
    }
    
    @Override
    public int compare(Quad o1, Quad o2) {
        o1.update();
        o2.update();
        return o1.nquad.compareTo(o2.nquad);
    }
}
