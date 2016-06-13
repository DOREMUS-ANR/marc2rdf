package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F14_IndividualWork {

  /***
   * Correspond à l'expression représentative de l'oeuvre musicale
   ***/

  static Model modelF14 = ModelFactory.createDefaultModel();
  static URI uriF14 = null;

  public F14_IndividualWork() throws URISyntaxException {
    this.modelF14 = ModelFactory.createDefaultModel();
    this.uriF14 = getURIF14();
  }

  String mus = "http://data.doremus.org/ontology/";
  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";

  /********************************************************************************************/
  public URI getURIF14() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriF14 = uri.getUUID("Individual_Work", "F14", uuid.get());
    return uriF14;
  }

  /********************************************************************************************/
  public Model getModel() throws URISyntaxException {

    Resource F14 = modelF14.createResource(uriF14.toString());
    F14.addProperty(RDF.type, FRBROO.F14_Individual_Work);

    /**************************** Work: is member of (Work) *********************************/
    F14.addProperty(modelF14.createProperty(frbroo + "R10i_is_member_of"), modelF14.createResource(F15_ComplexWork.uriF15.toString()));

    /**************************** Work: realised through ************************************/
    F14.addProperty(modelF14.createProperty(frbroo + "R19i_was_realised_through"), modelF14.createResource(F28_ExpressionCreation.uriF28.toString()));

    /**************************** Work: is realised through (expression) ********************/
    F14.addProperty(modelF14.createProperty(frbroo + "R9_is_realised_in"), modelF14.createResource(F22_SelfContainedExpression.uriF22.toString()));

    /**************************** Work: was assigned by *************************************/
    F14.addProperty(modelF14.createProperty(frbroo + "R45i_was_assigned_by"), modelF14.createResource(F40_IdentifierAssignment.uriF40.toString()));

    /**************************** Work: had princeps publication ****************************/
    F14.addProperty(modelF14.createProperty(mus + "U4_had_princeps_publication"), modelF14.createResource(F30_PublicationEvent.uriF30.toString()));

    /**************************** Work: had premiere ****************************************/
    F14.addProperty(modelF14.createProperty(mus + "U5_had_premiere"), modelF14.createResource(F31_Performance.uriF31.toString()));

    return modelF14;
  }
  /********************************************************************************************/
}
