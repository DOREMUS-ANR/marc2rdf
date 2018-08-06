package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class PF21_RecordingWork extends DoremusResource {
  public PF21_RecordingWork(Record record) {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F21_Recording_Work);
  }
}
