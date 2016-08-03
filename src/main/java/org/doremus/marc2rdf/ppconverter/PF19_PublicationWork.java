package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF19_PublicationWork {
  private Model modelF19;
  private URI uriF19;
  private Resource F19;

  public PF19_PublicationWork() throws URISyntaxException {
    this.modelF19 = ModelFactory.createDefaultModel();
    this.uriF19 = ConstructURI.build("Publication_Work", "F19");

    F19 = modelF19.createResource(uriF19.toString());
    F19.addProperty(RDF.type, FRBROO.F19_Publication_Work);
  }

  public Model getModel() {
    return modelF19;
  }

  public PF19_PublicationWork add(PF24_PublicationExpression f24) {
    /**************************** réalisation d'une work ************************************/
    F19.addProperty(FRBROO.R3_is_realised_in, f24.asResource());
    return this;
  }

  public Resource asResource() {
    return F19;
  }
}
