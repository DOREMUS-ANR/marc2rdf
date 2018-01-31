package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class PF25_PerformancePlan extends DoremusResource {
  public PF25_PerformancePlan(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    PF28_ExpressionCreation planCreation = new PF28_ExpressionCreation(identifier);
    planCreation.asResource().addProperty(FRBROO.R17_created, this.resource);
    model.add(planCreation.getModel());
  }

  public PF25_PerformancePlan(Record record) throws URISyntaxException {
    this(record.getIdentifier());
    this.record = record;
  }

  public PF25_PerformancePlan add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P165_incorporates, f22.asResource());
    return this;
  }

  public void setAsImprovisation() {
    try {
      PM50_Performance_Mode pMode = new PM50_Performance_Mode(this.uri + "/mode", "improvisation");
      this.resource.addProperty(MUS.U86_had_performance_mode, pMode.asResource());
      this.model.add(pMode.getModel());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
