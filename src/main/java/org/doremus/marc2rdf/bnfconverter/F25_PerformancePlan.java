package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F25_PerformancePlan {
  private final Resource F25;
  private Model model;
  private URI uriF25 = null;

  public F25_PerformancePlan() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF25 = ConstructURI.build("Performance_Plan", "F25");

    F25 = model.createResource(uriF25.toString());
    F25.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    /**************************** création d'une expression de plan d'exécution *************/
    F28_ExpressionCreation planCreation = new F28_ExpressionCreation();
    planCreation.asResource().addProperty(FRBROO.R17_created, F25);
    model.add(planCreation.getModel());
  }

  public Resource asResource() {
    return F25;
  }

  public Model getModel() {
    return model;
  }

  public F25_PerformancePlan add(F22_SelfContainedExpression f22) {
    F25.addProperty(CIDOC.P165_incorporates, f22.asResource());
    return this;
  }
}
