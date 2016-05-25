package org.doremus.marc2rdf.ppconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import org.apache.jena.ontology.Individual;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class PF15_ComplexWork {

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
    	PF14_IndividualWork F14= new PF14_IndividualWork();
    	F15.addProperty(modelF15.createProperty(frbroo+ "R10_has_member"), modelF15.createResource(F14.getURIF14().toString()));

    	/**************************** Work: has representative Expression ***********************/
    	PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
    	F15.addProperty(modelF15.createProperty(frbroo+ "R40_has_representative_expression"), modelF15.createResource(F22.getURIF22().toString()));

    	/**************************** Work: was assigned by *************************************/
    	PF40_IdentifierAssignment F40= new PF40_IdentifierAssignment();
    	F15.addProperty(modelF15.createProperty(frbroo+ "R45i_was_assigned_by"), modelF15.createResource(F40.getURIF40().toString()));

		return modelF15;
    }
    /********************************************************************************************/
}
