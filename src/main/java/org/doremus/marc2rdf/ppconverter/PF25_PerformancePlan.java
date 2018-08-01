package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class PF25_PerformancePlan extends DoremusResource {
  public PF25_PerformancePlan(String identifier) {
    this(identifier, false);
  }

  public PF25_PerformancePlan(String identifier, boolean light) {
    super(identifier);

    if (light) return;
    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    PF20_PerformanceWork work = new PF20_PerformanceWork(this.identifier);
    PF28_ExpressionCreation planCreation = new PF28_ExpressionCreation(identifier);
    work.add(this);
    planCreation.asResource()
      .addProperty(FRBROO.R17_created, this.resource)
      .addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    model.add(work.getModel());
    model.add(planCreation.getModel());
  }


  public PF25_PerformancePlan(Record record) {
    this(record.getIdentifier());
    this.record = record;
  }

  public void setAsImprovisation() {
    PM50_Performance_Mode pMode = new PM50_Performance_Mode(this.uri + "/mode", "improvisation");
    this.resource.addProperty(MUS.U90_foresees_creation_or_performance_mode, pMode.asResource());
    this.model.add(pMode.getModel());
  }

  public PF25_PerformancePlan add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P165_incorporates, f22.asResource());
    return this;
  }

  public PF25_PerformancePlan add(PM42_PerformedExpressionCreation m42) {
    this.resource.addProperty(CIDOC.P9_consists_of, m42.asResource());
    return this;
  }
}
