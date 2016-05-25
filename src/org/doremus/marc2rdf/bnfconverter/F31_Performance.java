package org.doremus.marc2rdf.bnfconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.bnfparser.MarcXmlReader;
import org.doremus.marc2rdf.bnfparser.Record;
import org.doremus.marc2rdf.main.Converter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class F31_Performance {

	static Model modelF31 = ModelFactory.createDefaultModel();
	static URI uriF31=null;

	public F31_Performance() throws URISyntaxException{
		this.modelF31 = ModelFactory.createDefaultModel();
		this.uriF31= getURIF31();
	}

	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    String dcterms = "http://dublincore.org/documents/dcmi-terms/#";
    String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/********************************************************************************************/
    public URI getURIF31() throws URISyntaxException {
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF31 = uri.getUUID("Performance","F31", uuid.get());
    	return uriF31;
    }

    /********************************************************************************************/
    public Model getModel() throws URISyntaxException, IOException{

    	Resource F31 = modelF31.createResource(uriF31.toString());

    	/**************************** création d'une expression de plan d'exécution *************/
    	modelF31.createResource(F28_ExpressionCreation.uriF28.toString()).addProperty(modelF31.createProperty(frbroo+ "R17_created"), getURIF31().toString());

    	/**************************** exécution du plan *****************************************/
    	modelF31.createResource(F25_PerformancePlan.uriF25.toString()).addProperty(modelF31.createProperty(frbroo+ "R25i_was_performed_by"), getURIF31().toString());

    	/**************************** Performance: 1ère exécution *******************************/
    	modelF31.createResource(F14_IndividualWork.uriF14.toString()).addProperty(modelF31.createProperty(mus+ "U5_had_premiere"), getURIF31().toString());

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
    			 	 if ((execution.contains("exécution : "))||(execution.contains("éxécution : "))||(execution.contains("représentation : "))){
    				 buffer.append(execution);
    			 	 }
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}

}
