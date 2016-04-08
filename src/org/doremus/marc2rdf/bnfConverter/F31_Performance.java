package org.doremus.marc2rdf.bnfConverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.bnfConverter.ConstructURI;
import org.doremus.marc2rdf.bnfConverter.F14_IndividualWork;
import org.doremus.marc2rdf.bnfConverter.F25_PerformancePlan;
import org.doremus.marc2rdf.bnfConverter.F28_ExpressionCreation;
import org.doremus.marc2rdf.bnfConverter.GenerateUUID;
import org.doremus.marc2rdf.bnfMarcXML.MarcXmlReader;
import org.doremus.marc2rdf.bnfMarcXML.Record;
import org.doremus.marc2rdf.main.Converter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class F31_Performance {
	
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
    	
    	/**************************** création d'une expression de plan d'exécution *************/
    	F28_ExpressionCreation F28= new F28_ExpressionCreation();
    	modelF31.createResource(F28.getURIF28().toString()).addProperty(modelF31.createProperty(frbroo+ "R17_created"), getURIF31().toString());
    	
    	/**************************** exécution du plan *****************************************/
    	F25_PerformancePlan F25= new F25_PerformancePlan();
    	modelF31.createResource(F25.getURIF25().toString()).addProperty(modelF31.createProperty(frbroo+ "R25i_was_performed_by"), getURIF31().toString());
    	
    	/**************************** Performance: 1ère exécution *******************************/
    	F14_IndividualWork F14= new F14_IndividualWork();
    	modelF31.createResource(F14.getURIF14().toString()).addProperty(modelF31.createProperty(mus+ "U5_had_premiere"), getURIF31().toString());

    	/**************************** Performance: 1ère exécution *******************************/
    	F31.addProperty(modelF31.createProperty(mus+ "U5_had_premiere"), getExecution(Converter.getFile()));
    	
    	return modelF31;
    }
    
    /*********************************** L'exécution ***********************************/
    public static String getExecution(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 String execution = s.dataFields.get(i).getSubfield('a').getData();
    			 	 if ((execution.contains("exécution"))||(execution.contains("éxécution"))||(execution.contains("représentation : "))){
    				 buffer.append(execution);
    			 	 }
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    
}
