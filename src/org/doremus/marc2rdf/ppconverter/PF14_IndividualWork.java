package org.doremus.marc2rdf.ppconverter;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class PF14_IndividualWork {

	/*** Correspond à l'expression représentative de l'oeuvre musicale ***/

	static Model modelF14;
	static URI uriF14;

	public PF14_IndividualWork() throws URISyntaxException{
		this.modelF14 = ModelFactory.createDefaultModel();
		this.uriF14= getURIF14();
	}

	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";

	/********************************************************************************************/
    public URI getURIF14() throws URISyntaxException {
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF14 = uri.getUUID("Individual_Work","F14", uuid.get());
    	return uriF14;
    }

    /********************************************************************************************/
    public Model getModel() throws URISyntaxException{

    	Resource F14 = modelF14.createResource(uriF14.toString());

    	/**************************** Work: is member of (Work) *********************************/
    	F14.addProperty(modelF14.createProperty(frbroo+ "R10i_is_member_of"), modelF14.createResource(PF15_ComplexWork.uriF15.toString()));

    	/**************************** Work: realised through ************************************/
    	F14.addProperty(modelF14.createProperty(frbroo+ "R19i_was_realised_through"), modelF14.createResource(PF28_ExpressionCreation.uriF28.toString()));

    	/**************************** Work: is realised through (expression) ********************/
    	F14.addProperty(modelF14.createProperty(frbroo+ "R9_is_realised_in"), modelF14.createResource(PF22_SelfContainedExpression.uriF22.toString()));

    	/**************************** Work: was assigned by *************************************/
    	F14.addProperty(modelF14.createProperty(frbroo+ "R45i_was_assigned_by"), modelF14.createResource(PF40_IdentifierAssignment.uriF40.toString()));

    	/**************************** Work: is identified by *************************************/
    	F14.addProperty(modelF14.createProperty(cidoc+ "P1_is_identified_by"), modelF14.createResource(PF50_ControlledAccessPoint.uriF50.toString()));

    	/**************************** Performance: 1ère exécution *************************************/
    	F14.addProperty(modelF14.createProperty(mus+ "U5_had_premiere"), modelF14.createResource(PF31_Performance.uriF31.toString()));

		return modelF14;
    }
    /********************************************************************************************/
}
