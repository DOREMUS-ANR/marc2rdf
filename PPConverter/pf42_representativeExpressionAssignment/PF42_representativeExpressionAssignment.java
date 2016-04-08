package pf42_representativeExpressionAssignment;

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

import ppConverter.ConstructURI;
import ppConverter.GenerateUUID;
import pf14_IndividualWork.PF14_IndividualWork;
import pf15_ComplexWork.PF15_ComplexWork;
import pf22_SelfContainedExpression.PF22_SelfContainedExpression;

public class PF42_representativeExpressionAssignment {
	
	static Model modelF42 = ModelFactory.createDefaultModel();
	static URI uriF42=null;

	String mus = "http://data.doremus.org/ontology/";
	String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
	String frbroo = "http://erlangen-crm.org/efrbroo/";
	String xsd = "http://www.w3.org/2001/XMLSchema#";

	/********************************************************************************************/
	public URI getURIF42() throws URISyntaxException {
		if (uriF42==null){
			ConstructURI uri = new ConstructURI();
			GenerateUUID uuid = new GenerateUUID();
			uriF42 = uri.getUUID("Representative_Expression_Assignment","F42", uuid.get());
		}
		return uriF42;
	}

	/********************************************************************************************/
	public Model getModel() throws URISyntaxException{
		uriF42 = getURIF42();
		Resource F42 = modelF42.createResource(uriF42.toString());

		/**************************** Responsable de l'attribution **********************************/
		F42.addProperty(modelF42.createProperty(frbroo+ "R44_carried_out_by"), modelF42.createResource("http://data.doremus.org/Philharmonie_de_Paris"));

		/**************************** Attribution à ***********************************************/
		PF15_ComplexWork F15= new PF15_ComplexWork();
		F42.addProperty(modelF42.createProperty(frbroo+ "R50_assigned_to"), modelF42.createResource(F15.getURIF15().toString()));

		/**************************** Attribution à ***********************************************/
		PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
		F42.addProperty(modelF42.createProperty(frbroo+ "R51_assigned"), modelF42.createResource(F22.getURIF22().toString()));
		
		return modelF42;
	}
	/********************************************************************************************/

}