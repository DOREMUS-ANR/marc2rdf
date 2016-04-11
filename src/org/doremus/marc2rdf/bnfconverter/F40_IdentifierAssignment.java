package org.doremus.marc2rdf.bnfconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import main.Converter;

import org.apache.http.client.utils.URIBuilder;
import org.doremus.marc2rdf.bnfparser.MarcXmlReader;
import org.doremus.marc2rdf.bnfparser.Record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class F40_IdentifierAssignment {
	
	/*** Correspond à l'attribution d'identifiant pour l'oeuvre ***/
	
	static Model modelF40 = ModelFactory.createDefaultModel();
	static URI uriF40=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF40() throws URISyntaxException {
    	if (uriF40==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF40 = uri.getUUID("Identifier_Assignment","F40", uuid.get());
    	}
    	return uriF40;
    }
    
    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	uriF40 = getURIF40();
    	Resource F40 = modelF40.createResource(uriF40.toString());
    	
    	/**************************** Schéma général : Attribution ******************************/
    	F22_SelfContainedExpression F22= new F22_SelfContainedExpression();
    	F40.addProperty(modelF40.createProperty(frbroo+ "R45_assigned_to"), modelF40.createResource(F22.getURIF22().toString()));
    	
    	/**************************** Schéma général : agence ***********************************/
    	F40.addProperty(modelF40.createProperty(cidoc+ "P14_carried_out_by"), modelF40.createResource(getBiblioAgency(Converter.getFile()).toString()));
    	
    	/**************************** Schéma général : règles ***********************************/
    	F40.addProperty(modelF40.createProperty(frbroo+ "R52_used_rule"), "NF Z 44-079 (Novembre 1993) - Documentation - Catalogage- Forme et structure des vedettes titres musicaux");
    	
    	/****************************  Schéma général : type d'identifiant************************/
    	//F40.addProperty(modelF40.createProperty(cidoc+ "P2_has_type"), "NF Z 44-079 (Novembre 1993) - Documentation - Catalogage- Forme et structure des vedettes titres musicaux");
    	
		return modelF40;
    }
    /********************************************************************************************/
	
	/******** L'agence bibliographique qui a attribué l'identifiant @throws URISyntaxException *****************/
    public static URI getBiblioAgency(String xmlFile) throws FileNotFoundException, URISyntaxException {
    	URIBuilder builder = null ;
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String agence = "";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 if (s.controlFields.get(i).getEtiq().equals("001")) {
        			 for (int j=0; j<=4; j++){
        				 agence = agence + s.controlFields.get(i).getData().charAt(j);
        			 }
        		 }
        	 }
        }
        if (agence.equals("FRBNF")) {
        	builder = new URIBuilder().setPath("http://isni.org/isni/0000000121751303");
        }
        URI uri = builder.build();
        return uri;
    }
    /************************** 4. Work: identifier assignment (Identifier) *********************/
	 public static String getIdentifier (String xmlFile) throws FileNotFoundException {
	    	StringBuilder buffer = new StringBuilder();
	        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
	        MarcXmlReader reader = new MarcXmlReader(file);
	        while (reader.hasNext()) { // Parcourir le fichier MARCXML
	        	 Record s = reader.next();
	        	 for (int i=0; i<s.controlFields.size(); i++) {
	        		 if (s.controlFields.get(i).getEtiq().equals("003")) {
	        				 buffer.append(s.controlFields.get(i).getData());
	        		 }
	        	 }
	        }
	        return buffer.toString();
	    }
	/********************************************************************************************/
    /*********************** Le créateur de l'identifiant *************************************/
    public static String getCreator(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("100")) {
        			 	 if (s.dataFields.get(i).isCode('a')){
        			 		 String aSubField = s.dataFields.get(i).getSubfield('a').getData();
        			 		 buffer.append(aSubField+" ");
        			 	 }
        			 	if (s.dataFields.get(i).isCode('m')){
        			 		 String mSubField = s.dataFields.get(i).getSubfield('m').getData();
        			 		 buffer.append(mSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('d')){
        			 		 String dSubField = s.dataFields.get(i).getSubfield('d').getData();
        			 		 buffer.append(dSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('e')){
        			 		 String eSubField = s.dataFields.get(i).getSubfield('e').getData();
        			 		 buffer.append(eSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('h')){
        			 		 String hSubField = s.dataFields.get(i).getSubfield('h').getData();
        			 		 buffer.append(hSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('u')){
        			 		 String uSubField = s.dataFields.get(i).getSubfield('u').getData();
        			 		 buffer.append(uSubField+" ");
        			 	}
        		 }
        	 }
        }
        return buffer.toString();
    }
    /*********************************** Le catalogue ***********************************/
    public static String getCatalog(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('k'))
        				 buffer.append(s.dataFields.get(i).getSubfield('k').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** L'opus ***********************************/
    public static String getOpus(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('p'))
        				 buffer.append(s.dataFields.get(i).getSubfield('p').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La distribution ***********************************/
    public static String getDistribution(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('b'))
        				 buffer.append(s.dataFields.get(i).getSubfield('b').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le numéro d'ordre ***********************************/
    public static String getOrderNumber(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('n'))
        				 buffer.append(s.dataFields.get(i).getSubfield('n').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Key (Tonalité) ***********************************/
    public static String getKey(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('t'))
        				 buffer.append(s.dataFields.get(i).getSubfield('t').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La langue ***********************************/
    public static String getLangue(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('f'))
        				 buffer.append(s.dataFields.get(i).getSubfield('f').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le genre **************************************/
    public static String getGenre(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('a'))
        				 buffer.append(s.dataFields.get(i).getSubfield('a').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La date (année) **************************************/
    public static String getDateYear(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('j'))
        				 buffer.append(s.dataFields.get(i).getSubfield('j').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La version **************************************/
    public static String getVersion(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('q'))
        				 buffer.append(s.dataFields.get(i).getSubfield('q').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La documentation **************************************/
    public static String getDocumentation(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("610")) {
        			 if (s.dataFields.get(i).isCode('a'))
        				 buffer.append(s.dataFields.get(i).getSubfield('a').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
}
