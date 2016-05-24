package org.doremus.marc2rdf.bnfconverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class F42_representativeExpressionAssignment {
	
	static Model modelF42 = ModelFactory.createDefaultModel();
	static URI uriF42=null;
	
	public F42_representativeExpressionAssignment() throws URISyntaxException{
		this.modelF42 = ModelFactory.createDefaultModel();
		this.uriF42= getURIF42();
	}
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    String dcterms = "http://dublincore.org/documents/dcmi-terms/#";
    String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
	/********************************************************************************************/
    public URI getURIF42() throws URISyntaxException {
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF42 = uri.getUUID("Representative_Expression_Assignment","F42", uuid.get());
    	return uriF42;
    }

	/********************************************************************************************/
	public Model getModel() throws URISyntaxException{
		
		Resource F42 = modelF42.createResource(uriF42.toString());

		/**************************** Responsable de l'attribution **********************************/
		F42.addProperty(modelF42.createProperty(frbroo+ "R44_carried_out_by"), modelF42.createResource("http://isni.org/isni/0000000121751303"));

		/**************************** Attribution à ***********************************************/
		F42.addProperty(modelF42.createProperty(frbroo+ "R50_assigned_to"), modelF42.createResource(F15_ComplexWork.uriF15.toString()));

		/**************************** Attribution à ***********************************************/
		F42.addProperty(modelF42.createProperty(frbroo+ "R51_assigned"), modelF42.createResource(F22_SelfContainedExpression.uriF22.toString()));
		
		return modelF42;
	}
	/********************************************************************************************/

}
