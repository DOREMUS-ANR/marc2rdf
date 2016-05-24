package org.doremus.marc2rdf.ppconverter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

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

	static Model model;
	static PF15_ComplexWork f15;
	static PF14_IndividualWork f14;
	static PF28_ExpressionCreation f28;
	static PF22_SelfContainedExpression f22;
	static PF42_representativeExpressionAssignment f42;
	static PF25_PerformancePlan f25;
	static PF31_Performance f31;
	static PF25_AutrePerformancePlan fa25;
	static PF31_AutrePerformance fa31;
	static PF30_PublicationEvent f30;
	static PF40_IdentifierAssignment f40;
	static PF50_ControlledAccessPoint f50;
	static PF24_PublicationExpression f24;
	static PF19_PublicationWork f19;
	static PM18_DenominationControlledAccessPoint m18;
	
	public PP2RDF() throws URISyntaxException{
        model = ModelFactory.createDefaultModel();
		f15 = new PF15_ComplexWork();
		f14 = new PF14_IndividualWork();
		f28 = new PF28_ExpressionCreation();
		f22 = new PF22_SelfContainedExpression();
		f42 = new PF42_representativeExpressionAssignment();
		f25 = new PF25_PerformancePlan();
		f31 = new PF31_Performance();
		fa25 = new PF25_AutrePerformancePlan();
		fa31 = new PF31_AutrePerformance();
		f30 = new PF30_PublicationEvent();
		f40 = new PF40_IdentifierAssignment ();
        }
	
	public static Model convert (String file) throws URISyntaxException, IOException {
		
		
		/************* Creer un modele vide **************************/
		//Model model = VirtModel.openDatabaseModel("DOREMUS", "jdbc:virtuoso://localhost:1111", "dba", "dba");
		Model model = ModelFactory.createDefaultModel();
        String mus = "http://data.doremus.org/ontology/";
        String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
        String frbroo = "http://erlangen-crm.org/efrbroo/";
        String xsd = "http://www.w3.org/2001/XMLSchema#";
        String dcterms = "http://dublincore.org/documents/dcmi-terms/#";
        
		/******************************** F15_ComplexWork ***************************************/
		model.add(f15.getModel());
		model.add(f14.getModel());
		model.add(f28.getModel());
		model.add(f22.getModel());
		model.add(f42.getModel());
		model.add(f25.getModel());
		model.add(f31.getModel());
		model.add(fa25.getModel());
		model.add(fa31.getModel());
		model.add(f30.getModel());
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
