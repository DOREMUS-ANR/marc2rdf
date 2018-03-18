package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class PF19_PublicationWork extends DoremusResource{

  public PF19_PublicationWork(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F19_Publication_Work);
  }

  public PF19_PublicationWork add(PF24_PublicationExpression f24) {
    this.resource.addProperty(FRBROO.R3_is_realised_in, f24.asResource());
    return this;
  }
}
