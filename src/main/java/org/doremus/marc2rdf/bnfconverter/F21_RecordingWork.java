package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class F21_RecordingWork extends BIBDoremusResource {
  public F21_RecordingWork(Record record, int i) {
    super(record, null, i);
    this.setClass(FRBROO.F21_Recording_Work);
  }

  public F21_RecordingWork add(F26_Recording recording) {
    this.addProperty(FRBROO.R13_is_realised_in, recording);
    return this;
  }
}
