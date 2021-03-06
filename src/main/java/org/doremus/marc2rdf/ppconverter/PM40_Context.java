package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class PM40_Context extends DoremusResource {
  public PM40_Context(String label) {
    super();

    try {
      this.uri = ConstructURI.build("M40_Context", label).toString();
      this.resource = model.createResource(this.uri);
      this.resource.addProperty(RDF.type, MUS.M40_Context)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P1_is_identified_by, label);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

  }
}
