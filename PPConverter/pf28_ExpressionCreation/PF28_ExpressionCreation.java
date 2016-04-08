package pf28_ExpressionCreation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import ppMarcXML.MarcXmlReader;
import ppMarcXML.Record;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import pf22_SelfContainedExpression.PF22_SelfContainedExpression;
import pf25_AutrePerformancePlan.PF25_AutrePerformancePlan;
import pf25_PerformancePlan.PF25_PerformancePlan;
import pf40_IdentifierAssignment.PF40_IdentifierAssignment;
import pf14_IndividualWork.PF14_IndividualWork;
import ppConverter.ConstructURI;
import ppConverter.GenerateUUID;
import main.Converter;

public class PF28_ExpressionCreation {
	
/*** Correspond à l'expression représentative de l'oeuvre musicale ***/
	
	static Model modelF28 = ModelFactory.createDefaultModel();
	static URI uriF28=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    String dcterms = "http://dublincore.org/documents/dcmi-terms/#";
    String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
	/********************************************************************************************/
    public URI getURIF28() throws URISyntaxException {
    	if (uriF28==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF28 = uri.getUUID("Expression_Creation","F28", uuid.get());
    	}
    	return uriF28;
    }
    
    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	uriF28 = getURIF28();
    	Resource F28 = modelF28.createResource(uriF28.toString());
    	
    	/**************************** Work: created a realisation *******************************/
    	PF14_IndividualWork F14= new PF14_IndividualWork();
    	F28.addProperty(modelF28.createProperty(frbroo+ "R19_created_a_realisation_of"), modelF28.createResource(F14.getURIF14().toString()));
    	
    	/**************************** Expression: created ***************************************/
    	PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
    	F28.addProperty(modelF28.createProperty(frbroo+ "R17_created"), modelF28.createResource(F22.getURIF22().toString()));
    	
    	/**************************** Work: Date of the work (expression représentative) ********/
    	if (!(getDateMachine(Converter.getFile()).equals(""))){
    	RDFDatatype W3CDTF = TypeMapper.getInstance().getSafeTypeByName(dcterms + "terms-W3CDTF");
    	F28.addProperty(modelF28.createProperty(cidoc+ "P4_has_time_span"), modelF28.createResource()
    			.addProperty(modelF28.createProperty(rdf+ "type"), modelF28.createResource(cidoc+"E52_Time_Span"))
    			.addProperty(modelF28.createProperty(cidoc+ "P82_at_some_time_within"), ResourceFactory.createTypedLiteral(getDateMachine(Converter.getFile()), W3CDTF)));
    	}
    	/**************************** Work: Date of the work (expression représentative) ********/
    	F28.addProperty(modelF28.createProperty(cidoc+ "P3_has_note"), getDateText(Converter.getFile()));
    //	F28.addProperty(modelF28.createProperty(cidoc+ "P3_has_note"), modelF28.createLiteral("Manel","fr"));
    	
    	/**************************** Work: Period of the work **********************************/
    	if (!(getPeriod(Converter.getFile()).equals(""))){
        	F28.addProperty(modelF28.createProperty(cidoc+ "P10_falls_within"), modelF28.createResource()
        			.addProperty(modelF28.createProperty(rdf+ "type"), modelF28.createResource(cidoc+"E4_Period"))
        			.addProperty(modelF28.createProperty(cidoc+ "P1_is_identified_by"), getPeriod(Converter.getFile())));
        	}
    	
    	/**************************** Work: is created by ***************************************/
    	if (!(getComposer(Converter.getFile()).equals(""))){
    	F28.addProperty(modelF28.createProperty(cidoc+ "P9_consists_of"), modelF28.createResource()
        			.addProperty(modelF28.createProperty(rdf+ "type"), modelF28.createResource(cidoc+"E7_activity"))
    				.addProperty(modelF28.createProperty(cidoc+ "U35_had_function_of_type"), "compositeur")
    				.addProperty(modelF28.createProperty(cidoc+ "P14_carried_out_by"), modelF28.createResource()
    						.addProperty(modelF28.createProperty(rdf+ "type"), modelF28.createResource(cidoc+"E21_Person"))
    						.addProperty(modelF28.createProperty(cidoc+ "P131_is_identified_by"), getComposer(Converter.getFile())
    						))
    				);
    	}
    	
    	/**************************** création d'une expression de plan d'exécution *************/
    	PF25_PerformancePlan F25= new PF25_PerformancePlan();
    	F28.addProperty(modelF28.createProperty(frbroo+ "R17_created"), modelF28.createResource(F25.getURIF25().toString()));
    	
    	/**************************** création d'une expression de plan d'exécution *************/
    	PF25_AutrePerformancePlan FA25= new PF25_AutrePerformancePlan();
    	F28.addProperty(modelF28.createProperty(frbroo+ "R17_created"), modelF28.createResource(FA25.getURIF25().toString()));
		return modelF28;
    }
    /********************************************************************************************/
	
	/************* Date de creation de l'expression (Format machine) ***********************/
    public static String getDateMachine(String xmlFile) throws FileNotFoundException {
    	 StringBuilder buffer = new StringBuilder();
         InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
         MarcXmlReader reader = new MarcXmlReader(file);
         String dateG ="", dateH="";
         while (reader.hasNext()) { // Parcourir le fichier MARCXML
         	 Record s = reader.next();
         	 for (int i=0; i<s.dataFields.size(); i++) {
         		 if (s.dataFields.get(i).getEtiq().equals("909")) {
         			 if (s.dataFields.get(i).isCode('g')){
         			 	 dateG = s.dataFields.get(i).getSubfield('g').getData();
         			 }
         			if (s.dataFields.get(i).isCode('h')){
        			 	 dateH = s.dataFields.get(i).getSubfield('h').getData();
        			 }
         		 }
         	 }
         }
         if (dateG.equals(dateH)) {buffer.append(dateG);}
         else {
        	 buffer.append(dateG); 
        	 buffer.append("/");
        	 buffer.append(dateH);
         }
         return buffer.toString();
    }
	/************* Date de création de l'expression (Format texte) ***********************/
    public static String getDateText(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("909")) {
        			 if (s.dataFields.get(i).isCode('b')){
        			 	 String date = s.dataFields.get(i).getSubfield('b').getData();
        				 buffer.append(date);
        			 }
        		 }
        	 }
        }
        return buffer.toString();
    }
    
    /************* Période de création de l'expression ********************************/
    public static String getPeriod(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String period ="", value="";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("610")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 	 period = s.dataFields.get(i).getSubfield('a').getData();
        			 }
        			 if (s.dataFields.get(i).isCode('b')){
        			 	 value = s.dataFields.get(i).getSubfield('b').getData();
        			 }
        		 }
        	 }
        }
        if (value.equals("02")) buffer.append(period);
        else buffer.append("");
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
	 
    /***************** Le compositeur qui a crée l'oeuvre ******************************/
    public static String getComposer(String xmlFile) throws FileNotFoundException {
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
}