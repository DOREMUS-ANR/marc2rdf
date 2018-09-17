package org.doremus.marc2rdf.ppconverter;

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
    this.setClass(FRBROO.F25_Performance_Plan);

    PF20_PerformanceWork work = new PF20_PerformanceWork(this.identifier);
    PF28_ExpressionCreation planCreation = new PF28_ExpressionCreation(identifier);
    work.add(this);
    planCreation.addProperty(FRBROO.R17_created, this)
      .addProperty(FRBROO.R19_created_a_realisation_of, work);
    model.add(planCreation.getModel());
  }


  public PF25_PerformancePlan(Record record) {
    this(record.getIdentifier());
    this.record = record;
  }

  public void setAsImprovisation() {
    PM50_Performance_Mode pMode = new PM50_Performance_Mode(this.uri + "/mode", "improvisation");
    this.addProperty(MUS.U90_foresees_creation_or_performance_mode, pMode);
  }

  public PF25_PerformancePlan add(PF22_SelfContainedExpression f22) {
    this.addProperty(CIDOC.P165_incorporates, f22);
    return this;
  }

  public PF25_PerformancePlan add(PM42_PerformedExpressionCreation m42) {
    this.addProperty(CIDOC.P9_consists_of, m42);
    return this;
  }

  public PF25_PerformancePlan add(PF25_PerformancePlan subplan) {
    this.addProperty(FRBROO.R5_has_component, subplan);
    return this;
  }
}
