package org.doremus.marc2rdf.main;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;
import org.pmw.tinylog.Logger;

import java.net.URISyntaxException;

public abstract class DoremusResource {
  protected String className;
  protected String sourceDb;

  protected Model model;
  protected String uri;
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
      this.publisher = PP2RDF.PHILHARMONIE;
    } else
      this.publisher = model.createResource(BNF2RDF.organizationURI);
  }

  public DoremusResource(String identifier) {
    this();
    this.identifier = identifier;

    /* create RDF resource */
    regenerateResource();
  }

  public DoremusResource(Record record) {
    this(record.getIdentifier());
    this.record = record;
  }

  public DoremusResource(Record record, String identifier) {
    this(identifier);
    this.record = record;
  }


  protected void regenerateResource() {
    try {
      regenerateResource(ConstructURI.build(this.sourceDb, this.className, this.identifier).toString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  protected void regenerateResource(String uri) {
    this.uri = uri;

    // delete old one
    if (this.resource != null) this.resource.removeProperties();

    // generate the new one
    this.resource = model.createResource(this.uri);
  }

  public void addProvenance(Resource intermarcRes, Resource provActivity) {
    this.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, PP2RDF.DOREMUS)
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

  protected void log(String message) {
    Logger.info(record.getIdentifier() + " | " + message);
  }

  protected static void log(String message, Record record) {
    Logger.info(record.getIdentifier() + " | " + message);
  }

  public void setUri(String uri) {
    if (this.uri == null || uri.equals(this.uri)) return;

    this.uri = uri;
    if (this.resource != null)
      this.resource = ResourceUtils.renameResource(this.resource, uri);
  }
}
