package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F42_representativeExpressionAssignment {

  static Model modelF42 = ModelFactory.createDefaultModel();
  static URI uriF42 = null;

  public F42_representativeExpressionAssignment() throws URISyntaxException {
    this.modelF42 = ModelFactory.createDefaultModel();
    this.uriF42 = getURIF42();
  }

  String mus = "http://data.doremus.org/ontology/";
  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";
  String dcterms = "http://dublincore.org/documents/dcmi-terms/#";

  /********************************************************************************************/
  public URI getURIF42() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriF42 = uri.getUUID("Representative_Expression_Assignment", "F42", uuid.get());
    return uriF42;
  }

  /********************************************************************************************/
  public Model getModel() throws URISyntaxException {

    Resource F42 = modelF42.createResource(uriF42.toString());
    F42.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);

    /**************************** Responsable de l'attribution **********************************/
    F42.addProperty(modelF42.createProperty(frbroo + "R44_carried_out_by"), modelF42.createResource("http://isni.org/isni/0000000121751303"));

    /**************************** Attribution à ***********************************************/
    F42.addProperty(modelF42.createProperty(frbroo + "R50_assigned_to"), modelF42.createResource(F15_ComplexWork.uriF15.toString()));

    /**************************** Attribution à ***********************************************/
    F42.addProperty(modelF42.createProperty(frbroo + "R51_assigned"), modelF42.createResource(F22_SelfContainedExpression.uriF22.toString()));

    return modelF42;
  }
  /********************************************************************************************/

}
