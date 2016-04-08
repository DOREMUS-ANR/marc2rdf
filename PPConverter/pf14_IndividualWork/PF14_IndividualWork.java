package pf14_IndividualWork;

import java.net.URI;
import java.net.URISyntaxException;

import ppConverter.ConstructURI;
import ppConverter.GenerateUUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import pf15_ComplexWork.PF15_ComplexWork;
import pf22_SelfContainedExpression.PF22_SelfContainedExpression;
import pf28_ExpressionCreation.PF28_ExpressionCreation;
import pf31_Performance.PF31_Performance;
import pf40_IdentifierAssignment.PF40_IdentifierAssignment;
import pf50_ControlledAccessPoint.PF50_ControlledAccessPoint;

public class PF14_IndividualWork {

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
    	PF15_ComplexWork F15= new PF15_ComplexWork();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R10i_is_member_of"), modelF14.createResource(F15.getURIF15().toString()));
    	
    	/**************************** Work: realised through ************************************/
    	PF28_ExpressionCreation F28= new PF28_ExpressionCreation();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R19i_was_realised_through"), modelF14.createResource(F28.getURIF28().toString()));
    	
    	/**************************** Work: is realised through (expression) ********************/
    	PF22_SelfContainedExpression F22= new PF22_SelfContainedExpression();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R9_is_realised_in"), modelF14.createResource(F22.getURIF22().toString()));
    	
    	/**************************** Work: was assigned by *************************************/
    	PF40_IdentifierAssignment F40= new PF40_IdentifierAssignment();
    	F14.addProperty(modelF14.createProperty(frbroo+ "R45i_was_assigned_by"), modelF14.createResource(F40.getURIF40().toString()));
    	
    	/**************************** Work: is identified by *************************************/
    	PF50_ControlledAccessPoint F50= new PF50_ControlledAccessPoint();
    	F14.addProperty(modelF14.createProperty(cidoc+ "P1_is_identified_by"), modelF14.createResource(F50.getURIF50().toString()));
    	
    	/**************************** Performance: 1ère exécution *************************************/
    	PF31_Performance F31= new PF31_Performance();
    	F14.addProperty(modelF14.createProperty(mus+ "U5_had_premiere"), modelF14.createResource(F31.getURIF31().toString()));
    	
		return modelF14;
    }
    /********************************************************************************************/
}
