package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class F24_PublicationExpression extends DoremusResource {
  public F24_PublicationExpression(String identifier) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(identifier);

    this.uri = ConstructURI.build("bnf", "F24", "Publication_Expression", identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F24_Publication_Expression);
  }


  public F24_PublicationExpression add(F22_SelfContainedExpression expression) {
    /*********** création d'une expression de publication incorporant l'expression **********/
    this.resource.addProperty(CIDOC.P165_incorporates, expression.asResource());
    return this;
  }

}
