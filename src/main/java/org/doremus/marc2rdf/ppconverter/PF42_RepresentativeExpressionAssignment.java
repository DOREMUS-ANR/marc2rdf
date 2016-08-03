package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF42_RepresentativeExpressionAssignment {
  private Model modelF42;
  private URI uriF42;
  private Resource F42;

  public PF42_RepresentativeExpressionAssignment() throws URISyntaxException {
    this.modelF42 = ModelFactory.createDefaultModel();
    this.uriF42 = ConstructURI.build("Representative_Expression_Assignment", "F42");

    F42 = modelF42.createResource(uriF42.toString());
    F42.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);

    /**************************** Responsable de l'attribution **********************************/
    F42.addProperty(FRBROO.R44_carried_out_by, modelF42.createResource("http://data.doremus.org/Philharmonie_de_Paris"));
  }

  public Model getModel() {
    return modelF42;
  }

  public PF42_RepresentativeExpressionAssignment add(PF22_SelfContainedExpression f22) {
    /**************************** Attribution à ***********************************************/
    F42.addProperty(FRBROO.R51_assigned, f22.asResource());
    return this;
  }

  public PF42_RepresentativeExpressionAssignment add(PF15_ComplexWork f15) {
    /**************************** Attribution à ***********************************************/
    F42.addProperty(FRBROO.R50_assigned_to, f15.asResource());
    return this;
  }
}
