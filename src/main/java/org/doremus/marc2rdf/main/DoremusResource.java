package org.doremus.marc2rdf.main;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.doremus.marc2rdf.marcparser.Record;

import java.net.URI;


public abstract class DoremusResource {
  protected Model model;
  protected URI uri;
  protected Resource resource;
  protected Record record;
  protected String identifier;

  public DoremusResource(String identifier) {
    this.identifier = identifier;
    this.model = ModelFactory.createDefaultModel();
  }

  public DoremusResource(Record record) {
    this(record.getIdentifier());
    this.record = record;
  }

  public DoremusResource(Record record, String identifier) {
    this(identifier);
    this.record = record;
  }


  public Resource asResource() {
    return this.resource;
  }

  public Model getModel() {
    return this.model;
  }

  public String getIdentifier() {
    return this.identifier;
  }

}
