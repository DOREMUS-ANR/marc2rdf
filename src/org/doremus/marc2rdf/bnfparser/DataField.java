package org.doremus.marc2rdf.bnfparser;

import java.util.ArrayList;
import java.util.List;

public class DataField extends Etiq {

    private char ind1;
    private char ind2;
    private List<Subfield> subfields = new ArrayList<Subfield>();

    public DataField(String etiq, char ind1, char ind2) {
        super(etiq);
        this.ind1 = ind1;
        this.ind2 = ind2;
    }

    public char getIndicator1() { return ind1; }

    public char getIndicator2() { return ind2; }

    public void addSubfield(Subfield subfield) { subfields.add(subfield); }

    public List<Subfield> getSubfields() { return subfields; }
    
    public boolean isCode (char c) {
    	for (Subfield subfield : subfields) 
        {
            if (subfield.getCode() == c)
                return true; 
        }
    	return false;
    }
    
    /************ Recuperer une sous-zone d'une certaine zone *************/
    public Subfield getSubfield(char code) { 
        for (Subfield subfield : subfields) 
        {
            if (subfield.getCode() == code)
                return subfield; 
        }
        return null;
    }
    
    /*************** Recuperer la valeur d'une zone ***********************/
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(' ');
        sb.append(getIndicator1());
        sb.append(getIndicator2());
        for (Subfield sf : subfields) 
        {
            sb.append(sf.toString());
        } 
        return sb.toString();
    }
    /*********************************************************************/
}