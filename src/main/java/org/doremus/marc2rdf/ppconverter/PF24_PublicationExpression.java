package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class PF24_PublicationExpression extends DoremusResource {

  public PF24_PublicationExpression(String identifier) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(identifier);

    this.uri = ConstructURI.build("philharmonie", "F24", "Publication_Expression", this.identifier);

    this.resource = this.model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F24_Publication_Expression);
  }

  public PF24_PublicationExpression add(PF22_SelfContainedExpression expression) {
    /*********** création d'une expression de publication incorporant l'expression **********/
    this.resource.addProperty(CIDOC.P165_incorporates, expression.asResource());
    return this;
  }
}
