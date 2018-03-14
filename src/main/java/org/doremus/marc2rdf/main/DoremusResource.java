package org.doremus.marc2rdf.main;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;

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

  public DoremusResource() {
    // do nothing, enables customisation for child class
    this.model = ModelFactory.createDefaultModel();
    this.className = this.getClass().getSimpleName();
    this.sourceDb = "bnf";
    this.resource = null;

    if (this.className.startsWith("P")) {
      this.sourceDb = "pp";
      this.className = this.className.substring(1);
      this.publisher = model.createResource(PP2RDF.organizationURI);
    } else
      this.publisher = model.createResource(BNF2RDF.organizationURI);
  }

  public DoremusResource(String identifier) throws URISyntaxException {
    this();
    this.identifier = identifier;

    /* create RDF resource */
    regenerateResource();
  }

  public DoremusResource(Record record) throws URISyntaxException {
    this(record.getIdentifier());
    this.record = record;
  }

  public DoremusResource(Record record, String identifier) throws URISyntaxException {
    this(identifier);
    this.record = record;
  }


  protected void regenerateResource() throws URISyntaxException {
    // delete old one
    if (this.resource != null) this.resource.removeProperties();

    // generate the new one
    this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);
    this.resource = model.createResource(this.uri.toString());
  }

  public void addProvenance(Resource intermarcRes, Resource provActivity) {
    this.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, model.createResource(PP2RDF.doremusURI))
      .addProperty(PROV.wasDerivedFrom, intermarcRes)
      .addProperty(PROV.wasGeneratedBy, provActivity);
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

  public void addNote(String text) {
    if (text == null) return;
    text = text.trim();
    if (text.isEmpty()) return;

    this.resource
      .addProperty(RDFS.comment, text, "fr")
      .addProperty(CIDOC.P3_has_note, text, "fr");
  }

}
