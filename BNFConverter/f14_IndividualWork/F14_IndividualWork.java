package f14_IndividualWork;

import java.net.URI;
import java.net.URISyntaxException;

import f28_ExpressionCreation.F28_ExpressionCreation;
import bnfConverter.ConstructURI;
import bnfConverter.GenerateUUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import f15_ComplexWork.F15_ComplexWork;
import f22_SelfContainedExpression.F22_SelfContainedExpression;
import f30_PublicationEvent.F30_PublicationEvent;
import f31_Performance.F31_Performance;
import f40_IdentifierAssignment.F40_IdentifierAssignment;

public class F14_IndividualWork {

	/*** Correspond à l'expression représentative de l'oeuvre musicale ***/
	
	static Model modelF14 = ModelFactory.createDefaultModel();
	static URI uriF14=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF14() throws URISyntaxException {
    	if (uriF14==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF14 = uri.getUUID("Individual_Work","F14", uuid.get());
    	}
    	return uriF14;
    }
    
    /********************************************************************************************/
    public Model getModel() throws URISyntaxException{
    	uriF14 = getURIF14();
    	Resource F14 = modelF14.createResource(uriF14.toString());
    	
    	/**************************** Work: is member of (Work) *********************************/
    	F15_ComplexWork F15= new F15_ComplexWork();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R10i_is_member_of"), modelF14.createResource(F15.getURIF15().toString()));
    	
    	/**************************** Work: realised through ************************************/
    	F28_ExpressionCreation F28= new F28_ExpressionCreation();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R19i_was_realised_through"), modelF14.createResource(F28.getURIF28().toString()));
    	
    	/**************************** Work: is realised through (expression) ********************/
    	F22_SelfContainedExpression F22= new F22_SelfContainedExpression();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R9_is_realised_in"), modelF14.createResource(F22.getURIF22().toString()));
    	
    	/**************************** Work: was assigned by *************************************/
    	F40_IdentifierAssignment F40= new F40_IdentifierAssignment();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R45i_was_assigned_by"), modelF14.createResource(F40.getURIF40().toString()));
    	
    	/**************************** Work: had princeps publication ****************************/
    	F30_PublicationEvent F30= new F30_PublicationEvent();
    	F14.addProperty(modelF14.createProperty(mus+ "U4_had_princeps_publication"), modelF14.createResource(F30.getURIF30().toString()));
    	
    	/**************************** Work: had premiere ****************************************/
    	F31_Performance F31= new F31_Performance();
    	F14.addProperty(modelF14.createProperty(mus+ "U5_had_premiere"), modelF14.createResource(F31.getURIF31().toString()));
    	
		return modelF14;
    }
    /********************************************************************************************/
}
