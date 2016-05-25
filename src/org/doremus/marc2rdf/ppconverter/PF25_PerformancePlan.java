package org.doremus.marc2rdf.ppconverter;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class PF25_PerformancePlan {

	static Model modelF25 = ModelFactory.createDefaultModel();
	static URI uriF25=null;

	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";

	/********************************************************************************************/
    public URI getURIF25() throws URISyntaxException {
    	if (uriF25==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF25 = uri.getUUID("Performance_Plan","F25", uuid.get());
    	}
    	return uriF25;
    }

    /********************************************************************************************/
    public Model getModel() throws URISyntaxException{
    	uriF25 = getURIF25();
    	Resource F25 = modelF25.createResource(uriF25.toString());

    	/**************************** exécution du plan ******************************************/
    	PF31_Performance F31= new PF31_Performance();
    	F25.addProperty(modelF25.createProperty(frbroo+ "R25i_was_performed_by"), modelF25.createResource(F31.getURIF31().toString()));


		return modelF25;
    }
    /********************************************************************************************/
}
