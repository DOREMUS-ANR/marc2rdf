package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.MUS;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/
public class PM18_DenominationControlledAccessPoint extends DoremusResource {
  public PM18_DenominationControlledAccessPoint(String identifier) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(identifier);

    this.uri = ConstructURI.build("philharmonie", "F14", "Individual_Work", identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, MUS.M18_Controlled_Access_Point_Denomination);
  }
}
