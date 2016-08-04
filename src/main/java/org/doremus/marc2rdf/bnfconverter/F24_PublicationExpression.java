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

public class F24_PublicationExpression {
  private Model model = ModelFactory.createDefaultModel();
  private final Resource F24;
  private URI uriF24;

  public F24_PublicationExpression() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF24 = ConstructURI.build("Publication_Expression", "F24");

    F24 = model.createResource(uriF24.toString());
    F24.addProperty(RDF.type, FRBROO.F24_Publication_Expression);
  }

  public F24_PublicationExpression add(F22_SelfContainedExpression expression) {
    /*********** création d'une expression de publication incorporant l'expression **********/
    F24.addProperty(CIDOC.P165_incorporates, expression.asResource());
    return this;
  }

  public Resource asResource() {
    return F24;
  }

  public Model getModel() throws URISyntaxException {
    return model;
  }
}
