package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;

import java.net.URISyntaxException;

public class E53_Place extends DoremusResource {
  private static final String GEO_NAME = "http://www.geonames.org/ontology#name";

  public E53_Place(String label) throws URISyntaxException {
    super(label);

    this.resource.addProperty(RDF.type, CIDOC.E53_Place)
      .addProperty(RDFS.label, label)
      .addProperty(model.createProperty(GEO_NAME), label)
      .addProperty(CIDOC.P1_is_identified_by, label);
  }

  public void addSurroundingPlace(E53_Place external) {
    this.resource.addProperty(CIDOC.P89_falls_within, external.asResource());
  }
}
