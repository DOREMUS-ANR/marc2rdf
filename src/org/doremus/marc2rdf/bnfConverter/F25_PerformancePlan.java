package org.doremus.marc2rdf.bnfConverter;

import java.net.URI;
import java.net.URISyntaxException;

import org.doremus.marc2rdf.bnfConverter.ConstructURI;
import org.doremus.marc2rdf.bnfConverter.GenerateUUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class F25_PerformancePlan {

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
}
