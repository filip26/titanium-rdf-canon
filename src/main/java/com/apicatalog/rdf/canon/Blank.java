package com.apicatalog.rdf.canon;

final class Blank {

    String id;
    String canon;

    Blank(String id) {
        this.id = id;
    }

    
    
//        @Override
//        public int hashCode() {
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + getEnclosingInstance().hashCode();
//            result = prime * result + Objects.hash(id);
//            return result;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (!(obj instanceof Blank)) {
//                return false;
//            }
//            Blank other = (Blank) obj;
//            if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
//                return false;
//            }
//            return Objects.equals(id, other.id);
//        }
//
//        private Quad getEnclosingInstance() {
//            return Quad.this;
//        }
}
