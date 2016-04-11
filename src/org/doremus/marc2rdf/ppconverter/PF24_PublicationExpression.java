package org.doremus.marc2rdf.ppconverter;

import java.net.URI;
import java.net.URISyntaxException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class PF24_PublicationExpression {
	
	static Model modelF24 = ModelFactory.createDefaultModel();
	static URI uriF24 = null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF24() throws URISyntaxException {
    	if (uriF24==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF24 = uri.getUUID("Publication_Expression","F24", uuid.get());
    	}
    	return uriF24;
    }
    
    /********************************************************************************************/
    public Model getModel() throws URISyntaxException{
    	uriF24 = getURIF24();
    	Resource F24 = modelF24.createResource(uriF24.toString());
    	
    	/*********** création d'une expression de publication incorporant l'expression **********/
    	PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
    	F24.addProperty(modelF24.createProperty(cidoc+ "P165_incorporates"), modelF24.createResource(F22.getURIF22().toString()));
    	
		return modelF24;
    }
    /********************************************************************************************/
}
