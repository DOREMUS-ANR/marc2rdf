package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class CorporateBody extends Artist {
  private String name;
  public CorporateBody(String name) throws URISyntaxException, NullPointerException {
    super();
    if (name == null) throw new RuntimeException("Missing artist name");

    this.name = name;
    this.uri = ConstructURI.build("F11_CorporateBody", name);
    initResource();
  }

  private void initResource() {
    this.resource = model.createResource(this.uri.toString());

    resource.addProperty(RDF.type, FRBROO.F11_Corporate_Body)
      .addProperty(RDFS.label, this.name)
      .addProperty(CIDOC.P131_is_identified_by, this.name);
  }

  public String getName() {
    return name;
  }

  public Model getModel() {
    return model;
  }

  public URI getUri() {
    return uri;
  }

  public static CorporateBody fromUnimarcField(DataField field) throws URISyntaxException {
    String name = field.getString('a');
    if (name == null) return null;

    return new CorporateBody(name.trim());
  }

  @Override
  public String getFullName() {
    return name;
  }
}