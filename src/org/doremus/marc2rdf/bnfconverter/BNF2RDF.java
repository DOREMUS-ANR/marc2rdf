package org.doremus.marc2rdf.bnfconverter;

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

public class BNF2RDF {

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
		F15_ComplexWork f15 = new F15_ComplexWork();
		model.add(f15.getModel());
		
		F14_IndividualWork f14 = new F14_IndividualWork();
		model.add(f14.getModel());
		
		F28_ExpressionCreation f28 = new F28_ExpressionCreation();
		model.add(f28.getModel());
		
		F22_SelfContainedExpression f22 = new F22_SelfContainedExpression();
		model.add(f22.getModel());
		
		F40_IdentifierAssignment f40 = new F40_IdentifierAssignment ();
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
	    
	    model.write(System.out);
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
