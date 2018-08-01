package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.FRBROO;

public class PF20_PerformanceWork extends DoremusResource{

  public PF20_PerformanceWork(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F20_Performance_Work);
  }

  public PF20_PerformanceWork add(PF25_PerformancePlan f25) {
    this.resource.addProperty(FRBROO.R12_is_realised_in, f25.asResource());
    return this;
  }
}
