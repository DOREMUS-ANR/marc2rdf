package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class PF19_PublicationWork extends DoremusResource{

  public PF19_PublicationWork(String identifier) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(identifier);

    this.uri = ConstructURI.build("philharmonie", "F19", "Publication_Work", this.identifier);

    this.resource = this.model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F19_Publication_Work);
  }

  public PF19_PublicationWork add(PF24_PublicationExpression f24) {
    /**************************** réalisation d'une work ************************************/
    this.resource.addProperty(FRBROO.R3_is_realised_in, f24.asResource());
    return this;
  }
}
