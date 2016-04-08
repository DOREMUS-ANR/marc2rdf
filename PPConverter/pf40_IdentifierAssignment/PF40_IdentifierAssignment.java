package pf40_IdentifierAssignment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import main.Converter;

import org.apache.http.client.utils.URIBuilder;

import pf22_SelfContainedExpression.PF22_SelfContainedExpression;
import pf50_ControlledAccessPoint.PF50_ControlledAccessPoint;
import pf14_IndividualWork.PF14_IndividualWork;
import ppConverter.ConstructURI;
import ppConverter.GenerateUUID;
import ppMarcXML.MarcXmlReader;
import ppMarcXML.Record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class PF40_IdentifierAssignment {
	
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
    	
    	/**************************** Work: was assigned by *************************************/
    	PF50_ControlledAccessPoint F50= new PF50_ControlledAccessPoint();
    	F40.addProperty(modelF40.createProperty(frbroo+ "R46_assigned"), modelF40.createResource(F50.getURIF50().toString()));
    	
    	/**************************** Schéma général : Attribution ******************************/
    	PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
    	F40.addProperty(modelF40.createProperty(frbroo+ "R45_assigned_to"), modelF40.createResource(F22.getURIF22().toString()));
    	
    //	/**************************** Schéma général : agence ***********************************/
    	F40.addProperty(modelF40.createProperty(cidoc+ "P14_carried_out_by"), modelF40.createResource("http://data.doremus.org/Philharmonie_de_Paris"));
    	
   // 	/**************************** Work: identifier assignment (Identifier) ******************/
    	F40.addProperty(modelF40.createProperty(frbroo+ "R46_assigned"), getIdentifier(Converter.getFile()).toString()); // L'identifiant de l'oeuvre
    	
    //	/**************************** Work: identifier assignment (type) ************************/
    	F40.addProperty(modelF40.createProperty(cidoc+ "P2_has_Type"), "N° de notice"); // "N° de notice" par défaut pour toutes les notices 
    	
    //	/**************************** Work: was assigned by *************************************/
    	F40.addProperty(modelF40.createProperty(cidoc+ "P2_has_Type"), getTypeTitle(Converter.getFile()).toString());
    	
    	/**************************** Schéma général : règles ***********************************/
    	F40.addProperty(modelF40.createProperty(frbroo+ "R52_used_rule"), getRule(Converter.getFile()).toString());
    	
    	/**************************** Attribution d'identifiant compositeur *********************/
    //	F40.addProperty(modelF40.createProperty(frbroo+ "R52_used_rule"), getCreator(Converter.getFile()).toString());
    	// Il faut voir pour la propriété à utiliser ici avec la PP
    	
		return modelF40;
    }
    /********************************************************************************************/
	
    /************************** 4. Work: identifier assignment (Identifier) *********************/
	 public static String getIdentifier (String xmlFile) throws FileNotFoundException {
	    	StringBuilder buffer = new StringBuilder();
	        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
	        MarcXmlReader reader = new MarcXmlReader(file);
	        while (reader.hasNext()) { // Parcourir le fichier MARCXML
	        	 Record s = reader.next();
	        	 buffer.append(s.getIdentifier());
	        }
	        return buffer.toString();
	    }
	 
	 /************************** Schéma général : règles  **************************************/
	 public static String getRule (String xmlFile) throws FileNotFoundException {
	    	StringBuilder buffer = new StringBuilder();
	        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
	        MarcXmlReader reader = new MarcXmlReader(file);
	        String typeNotice="";
	        while (reader.hasNext()) { // Parcourir le fichier MARCXML
	        	 Record s = reader.next();
	        	 typeNotice = s.getType();
	        }
	        if (typeNotice.equals("AIC:14")) buffer.append("NF Z 44-079 (Novembre 1993) - Documentation - Catalogage- Forme et structure des vedettes titres musicaux");
	        else if (typeNotice.equals("UNI:100")) buffer.append("Usages de la Philharmonie de Paris");
	        return buffer.toString();
	    }
	 
	 /************************** Type du titre  ************************************************/
	 public static String getTypeTitle (String xmlFile) throws FileNotFoundException {
		 	
		 	StringBuilder buffer = new StringBuilder();
	        if (getTypeNotice(xmlFile).equals("AIC:14")) buffer.append("Variante");
	        else if (getTypeNotice(xmlFile).equals("UNI:100")) buffer.append("Point d'accès privilégié");
	        return buffer.toString();
	    }
	 
	 /************************** Type de notice  *********************************************/
	 public static String getTypeNotice (String xmlFile) throws FileNotFoundException {
	    	StringBuilder buffer = new StringBuilder();
	        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
	        MarcXmlReader reader = new MarcXmlReader(file);
	        String typeNotice="";
	        while (reader.hasNext()) { // Parcourir le fichier MARCXML
	        	 Record s = reader.next();
	        	 typeNotice = s.getType();
	        }
	        buffer.append(typeNotice);
	        return buffer.toString();
	    }
	
    /*********************** Le créateur de l'identifiant *************************************/
    public static String getCreator(String xmlFile) throws FileNotFoundException {
    	StringBuilder buffer = new StringBuilder();
        if (getTypeNotice(xmlFile).equals("UNI:100")) { //Si c'est une notice d'oeuvre
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String aSubField = "", bSubField="", fSubField="";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("700")) {
        			 	 if (s.dataFields.get(i).isCode('a')){
        			 		 aSubField = s.dataFields.get(i).getSubfield('a').getData();
        			 	 }
        			 	if (s.dataFields.get(i).isCode('b')){
        			 		 bSubField = s.dataFields.get(i).getSubfield('b').getData();
        			 	}
        			 	if (s.dataFields.get(i).isCode('f')){
        			 		 fSubField = s.dataFields.get(i).getSubfield('f').getData();
        			 	}
        		 }
        		 if (s.dataFields.get(i).getEtiq().equals("701")) {
        			 if (s.dataFields.get(i).isCode('4')){ //prendre en compte le 701 que si son $4 a la valeur "230"
        				 if ((s.dataFields.get(i).getSubfield('4').getData()).equals("230")){
        					 if (s.dataFields.get(i).isCode('a')){
        						 aSubField = s.dataFields.get(i).getSubfield('a').getData();
        					 }
        					 if (s.dataFields.get(i).isCode('b')){
        						 bSubField = s.dataFields.get(i).getSubfield('b').getData();
        					 }
        					 if (s.dataFields.get(i).isCode('f')){
        						 fSubField = s.dataFields.get(i).getSubfield('f').getData();
        					 }
        				 }
        			 }
        		 }
        	 }
        }
        if ((aSubField.equals("")) && (bSubField.equals("")) && (fSubField.equals(""))) buffer.append("");
        
        else {buffer.append(aSubField); buffer.append(","); buffer.append(bSubField); buffer.append("("); buffer.append(fSubField); buffer.append(")");}
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
