package org.doremus.marc2rdf.bnfconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.bnfparser.MarcXmlReader;
import org.doremus.marc2rdf.bnfparser.Record;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import main.Converter;

public class F28_ExpressionCreation {
	
/*** Correspond à l'expression représentative de l'oeuvre musicale ***/
	
	static Model modelF28 = ModelFactory.createDefaultModel();
	static URI uriF28=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    String dcterms = "http://dublincore.org/documents/dcmi-terms/#";
    
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
    	F14_IndividualWork F14= new F14_IndividualWork();
    	F28.addProperty(modelF28.createProperty(frbroo+ "R19_created_a_realisation_of"), modelF28.createResource(F14.getURIF14().toString()));
    	
    	/**************************** Expression: created ***************************************/
    	F22_SelfContainedExpression F22= new F22_SelfContainedExpression();
    	F28.addProperty(modelF28.createProperty(frbroo+ "R17_created"), modelF28.createResource(F22.getURIF22().toString()));
    	
    	/**************************** Work: Date of the work (expression représentative) ********/
    	RDFDatatype W3CDTF = TypeMapper.getInstance().getSafeTypeByName(dcterms + "terms-W3CDTF");
    	F28.addProperty(modelF28.createProperty(cidoc+ "P4_has_time_span"), ResourceFactory.createTypedLiteral(getDateMachine(Converter.getFile()), W3CDTF));
    	
    	/**************************** Work: Date of the work (expression représentative) ********/
    	F28.addProperty(modelF28.createProperty(cidoc+ "P3_has_note"), getDateText(Converter.getFile()));
    //	F28.addProperty(modelF28.createProperty(cidoc+ "P3_has_note"), modelF28.createLiteral("Manel","fr"));
    	/**************************** Work: is created by ***************************************/
    	F28.addProperty(modelF28.createProperty(cidoc+ "P14_carried_out_by"), getComposer(Converter.getFile()));
        
		return modelF28;
    }
    /********************************************************************************************/
	
	/************* Date de creation de l'expression (Format machine) ***********************/
    public static String getDateMachine(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 
        		 /***************************** Cas 1 *********************************/
        		 if (s.controlFields.get(i).getEtiq().equals("008")) {
        			 if (((s.controlFields.get(i).getData().charAt(36))==((" ").charAt(0)))&&
        			 ((s.controlFields.get(i).getData().charAt(46))==((" ").charAt(0)))){
        			 	for (int j=28; j<=31; j++){
        			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
        			 	}
        			 	String st = "";
        			 	boolean nothing = true;
        			 	for (int j=38; j<=41; j++){
        			 		char character = s.controlFields.get(i).getData().charAt(j);
        			 		st = st+ character;
        			 		if (!(character==(' '))) {nothing = false;}
        			 	}
        			 	if (nothing==false) {
        			 		buffer.append("/");
        			 		buffer.append(st);
        			 	}
        			 }
        			 /***************************** Cas 2 *********************************/
        			 else if (((s.controlFields.get(i).getData().charAt(36))==(("?").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(46))==(("?").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(30))==((".").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(31))==((".").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(40))==((".").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(41))==((".").charAt(0)))){
        				 /******************** Sous cas 2.1 ********************************/
        				 if (((s.controlFields.get(i).getData().charAt(30))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(31))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=28; j<=29; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("00/"+dt+"99");
        				 }
        				 /******************** Sous cas 2.2 ********************************/
        				 if (!((s.controlFields.get(i).getData().charAt(30))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(31))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=28; j<=30; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("0/"+dt+"9");
        				 }
        				 /******************** Sous cas 2.3 ********************************/
        				 if (((s.controlFields.get(i).getData().charAt(40))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(41))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=38; j<=39; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("00/"+dt+"99");
        				 }
        				 /******************** Sous cas 2.4 ********************************/
        				 if (!((s.controlFields.get(i).getData().charAt(40))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(41))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=38; j<=40; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("0/"+dt+"9");
        				 }
        			 }
        			 /******************************************************************************/
        		 }
        	 }
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
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 	 String date = s.dataFields.get(i).getSubfield('a').getData();
        			 	 if ((date.contains("comp."))||(date.contains("Date de composition"))||(date.contains("Dates de composition")))
        				 buffer.append(date);
        			 }
        		 }
        	 }
        }
        return buffer.toString();
    }
    
    /***************** Le compositeur qui a crée l'oeuvre ******************************/
    public static String getComposer(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String aSubField = "", mSubField="", dSubField="",eSubField="",hSubField="", uSubField = "";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("100")) {
        			/* 	 if (s.dataFields.get(i).isCode('w')){
        			 		 String wSubField = s.dataFields.get(i).getSubfield('w').getData();
        			 		 buffer.append(wSubField+" ");
        			 	 } */
        			 	 if (s.dataFields.get(i).isCode('a')){
        			 		 aSubField = s.dataFields.get(i).getSubfield('a').getData();
        			 	 }
        			 	if (s.dataFields.get(i).isCode('m')){
        			 		 mSubField = s.dataFields.get(i).getSubfield('m').getData();
        			 	}
        			 	if (s.dataFields.get(i).isCode('d')){
        			 		 dSubField = s.dataFields.get(i).getSubfield('d').getData();
        			 	}
        			 	if (s.dataFields.get(i).isCode('e')){
        			 		 eSubField = s.dataFields.get(i).getSubfield('e').getData();
        			 	}
        			 	if (s.dataFields.get(i).isCode('h')){
        			 		 hSubField = s.dataFields.get(i).getSubfield('h').getData();
        			 	}
        			 	if (s.dataFields.get(i).isCode('u')){
        			 		 uSubField = s.dataFields.get(i).getSubfield('u').getData();
        			 	}
        		 }
        	 }
        }
        buffer.append(aSubField); buffer.append(","); buffer.append(mSubField); buffer.append("("); buffer.append(dSubField); buffer.append(")");
        buffer.append(eSubField); buffer.append(hSubField); buffer.append(uSubField);
        return buffer.toString();
    }
}
