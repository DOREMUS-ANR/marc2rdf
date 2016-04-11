package org.doremus.marc2rdf.ppconverter;

import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class PF19_PublicationWork {

	static Model modelF19 = ModelFactory.createDefaultModel();
	static URI uriF19 = null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF19() throws URISyntaxException {
    	if (uriF19==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF19 = uri.getUUID("Publication_Work","F19", uuid.get());
    	}
    	return uriF19;
    }
    
    /********************************************************************************************/
    public Model getModel() throws URISyntaxException{
    	uriF19 = getURIF19();
    	Resource F19 = modelF19.createResource(uriF19.toString());
    	
    	/**************************** réalisation d'une work ************************************/
    	PF24_PublicationExpression F24= new PF24_PublicationExpression();
    	F19.addProperty(modelF19.createProperty(frbroo+ "R3_is_realised_in"), modelF19.createResource(F24.getURIF24().toString()));
    	
		return modelF19;
    }
    /********************************************************************************************/
}
