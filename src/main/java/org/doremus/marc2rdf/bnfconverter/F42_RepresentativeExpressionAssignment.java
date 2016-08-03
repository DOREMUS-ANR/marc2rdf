package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F42_RepresentativeExpressionAssignment {
  private Model modelF42 = ModelFactory.createDefaultModel();
  private final Resource F42;
  static URI uriF42 = null;

  public F42_RepresentativeExpressionAssignment() throws URISyntaxException {
    this.modelF42 = ModelFactory.createDefaultModel();
    this.uriF42 = ConstructURI.build("Representative_Expression_Assignment", "F42");

    F42 = modelF42.createResource(uriF42.toString());
    F42.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);

    /**************************** Responsable de l'attribution **********************************/
    F42.addProperty(FRBROO.R44_carried_out_by, modelF42.createResource("http://isni.org/isni/0000000121751303"));
  }

  public F42_RepresentativeExpressionAssignment add(F22_SelfContainedExpression expression) {
    /**************************** Attribution à ***********************************************/
    F42.addProperty(FRBROO.R51_assigned, expression.asResource());
    return this;
  }

  public F42_RepresentativeExpressionAssignment add(F15_ComplexWork f15) {
    /**************************** Attribution à ***********************************************/
    F42.addProperty(FRBROO.R50_assigned_to, f15.asResource());
    return this;
  }

  public Model getModel() throws URISyntaxException {
    return modelF42;
  }

}
