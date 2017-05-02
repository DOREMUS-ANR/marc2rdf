package org.doremus.vocabulary;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import java.io.File;

/**
 * Utility for Vocabulary referencing.
 */

public abstract class Vocabulary implements Comparable<Vocabulary> {

  protected Model vocabulary;
  protected String schemePath;
  private String name, category;

  public Vocabulary(String name, Model model) {
    this.vocabulary = model;
    this.name = name;
    this.category = name.split("-", 2)[0];
    this.schemePath = null;
  }

  public static Vocabulary fromFile(File file) {
    return fromUrl(file.getAbsolutePath());
  }

  public static Vocabulary fromUrl(String url) {
    Model vocabulary = ModelFactory.createDefaultModel();
    vocabulary.read(url, "TURTLE");
    String name = FilenameUtils.getName(url).replace(".ttl", "");

    // Check the type of Vocabulary
    // is it a SKOS?
    StmtIterator conceptSchemeIter = vocabulary.listStatements(new SimpleSelector(null, RDF.type, SKOS.Concept));
    if (conceptSchemeIter.hasNext()) return new SKOSVocabulary(name, vocabulary);

    // is it a MODS?
    conceptSchemeIter = vocabulary.listStatements(new SimpleSelector(null, RDF.type, MODS.ModsResource));
    if (conceptSchemeIter.hasNext()) return new MODS(name, vocabulary);

    // TODO else
    System.out.println("Not managed vocabulary: " + name);
    return null;
  }


  public Resource getConcept(String code) {
    if (schemePath == null) return null;
    Resource concept = vocabulary.getResource(schemePath + code);
    if (vocabulary.contains(concept, null, (RDFNode) null))
      return concept;
    else return null;
  }

  public abstract Resource findConcept(String text, boolean strict);


  @Override
  public int compareTo(Vocabulary v) {
    // give priority to mimo
    boolean thisMimo = this.getName().contains("mimo");
    boolean thatMimo = v.getName().contains("mimo");

    if (thisMimo != thatMimo) return thisMimo ? -1 : 1;

    // give priority to iaml
    boolean thisIaml = this.getName().contains("iaml");
    boolean thatIaml = v.getName().contains("iaml");

    if (thisIaml == thatIaml) return this.name.compareTo(v.name);

    if (thisIaml) return -1;
    return 1;
  }

  public String getCategory() {
    return category;
  }

  public String getName() {
    return name;
  }

  protected void setSchemePathFromType(Resource type) {
    // Save default path
    StmtIterator conceptSchemeIter = vocabulary.listStatements(new SimpleSelector(null, RDF.type, type));
    if (!conceptSchemeIter.hasNext()) return;
    schemePath = conceptSchemeIter.nextStatement().getSubject().toString();

    if (schemePath != null && !schemePath.endsWith("/") && !schemePath.endsWith("#"))
      schemePath += "/";
  }

  protected void setSchemePathFromType(String type) {
    setSchemePathFromType(vocabulary.createResource(type));
  }

}
