package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF42_representativeExpressionAssignment {

  static Model modelF42;
  static URI uriF42;

  public PF42_representativeExpressionAssignment() throws URISyntaxException {
    this.modelF42 = ModelFactory.createDefaultModel();
    this.uriF42 = getURIF42();
  }

  String mus = "http://data.doremus.org/ontology/";
  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";

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
    F42.addProperty(FRBROO.R44_carried_out_by, modelF42.createResource("http://data.doremus.org/Philharmonie_de_Paris"));

    /**************************** Attribution à ***********************************************/
    F42.addProperty(FRBROO.R50_assigned_to, modelF42.createResource(PF15_ComplexWork.uriF15.toString()));

    /**************************** Attribution à ***********************************************/
    F42.addProperty(modelF42.createProperty(frbroo + "R51_assigned"), modelF42.createResource(PF22_SelfContainedExpression.uriF22.toString()));

    return modelF42;
  }
  /********************************************************************************************/

}
