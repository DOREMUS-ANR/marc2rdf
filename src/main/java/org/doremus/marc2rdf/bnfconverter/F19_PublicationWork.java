package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F19_PublicationWork {
  private Model modelF19 = ModelFactory.createDefaultModel();
  private Resource F19;
  private URI uriF19;

  public F19_PublicationWork() throws URISyntaxException {
    this.modelF19 = ModelFactory.createDefaultModel();
    this.uriF19 = ConstructURI.build("Publication_Work", "F19");

    F19 = modelF19.createResource(uriF19.toString());
    F19.addProperty(RDF.type, FRBROO.F19_Publication_Work);
  }

  public F19_PublicationWork add(F24_PublicationExpression expression) {
    /**************************** réalisation d'une work ************************************/
    F19.addProperty(FRBROO.R3_is_realised_in, expression.asResource());
    return this;
  }

  public Model getModel() throws URISyntaxException {
    return modelF19;
  }

  public Resource asResource() {
    return F19;
  }
}
