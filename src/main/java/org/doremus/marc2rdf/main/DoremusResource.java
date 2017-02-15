package org.doremus.marc2rdf.main;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;

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
  private Resource publisher;

  public DoremusResource(String identifier) throws URISyntaxException {
    this.identifier = identifier;
    this.model = ModelFactory.createDefaultModel();

    /* generate URI */
    this.className = this.getClass().getSimpleName();
    this.sourceDb = "bnf";
    if (this.className.startsWith("P")) {
      this.sourceDb = "pp";
      this.className = this.className.substring(1);
      this.publisher = model.createResource(PP2RDF.organizationURI);
    } else
      this.publisher = model.createResource(BNF2RDF.organizationURI);

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
    this.resource.addProperty(DCTerms.publisher, this.publisher);
    return this.resource;
  }

  public Model getModel() {
    return this.model;
  }

  public String getIdentifier() {
    return this.identifier;
  }

}
