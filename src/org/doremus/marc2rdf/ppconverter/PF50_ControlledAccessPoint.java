package org.doremus.marc2rdf.ppconverter;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class PF50_ControlledAccessPoint {

/*** Correspond à l'attribution d'identifiant pour l'oeuvre ***/
	
	static Model modelF50;
	static URI uriF50;
	
	public PF50_ControlledAccessPoint() throws URISyntaxException{
		this.modelF50 = ModelFactory.createDefaultModel();
		this.uriF50= getURIF50();
	}
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF50() throws URISyntaxException {
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF50 = uri.getUUID("Controlled_Access_Point","F50", uuid.get());
    	return uriF50;
    }
    
    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	
    	Resource F50 = modelF50.createResource(uriF50.toString());
    	
    	/**************************** Identification de l'œuvre *********************************/
    	F50.addProperty(modelF50.createProperty(cidoc+ "P1i_identifies"), modelF50.createResource(PF14_IndividualWork.uriF14.toString()));
    	
    	/**************************** Identification de l'expression ****************************/
    	F50.addProperty(modelF50.createProperty(cidoc+ "P1i_identifies"), modelF50.createResource(PF22_SelfContainedExpression.uriF22.toString()));
    	
    	/**************************** Attribution d'identifiant dénomination ********************/
    	F50.addProperty(modelF50.createProperty(frbroo+ "R8_consists_of"), modelF50.createResource(PM18_DenominationControlledAccessPoint.uriM18.toString()));
    	
    	return modelF50;
}
}
