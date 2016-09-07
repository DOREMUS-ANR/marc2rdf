package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class PF25_PerformancePlan extends DoremusResource {
  public PF25_PerformancePlan(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);
  }

  public PF25_PerformancePlan add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P165_incorporates, f22.asResource());
//    f22.asResource().addProperty(model.createProperty(cidoc + "P165i_is_incorporated_in"), F25);
    return this;
  }

}
