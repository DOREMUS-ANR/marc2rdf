package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.FRBROO;

public class F20_PerformanceWork extends DoremusResource {
  public F20_PerformanceWork(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F20_Performance_Work);
  }

  public F20_PerformanceWork add(F25_PerformancePlan f25) {
    this.addProperty(FRBROO.R12_is_realised_in, f25);
    return this;
  }

}
