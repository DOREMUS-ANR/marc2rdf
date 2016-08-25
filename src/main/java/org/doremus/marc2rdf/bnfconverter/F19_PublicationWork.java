package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class F19_PublicationWork extends DoremusResource {
  public F19_PublicationWork(String identifier) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(identifier);

    this.uri = ConstructURI.build("bnf", "F19", "Publication_Work", identifier);

    this.resource = this.model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F19_Publication_Work);
  }

  public F19_PublicationWork add(F24_PublicationExpression expression) {
    /**************************** réalisation d'une work ************************************/
    this.resource.addProperty(FRBROO.R3_is_realised_in, expression.asResource());
    return this;
  }
}
