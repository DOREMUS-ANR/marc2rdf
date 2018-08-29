package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class M28_Individual_Performance extends DoremusResource {
  private Artist agent;
  private Resource mop;
  private String function;

  private int countSpecific;

  public M28_Individual_Performance(String uri) {
    super();
    regenerateResource(uri);
    this.setClass(MUS.M28_Individual_Performance);

    agent = null;
    mop = null;
    function = null;

    countSpecific = 0;
  }

  public void setAgent(Artist agent) {
    this.agent = agent;
    this.addProperty(CIDOC.P14_carried_out_by, agent);
  }

  public void setFunction(String function) {
    if (function == null || function.isEmpty()) return;
    this.function = function;
    Resource fun = model.createResource()
      .addProperty(RDF.type, MUS.M31_Actor_Function)
      .addProperty(RDFS.label, function);
    this.addProperty(MUS.U31_had_function, fun);
  }

  public void setMoP(Resource mop) {
    if (mop == null) return;
    this.addProperty(MUS.U1_used_medium_of_performance, mop);
    this.model.add(mop.listProperties());
    this.mop = mop;
  }

  public void setCharacter(String character) {
    if (character == null || character.isEmpty()) return;
    Resource charResource = model.createResource(this.uri + "/character")
      .addProperty(RDF.type, FRBROO.F38_Character)
      .addProperty(RDFS.label, character);
    this.addProperty(MUS.U27_performed_character, charResource);
  }

  public boolean hasMop(Resource mop) {
    if (this.mop == null) return false;
    if (this.mop.getURI().equals(mop.getURI())) return true;
    Statement thisLabel = this.mop.getProperty(RDFS.label);
    Statement thatLabel = mop.getProperty(RDFS.label);
    if (thisLabel == null || thatLabel == null) return false;
    return thisLabel.getString().equals(thatLabel.getString());
  }

  public boolean isEnsemble() {
    String uri = this.mop.getURI();
    return uri.matches("http://data\\.doremus\\.org/vocabulary/iaml/mop/o..");
  }

  public void setSpecificMop(String txt) {
    Resource specific = model.createResource(this.uri + "/" + ++countSpecific)
      .addProperty(RDF.type, CIDOC.E22_Man_Made_Object)
      .addProperty(RDFS.comment, txt)
      .addProperty(CIDOC.P3_has_note, txt);
    this.addProperty(CIDOC.P16_used_specific_object, specific);
  }

  public M28_Individual_Performance add(M28_Individual_Performance member) {
    this.addProperty(CIDOC.P9_consists_of, member);
    return this;
  }
}
