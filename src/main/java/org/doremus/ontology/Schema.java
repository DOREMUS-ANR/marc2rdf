package org.doremus.ontology;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Schema {

  protected static final Property property(String uri) {
    return ResourceFactory.createProperty(NS, uri);
  }

  public static final String NS = "http://schema.org/";

  public static String getURI() {
    return NS;
  }


  public static final Property birthPlace = property("birthPlace");
  public static final Property deathPlace = property("deathPlace");
  public static final Property birthDate = property("birthDate");
  public static final Property deathDate = property("deathDate");
}
