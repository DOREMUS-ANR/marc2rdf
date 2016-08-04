package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F25_AutrePerformancePlan {
  private final Resource F25;
  private Model modelF25 = ModelFactory.createDefaultModel();
  private URI uriF25;

  public F25_AutrePerformancePlan() throws URISyntaxException {
    this.modelF25 = ModelFactory.createDefaultModel();
    this.uriF25 = ConstructURI.build("Performance_Plan", "F25");

    F25 = modelF25.createResource(uriF25.toString());
    F25.addProperty(RDF.type, FRBROO.F25_Performance_Plan);
  }

  public Resource asResource() {
    return F25;
  }

  public Model getModel() {
    return modelF25;
  }
}
