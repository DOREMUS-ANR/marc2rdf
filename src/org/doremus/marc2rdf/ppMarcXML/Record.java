package org.doremus.marc2rdf.ppMarcXML;

import java.util.ArrayList;
import java.util.List;

public class Record {

    public List<DataField> dataFields;
    public String id;
    public String type;

    public Record(String type, String id) {
    	this.type=type;
    	this.id=id;
        dataFields = new ArrayList<DataField>();
    }
    
    public String getIdentifier(){
    	return this.id;
    }
    
    public String getType(){
    	return this.type;
    }
    
    public void addDataField(Etiq field) {
        dataFields.add((DataField)field);
    }
    
    public List<Etiq> getDataFields() 
    {
        List<Etiq> fields = new ArrayList<Etiq>();
        fields.addAll(dataFields);
        return fields;
    }
    
    public List<Etiq> getAllData() 
    {
        List<Etiq> fields = new ArrayList<Etiq>();
        fields.addAll(dataFields);
        return fields;
    }
    
    public int size () {
    	return dataFields.size();
    }
    
    /******************* Affichage final de la notice ***************************/
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Etiq field : getDataFields())
        {
            sb.append(field.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
    /*****************************************************************************/
}