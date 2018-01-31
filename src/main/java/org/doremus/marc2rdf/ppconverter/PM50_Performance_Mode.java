package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class PM50_Performance_Mode extends DoremusResource {
  public PM50_Performance_Mode(String uri, String label) throws URISyntaxException {
    super();

    this.uri = new URI(uri);
    this.resource = model.createResource(this.uri.toString());

    this.resource.addProperty(RDF.type, MUS.M50_Performance_Mode)
      .addProperty(RDFS.label, label)
      .addProperty(CIDOC.P1_is_identified_by, label);
  }
}
