package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class CorporateBody extends Artist {
  private String name;

  public CorporateBody(String name) throws NullPointerException {
    super();
    if (name == null) throw new RuntimeException("Missing artist name");

    this.name = name;
    try {
      this.uri = ConstructURI.build("F11_CorporateBody", name).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
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

  public String getUri() {
    return uri;
  }

  public static CorporateBody fromUnimarcField(DataField field) {
    String name = field.getString('a');
    if (name == null) return null;

    return new CorporateBody(name.trim());
  }

  public static Resource getFromDoremus(String name) {
    String sparql = "PREFIX rdfs: <" + RDFS.getURI() + ">\n" +
      "PREFIX efrbroo: <" + FRBROO.getURI() + ">\n" +
      "SELECT DISTINCT ?s " +
      "WHERE { " +
      "?s a efrbroo:F11_Corporate_Body; rdfs:label \"" + name + "\"." +
      "}";

    return (Resource) Utils.queryDoremus(sparql, "s");

  }

  @Override
  public String getFullName() {
    return name;
  }
}