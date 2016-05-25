package org.doremus.marc2rdf.bnfconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class F31_AutrePerformance {

	static Model modelF31 = ModelFactory.createDefaultModel();
	static URI uriF31=null;

	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";

	/********************************************************************************************/
    public URI getURIF31() throws URISyntaxException {
    	if (uriF31==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF31 = uri.getUUID("Performance","F31", uuid.get());
    	}
    	return uriF31;
    }

    /********************************************************************************************/
    public Model getModel() throws URISyntaxException, IOException{
    	uriF31 = getURIF31();
    	Resource F31 = modelF31.createResource(uriF31.toString());

    	/**************************** Performance: 1ère exécution *******************************/
    	F31.addProperty(modelF31.createProperty(cidoc+ "P3_has_note"), getNote(Converter.getFile()));

    	return modelF31;
    }

    /*********************************** L'exécution ***********************************/
    public static String getNote(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String note="";
        String st;

        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        				 note = s.dataFields.get(i).getSubfield('a').getData();
        				 if (note.contains("exécution ")){
        					 st = note.substring(note.lastIndexOf("exécution") + 2);
        					 if (!(st.contains(":"))) buffer.append(note);
        				 }
        				 if (note.contains("éxécution ")){
        					 st = note.substring(note.lastIndexOf("éxécution") + 2);
        					 if (!(st.contains(":"))) buffer.append(note);
        				 }
        				 if (note.contains("représentation  ")){
        					 st = note.substring(note.lastIndexOf("représentation") + 2);
        					 if (!(st.contains(":"))) buffer.append(note);
        				 }
        			 }
        		 }
        	 }
        }
        return buffer.toString();
    	}

}
