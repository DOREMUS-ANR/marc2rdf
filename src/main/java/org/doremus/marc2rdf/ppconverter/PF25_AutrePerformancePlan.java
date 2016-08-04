package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF25_AutrePerformancePlan {
  private Model model;
  private URI uriF25;
  private Resource F25;

  public PF25_AutrePerformancePlan() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF25 = ConstructURI.build("Performance_Plan", "F25");

    F25 = model.createResource(uriF25.toString());
    F25.addProperty(RDF.type, FRBROO.F25_Performance_Plan);
  }

  public Model getModel() {
    return model;
  }

  public Resource asResource() {
    return F25;
  }

  public PF25_AutrePerformancePlan add(PF22_SelfContainedExpression f22) {
    /**************************** Expression: 1ère exécution********************************/
    F25.addProperty(CIDOC.P165_incorporates, f22.asResource());
//    f22.asResource().addProperty(model.createProperty(cidoc + "P165i_is_incorporated_in"), F25);
    return this;
  }
}
