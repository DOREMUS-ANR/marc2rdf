package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class PF24_PublicationExpression extends DoremusResource {

  public PF24_PublicationExpression(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F24_Publication_Expression);
  }

  public PF24_PublicationExpression add(PF22_SelfContainedExpression expression) {
    this.resource.addProperty(CIDOC.P165_incorporates, expression.asResource());
    return this;
  }
}
