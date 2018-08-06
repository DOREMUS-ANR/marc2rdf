package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class PF26_Recording extends DoremusResource {
  public PF26_Recording(Record record) {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F26_Recording);
  }
}
