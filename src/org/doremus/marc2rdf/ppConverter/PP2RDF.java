package org.doremus.marc2rdf.ppConverter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.doremus.marc2rdf.ppConverter.PF14_IndividualWork;
import org.doremus.marc2rdf.ppConverter.PF15_ComplexWork;
import org.doremus.marc2rdf.ppConverter.PF22_SelfContainedExpression;
import org.doremus.marc2rdf.ppConverter.PF25_AutrePerformancePlan;
import org.doremus.marc2rdf.ppConverter.PF25_PerformancePlan;
import org.doremus.marc2rdf.ppConverter.PF28_ExpressionCreation;
import org.doremus.marc2rdf.ppConverter.PF30_PublicationEvent;
import org.doremus.marc2rdf.ppConverter.PF31_AutrePerformance;
import org.doremus.marc2rdf.ppConverter.PF31_Performance;
import org.doremus.marc2rdf.ppConverter.PF40_IdentifierAssignment;
import org.doremus.marc2rdf.ppConverter.PF42_representativeExpressionAssignment;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.vocabulary.RDF;

import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;


/***************************************************/

public class PP2RDF {

	public static Model convert (String file) throws URISyntaxException, IOException {
		
		file= file;
		
		/************* Creer un modele vide **************************/
		//Model model = VirtModel.openDatabaseModel("DOREMUS", "jdbc:virtuoso://localhost:1111", "dba", "dba");
		Model model = ModelFactory.createDefaultModel();
        String mus = "http://data.doremus.org/ontology/";
        String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
        String frbroo = "http://erlangen-crm.org/efrbroo/";
        String xsd = "http://www.w3.org/2001/XMLSchema#";
        String dcterms = "http://dublincore.org/documents/dcmi-terms/#";
        
		/******************************** F15_ComplexWork ***************************************/
		PF15_ComplexWork f15 = new PF15_ComplexWork();
		model.add(f15.getModel());
		
		PF14_IndividualWork f14 = new PF14_IndividualWork();
		model.add(f14.getModel());
		
		PF28_ExpressionCreation f28 = new PF28_ExpressionCreation();
		model.add(f28.getModel());
		
		PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression();
		model.add(f22.getModel());
		
		PF42_representativeExpressionAssignment f42 = new PF42_representativeExpressionAssignment();
		model.add(f42.getModel());
		
		PF25_PerformancePlan f25 = new PF25_PerformancePlan();
		model.add(f25.getModel());
		
		PF31_Performance f31 = new PF31_Performance();
		model.add(f31.getModel());
		
		PF25_AutrePerformancePlan fa25 = new PF25_AutrePerformancePlan();
		model.add(fa25.getModel());
		
		PF31_AutrePerformance fa31 = new PF31_AutrePerformance();
		model.add(fa31.getModel());
		
		PF30_PublicationEvent f30 = new PF30_PublicationEvent();
		model.add(f30.getModel());
		
		PF40_IdentifierAssignment f40 = new PF40_IdentifierAssignment ();
		model.add(f40.getModel());
		
		/****************************************************************************************/
		
		model.setNsPrefix( "mus", mus );
        model.setNsPrefix( "cidoc-crm", cidoc );
        model.setNsPrefix( "frbroo", frbroo );
        model.setNsPrefix( "xsd", xsd );
        model.setNsPrefix( "dcterms", dcterms );
		/****************************************************************************************/
	    String query = "delete where {?x ?p \"\" }";
	    UpdateAction.parseExecute(query, model);
	    
	    model.write(System.out, "TURTLE");
	    return model;
	    
	    /****************************************************************************************/
	   /* String query = "WITH GRAPH  <DOREMUS>"+ 
	    		       "delete where {?x ?p \"\" }";
	    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query, model);
	    vur.exec();  
		/****************************************************************************************/
	}
	
	/**************************************************************************/
	public static URI getURI(String path) throws URISyntaxException  {
		URIBuilder builder = new URIBuilder().setPath(path);
		URI uri = builder.build();
		 return uri;
	}
	/**************************************************************************/
	 private static Literal l ( String lexicalform, RDFDatatype datatype ) {
	        return ResourceFactory.createTypedLiteral ( lexicalform, datatype );
	    }
}
