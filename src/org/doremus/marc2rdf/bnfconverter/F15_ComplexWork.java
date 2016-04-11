package org.doremus.marc2rdf.bnfconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.doremus.marc2rdf.bnfparser.MarcXmlReader;
import org.doremus.marc2rdf.bnfparser.Record;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class F15_ComplexWork {
	
						/*** Correspond à l'oeuvre musicale ***/
	
	static Model modelF15 = ModelFactory.createDefaultModel();
	static URI uriF15=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF15() throws URISyntaxException {
    	if (uriF15==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF15 = uri.getUUID("Complex_Work","F15", uuid.get());
    	}
    	return uriF15;
    }
    
    /********************************************************************************************/
    public Model getModel() throws URISyntaxException{
    	uriF15 = getURIF15();
    	Resource F15 = modelF15.createResource(uriF15.toString());
    	
    	/**************************** Work: has member  (Work) **********************************/
    	F14_IndividualWork F14= new F14_IndividualWork();
    	F15.addProperty(modelF15.createProperty(frbroo+ "R10_has_member"), modelF15.createResource(F14.getURIF14().toString()));
    	
    	/**************************** Work: has representative Expression ***********************/
    	F22_SelfContainedExpression F22= new F22_SelfContainedExpression();
    	F15.addProperty(modelF15.createProperty(frbroo+ "R40_has_representative_expression"), modelF15.createResource(F22.getURIF22().toString()));
    	
    	/**************************** Work: was assigned by *************************************/
    	F40_IdentifierAssignment F40= new F40_IdentifierAssignment();
    	F15.addProperty(modelF15.createProperty(frbroo+ "R45i_was_assigned_by"), modelF15.createResource(F40.getURIF40().toString()));
    	
		return modelF15;
    }
    /********************************************************************************************/
}
