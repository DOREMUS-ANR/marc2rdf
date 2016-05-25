package org.doremus.marc2rdf.ppconverter;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class PM18_DenominationControlledAccessPoint {

/*** Correspond à l'attribution d'identifiant pour l'oeuvre ***/

	static Model modelM18 = ModelFactory.createDefaultModel();
	static URI uriM18=null;

	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";

	/********************************************************************************************/
    public URI getURIM18() throws URISyntaxException {
    	if (uriM18==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriM18 = uri.getUUID("Denomination_Controlled_Access_Point","M18", uuid.get());
    	}
    	return uriM18;
    }

    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	uriM18 = getURIM18();
    	Resource M18 = modelM18.createResource(uriM18.toString());

    	/**************************** Work: was assigned by *************************************/
    	PF50_ControlledAccessPoint F50= new PF50_ControlledAccessPoint();
    	M18.addProperty(modelM18.createProperty(frbroo+ "R46_assigned"), modelM18.createResource(F50.getURIF50().toString()));

    	return modelM18;
    }
    }
