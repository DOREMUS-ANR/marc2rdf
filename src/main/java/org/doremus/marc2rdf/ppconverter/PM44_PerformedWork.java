package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.MUS;

public class PM44_PerformedWork extends DoremusResource {
  public PM44_PerformedWork(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, MUS.M44_Performed_Work);
  }
}
