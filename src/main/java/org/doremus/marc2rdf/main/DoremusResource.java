package org.doremus.marc2rdf.main;


import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.ontology.PROV;
import org.doremus.string2vocabulary.VocabularyManager;
import org.pmw.tinylog.Logger;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DoremusResource {
  protected int activityCount;

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
    this.activityCount = 0;

    if (this.className.startsWith("P")) {
      this.sourceDb = "pp";
      this.className = this.className.substring(1);
      this.publisher = PP2RDF.PHILHARMONIE;
    } else
      this.publisher = BNF2RDF.BnF;
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

  public String getUri() {
    return uri;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void addNote(String text) {
    addNote(text, "fr");
  }

  protected void addNote(String text, String lang) {
    if (text == null) return;
    text = text.trim();
    if (text.isEmpty()) return;

    this.addProperty(RDFS.comment, text, lang)
      .addProperty(CIDOC.P3_has_note, text, lang);
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

  protected String searchInNote(String regex, int group) {
    return searchInNote(Pattern.compile(regex), group);
  }

  protected String searchInNote(String regex) {
    return searchInNote(Pattern.compile(regex), -1);
  }

  protected String searchInNote(Pattern pattern, int group) {
    if (record == null) return null;

    List<String> fields = record.getDatafieldsByCode(200, 'a');
    fields.addAll(record.getDatafieldsByCode(200, 'e'));

    for (String note : fields) {
      Matcher m = pattern.matcher(note);
      if (m.find()) return group < 0 ? note : m.group(group);
    }
    return null;
  }

  public Record getRecord() {
    return record;
  }

  protected void setClass(OntClass _class) {
    this.resource.addProperty(RDF.type, _class);
  }

  public DoremusResource addProperty(Property property, DoremusResource resource) {
    if (resource != null) {
      this.addProperty(property, resource.asResource());
      this.model.add(resource.getModel());
    }
    return this;
  }

  public DoremusResource addProperty(Property property, Artist resource) {
    if (resource != null) {
      this.addProperty(property, resource.asResource());
      this.model.add(resource.getModel());
    }
    return this;
  }

  public DoremusResource addProperty(Property property, Resource resource) {
    if (resource != null) this.resource.addProperty(property, resource);
    return this;
  }

  public DoremusResource addProperty(Property property, String literal) {
    if (literal != null && !literal.isEmpty()) this.resource.addProperty(property, literal.trim());
    return this;
  }

  public DoremusResource addProperty(Property property, String literal, String lang) {
    if (literal != null && !literal.isEmpty()) this.resource.addProperty(property, literal.trim(), lang);
    return this;
  }

  public DoremusResource addProperty(Property property, Literal literal) {
    if (literal != null) this.resource.addProperty(property, literal);
    return this;
  }

  public DoremusResource addProperty(Property property, String literal, XSDDatatype datatype) {
    if (literal != null && !literal.isEmpty()) this.resource.addProperty(property, literal.trim(), datatype);
    return this;
  }

  public void addTimeSpan(TimeSpan timeSpan) {
    if (timeSpan == null) return;
    if (!timeSpan.hasUri()) timeSpan.setUri(this.uri + "/interval");
    if (timeSpan.asResource() == null) return;
    this.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
    this.model.add(timeSpan.getModel());
  }

  public void addActivity(Artist agent, String function) {
    if (agent == null) return;

    Resource activity = model.createResource(this.uri + "/activity/" + ++activityCount)
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(MUS.U31_had_function, function)
      .addProperty(CIDOC.P14_carried_out_by, agent.asResource());

    this.addProperty(CIDOC.P9_consists_of, activity);
    this.model.add(agent.getModel());
  }

  protected void addComplexIdentifier(String identifier, String type) {
    this.addComplexIdentifier(identifier, type, null, null);
  }

  protected void addComplexIdentifier(String identifier, String type, String symbol) {
    this.addComplexIdentifier(identifier, type, null, symbol);
  }

  protected void addComplexIdentifier(String identifier, String type, DoremusResource issuer) {
    this.addComplexIdentifier(identifier, type, issuer, null);
  }

  protected void addComplexIdentifier(String identifier, String type, DoremusResource issuer, String symbol) {
    if (identifier == null) return;
    Resource typeRes = VocabularyManager.searchInCategory(type, "fr", "id", false);

    Resource idRes = model.createResource(this.uri + "/id/" + identifier)
      .addProperty(RDF.type, CIDOC.E42_Identifier)
      .addProperty(CIDOC.P2_has_type, typeRes)
      .addProperty(FRBROO.R8_consists_of, identifier)
      .addProperty(RDFS.label, identifier);

    if (issuer != null) idRes.addProperty(FRBROO.R8_consists_of, issuer.asResource());
    if (symbol != null) idRes.addProperty(FRBROO.R8_consists_of, symbol);

    this.addProperty(CIDOC.P1_is_identified_by, idRes);
  }

}
