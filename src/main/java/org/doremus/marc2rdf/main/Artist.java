package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.net.URI;

public abstract class Artist {
  protected final Model model;
  public Resource resource;
  protected URI uri;

  protected Artist() {
    this.model = ModelFactory.createDefaultModel();
  }

  public Model getModel() {
    return model;
  }

  public abstract String getFullName();

  public Resource asResource() {
    return this.resource;
  }

  public URI getUri() {
    return uri;
  }

}
