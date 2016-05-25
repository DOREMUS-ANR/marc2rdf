package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/***************************************************/

public class BNF2RDF {

	static F15_ComplexWork f15;
	static F14_IndividualWork f14;
	static F19_PublicationWork f19;
	static F28_ExpressionCreation f28;
	static F22_SelfContainedExpression f22;
	static F24_PublicationExpression f24;
	static F42_representativeExpressionAssignment f42;
	static F25_PerformancePlan f25;
	static F31_Performance f31;
	static F25_AutrePerformancePlan fa25;
	static F31_AutrePerformance fa31;
	static F30_PublicationEvent f30;
	static F40_IdentifierAssignment f40;

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
        f15 = new F15_ComplexWork();
		f14 = new F14_IndividualWork();
		f19 = new F19_PublicationWork();
		f28 = new F28_ExpressionCreation();
		f22 = new F22_SelfContainedExpression();
		f24 = new F24_PublicationExpression();
		f42 = new F42_representativeExpressionAssignment();
		f25 = new F25_PerformancePlan();
		f31 = new F31_Performance();
		fa25 = new F25_AutrePerformancePlan();
		fa31 = new F31_AutrePerformance();
		f30 = new F30_PublicationEvent();
		f40 = new F40_IdentifierAssignment ();

		model.add(f15.getModel());
		model.add(f14.getModel());
		model.add(f19.getModel());
		model.add(f28.getModel());
		model.add(f22.getModel());
		model.add(f24.getModel());
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

	    return model;

	    /****************************************************************************************/
	   /* String query = "WITH GRAPH  <DOREMUS>"+
	    		       "delete where {?x ?p \"\" }";
	    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query, model);
	    vur.exec();
		/****************************************************************************************/
  }

  /**************************************************************************/
  public static URI getURI(String path) throws URISyntaxException {
    URIBuilder builder = new URIBuilder().setPath(path);
    URI uri = builder.build();
    return uri;
  }

  /**************************************************************************/
  private static Literal l(String lexicalform, RDFDatatype datatype) {
    return ResourceFactory.createTypedLiteral(lexicalform, datatype);
  }
}
