package org.doremus.marc2rdf.ppconverter;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class PF50_ControlledAccessPoint {

/*** Correspond à l'attribution d'identifiant pour l'oeuvre ***/

	static Model modelF50 = ModelFactory.createDefaultModel();
	static URI uriF50=null;

	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";

	/********************************************************************************************/
    public URI getURIF50() throws URISyntaxException {
    	if (uriF50==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF50 = uri.getUUID("Controlled_Access_Point","F50", uuid.get());
    	}
    	return uriF50;
    }

    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	uriF50 = getURIF50();
    	Resource F50 = modelF50.createResource(uriF50.toString());

    	/**************************** Identification de l'œuvre *********************************/
    	PF14_IndividualWork F14= new PF14_IndividualWork();
    	F50.addProperty(modelF50.createProperty(cidoc+ "P1i_identifies"), modelF50.createResource(F14.getURIF14().toString()));

    	/**************************** Identification de l'expression ****************************/
    	PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
    	F50.addProperty(modelF50.createProperty(cidoc+ "P1i_identifies"), modelF50.createResource(F22.getURIF22().toString()));

    	/**************************** Attribution d'identifiant dénomination ********************/
    	PM18_DenominationControlledAccessPoint M18 = new PM18_DenominationControlledAccessPoint();
    	F50.addProperty(modelF50.createProperty(frbroo+ "R8_consists_of"), modelF50.createResource(M18.getURIM18().toString()));

    	return modelF50;
}
}
