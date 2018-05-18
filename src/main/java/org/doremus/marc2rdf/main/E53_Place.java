package org.doremus.marc2rdf.main;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.geonames.Toponym;

import java.net.URISyntaxException;

public class E53_Place extends DoremusResource {
  private String name;

  public E53_Place(String rawString) {
    super();

    this.name = null;
    Toponym tp = GeoNames.query(rawString);
    if (tp != null) {
      // simply download the file
      uri = GeoNames.toURI(tp.getGeoNameId());
      this.name = tp.getName();
    } else {
      try {
        uri = ConstructURI.build("E53_Place", rawString).toString();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    this.regenerateResource(uri);
    this.resource.addProperty(RDF.type, CIDOC.E53_Place)
      .addProperty(RDFS.label, rawString.replaceAll("\\(.+\\)", "").trim(), "fr")
      .addProperty(CIDOC.P1_is_identified_by, rawString, "fr");

    if (this.name != null) {
      this.resource.addProperty(RDFS.label, this.name, "en")
        .addProperty(model.createProperty(GeoNames.NAME), this.name);
    }

  }

}
