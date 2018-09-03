package org.doremus.marc2rdf.main;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.bnfconverter.FunctionMap;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class CorporateBody extends Artist {
  private String name;

  public CorporateBody(String name) {
    this(name, null);
  }

  public CorporateBody(String name, String lang) {
    super();
    if (name == null) throw new RuntimeException("Missing artist name");

    this.name = name.trim().replaceAll("^\\[(.+)]$", "$1").trim();
    try {
      this.uri = ConstructURI.build("F11_CorporateBody", name).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.resource = model.createResource(this.uri);

    this.setClass(FRBROO.F11_Corporate_Body);
    this.addProperty(RDFS.label, this.name, lang)
      .addProperty(CIDOC.P131_is_identified_by, this.name, lang);
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

  @Override
  public void addName(String name) {
    resource.addProperty(RDFS.label, this.name)
      .addProperty(CIDOC.P131_is_identified_by, name);
  }

  @Override
  public boolean hasName(String value) {
    if (value == null) return false;
    return this.getFullName().equalsIgnoreCase(value.trim());
  }

  public static CorporateBody fromUnimarcField(DataField field) {
    if (field == null) return null;
    String name = field.getString('a');
    if (name == null) return null;

    return new CorporateBody(name);
  }

  public static CorporateBody fromIntermarcField(DataField field) {
    if (field == null) return null;
    String name = field.getString('a');
    if (name == null) return null;

    String function = null;
    String lang = null;

    if (field.isCode('4')) { // function
      FunctionMap fm = FunctionMap.get(field.getString('4'));
      if (fm != null) function = fm.getFunction();
    }

    if (field.isCode('w')) { // lang
      String w = field.getString('w');
      lang = Utils.intermarcExtractLang(w);
    }

    CorporateBody cb = new CorporateBody(name, lang);
    cb.setFunction(function);
    return cb;
  }

  private static final String NAME_SPARQL = "PREFIX rdfs: <" + RDFS.getURI() + ">\n" +
    "PREFIX efrbroo: <" + FRBROO.getURI() + ">\n" +
    "SELECT DISTINCT ?s " +
    "WHERE { ?s a efrbroo:F11_Corporate_Body; rdfs:label ?o. " +
    "FILTER (lcase(str(?o)) = ?name) }";

  public static Resource getFromDoremus(String name) {
    ParameterizedSparqlString pss = new ParameterizedSparqlString();
    pss.setCommandText(NAME_SPARQL);
    pss.setLiteral("name", name.toLowerCase());

    return (Resource) Utils.queryDoremus(pss, "s");
  }

  @Override
  public String getFullName() {
    return name;
  }

  @Override
  public void interlink() {
    Resource match = getFromDoremus(this.getName());
    if (match != null) {
      this.setUri(match.getURI());
    }
  }

}