package org.doremus.marc2rdf.main;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.doremus.marc2rdf.marcparser.Record;

import java.net.URI;
import java.net.URISyntaxException;


public abstract class DoremusResource {
  protected String className;
  protected String sourceDb;

  protected Model model;
  protected URI uri;
  protected Resource resource;
  protected Record record;
  protected String identifier;

  public DoremusResource(String identifier) throws URISyntaxException {
    this.identifier = identifier;
    this.model = ModelFactory.createDefaultModel();

    /* generate URI */
    this.className = this.getClass().getSimpleName();
    this.sourceDb = "bnf";
    if (this.className.startsWith("P")) {
      this.sourceDb = "pp";
      this.className = this.className.substring(1);
    }
    this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);

    /* create RDF resource */
    this.resource = model.createResource(this.uri.toString());
  }

  public DoremusResource(Record record) throws URISyntaxException {
    this(record.getIdentifier());
    this.record = record;
  }

  public DoremusResource(Record record, String identifier) throws URISyntaxException {
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
