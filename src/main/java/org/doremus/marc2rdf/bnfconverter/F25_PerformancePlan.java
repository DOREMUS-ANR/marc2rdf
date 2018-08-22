package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F25_PerformancePlan extends DoremusResource {

  public F25_PerformancePlan(String identifier) {
    super(identifier);

    this.setClass(FRBROO.F25_Performance_Plan);

    F20_PerformanceWork work = new F20_PerformanceWork(this.identifier);
    F28_ExpressionCreation planCreation = new F28_ExpressionCreation(identifier);
    work.add(this);
    planCreation.addProperty(FRBROO.R17_created, this)
      .addProperty(FRBROO.R19_created_a_realisation_of, work);
    model.add(work.getModel());
    model.add(planCreation.getModel());
  }

  public F25_PerformancePlan add(F22_SelfContainedExpression f22) {
    this.addProperty(CIDOC.P165_incorporates, f22);
    return this;
  }
}
