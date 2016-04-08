package org.doremus.marc2rdf.ppConverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.ppConverter.ConstructURI;
import org.doremus.marc2rdf.ppConverter.GenerateUUID;
import org.doremus.marc2rdf.ppConverter.PF14_IndividualWork;
import org.doremus.marc2rdf.ppConverter.PF19_PublicationWork;
import org.doremus.marc2rdf.ppConverter.PF22_SelfContainedExpression;
import org.doremus.marc2rdf.ppConverter.PF24_PublicationExpression;
import org.doremus.marc2rdf.ppMarcXML.MarcXmlReader;
import org.doremus.marc2rdf.ppMarcXML.Record;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class PF30_PublicationEvent {

	static Model modelF30 = ModelFactory.createDefaultModel();
	static URI uriF30=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF30() throws URISyntaxException {
    	if (uriF30==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF30 = uri.getUUID("Publication_Event","F30", uuid.get());
    	}
    	return uriF30;
    }
    
    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	uriF30 = getURIF30();
    	Resource F30 = modelF30.createResource(uriF30.toString());
    	
    	/**************************** création d'une expression de publication *************/
    	PF24_PublicationExpression F24= new PF24_PublicationExpression();
    	F30.addProperty(modelF30.createProperty(frbroo+ "R24_created"), modelF30.createResource(F24.getURIF24().toString()));
    	
    	/**************************** réalisation d'une work *******************************/
    	PF19_PublicationWork F19= new PF19_PublicationWork();
    	F30.addProperty(modelF30.createProperty(frbroo+ "R19_created_a_realisation_of"), modelF30.createResource(F19.getURIF19().toString()));
    	
    	/**************************** Publication Event: édition princeps ******************/
    	PF14_IndividualWork F14= new PF14_IndividualWork();
    	F30.addProperty(modelF30.createProperty(mus+ "U4i_is_princeps_publication_of"), modelF30.createResource(F14.getURIF14().toString()));
    	
    	/**************************** Expression: 1ère publication *************************/
    	F30.addProperty(modelF30.createProperty(cidoc+ "P3_has_note"), getNote(Converter.getFile()));
    	
  
        
		return modelF30;
    }
    /********************************************************************************************/
    
    /*********************************** La note ***********************************/
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
        	note = note.substring(index+1, note.length());
        	buffer.append(note);
        }
        if (note.contains(". Première édition")){
        	int index = note.indexOf(". Première édition");
        	note = note.substring(index+1, note.length());
        	buffer.append(note);
        }
        if (note.contains(". 1ère édition")){
        	int index = note.indexOf(". 1ère édition");
        	note = note.substring(index+1, note.length());
        	buffer.append(note);
        }
        if (note.contains(". Publication")){
        	int index = note.indexOf(". Publication");
        	note = note.substring(index+1, note.length());
        	buffer.append(note);
        }
        if (note.contains(". Première publication")){
        	int index = note.indexOf(". Première publication");
        	note = note.substring(index+1, note.length());
        	buffer.append(note);
        }
        if (note.contains(". 1ere publication")){
        	int index = note.indexOf(". 1ere publication");
        	note = note.substring(index+1, note.length());
        	buffer.append(note);
        }
        return buffer.toString();
    	}
}
