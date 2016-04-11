package org.doremus.marc2rdf.ppconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;

import main.Converter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class PF31_Performance {
	
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
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("919")) {
        			 if (s.dataFields.get(i).isCode('a')){
        				 note = s.dataFields.get(i).getSubfield('a').getData();
        			 }
        		 }
        	 }
        }
        if (note.contains(". Editeur")){
        	int index = note.indexOf(". Editeur");
        	note = note.substring(0, index+1);
        }
        if (note.contains(". Première édition")){
        	int index = note.indexOf(". Première édition");
        	note = note.substring(0, index+1);
        }
        if (note.contains(". 1ère édition")){
        	int index = note.indexOf(". 1ère édition");
        	note = note.substring(0, index+1);
        }
        if (note.contains(". Publication")){
        	int index = note.indexOf(". Publication");
        	note = note.substring(0, index+1);
        }
        if (note.contains(". Première publication")){
        	int index = note.indexOf(". Première publication");
        	note = note.substring(0, index+1);
        }
        if (note.contains(". 1ere publication")){
        	int index = note.indexOf(". 1ere publication");
        	note = note.substring(0, index+1);
        }
        if (note.contains(". Création française")){
        	int index = note.indexOf(". Création française");
        	note = note.substring(0, index+1);
        }
        buffer.append(note);
        return buffer.toString();
    	}
    
}
