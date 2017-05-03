package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class Role {
  private final Model model;
  private String actor, function;
  private RDFNode actorRes, functionRes;

  public Role(String actor, Model model) {
    this.actor = actor;
    this.actorRes = null;
    this.model = model;
  }

  public Role(String actor, String function, Model model) {
    this(actor, model);
    this.function = function;
    this.functionRes = null;
  }

  public Role(String actor, RDFNode function, Model model) {
    this(actor, model);
    this.function = null;
    this.functionRes = function;
  }

  public Role(Resource actor, RDFNode function, Model model) {
    this.actorRes = actor;
    this.actor = null;
    this.functionRes = function;
    this.function = null;
    this.model = model;
  }

  public Role(Resource actor, String function, Model model) {
    this.actorRes = actor;
    this.actor = null;
    this.functionRes = null;
    this.function = function;
    this.model = model;
  }

  public String getActor() {
    return actor;
  }

  public void setActor(String actor) {
    this.actor = actor;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(RDFNode function) {
    this.functionRes = function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public Resource toM28IndividualPerformance() {
    if (actor == null && actorRes == null) return null;

    if (actorRes == null) actorRes = model.createLiteral(actor.trim());

    Resource M28 = model.createResource()
      .addProperty(RDF.type, MUS.M28_Individual_Performance)
      .addProperty(CIDOC.P14_carried_out_by, actorRes);

    if (functionRes != null) {
      M28.addProperty(MUS.U1_used_medium_of_performance, functionRes);
    } else if (function != null) {
      function = function.trim();
      if (function.equals("conducteur"))
        M28.addProperty(MUS.U35_foresees_function_of_type, model.createLiteral(function, "fr"));
      else M28.addProperty(MUS.U1_used_medium_of_performance, model.createLiteral(function));
    } else {
      // TODO compare with the casting
    }

    return M28;
  }
}
