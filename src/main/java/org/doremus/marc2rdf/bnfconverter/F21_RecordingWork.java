package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class F21_RecordingWork extends DoremusResource {
  public F21_RecordingWork(Record record, String identifier) {
    super(record, identifier);
    this.setClass(FRBROO.F21_Recording_Work);
  }

  public F21_RecordingWork add(F26_Recording recording) {
    this.addProperty(FRBROO.R13_is_realised_in, recording);
    return this;
  }
}
