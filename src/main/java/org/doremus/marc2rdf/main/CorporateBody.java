package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class CorporateBody {
  private final Model model;
  private final URI uri;
  private String name;
  private Resource resource;

  public CorporateBody(String name) throws URISyntaxException, NullPointerException {
    this.name = name;
    this.model = ModelFactory.createDefaultModel();

    if (name == null) throw new RuntimeException("Missing artist name");

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
}