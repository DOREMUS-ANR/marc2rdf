package org.doremus.marc2rdf.bnfparser;

public abstract class Etiq {

    private String etiq;
    
    public Etiq(String etiq) {
    	this.etiq = etiq;
    }

    public String getEtiq() {
        return etiq;
    }

    public String toString() {
        return etiq;
    }
    
}