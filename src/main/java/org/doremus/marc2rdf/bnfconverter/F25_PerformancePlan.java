package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class F25_PerformancePlan extends DoremusResource {

  public F25_PerformancePlan(String identifier) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(identifier);

    this.uri = ConstructURI.build("bnf", "F25", "Performance_Plan", identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    /**************************** création d'une expression de plan d'exécution *************/
    F28_ExpressionCreation planCreation = new F28_ExpressionCreation(identifier);
    planCreation.asResource().addProperty(FRBROO.R17_created, this.resource);
    model.add(planCreation.getModel());
  }

  public F25_PerformancePlan add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P165_incorporates, f22.asResource());
    return this;
  }
}
