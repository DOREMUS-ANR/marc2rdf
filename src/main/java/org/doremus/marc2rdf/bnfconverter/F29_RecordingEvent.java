package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class F29_RecordingEvent extends DoremusResource {
  public F29_RecordingEvent(Record record) {
    super(record);
    this.setClass(FRBROO.F29_Recording_Event);
  }

  public F29_RecordingEvent add(F26_Recording recording) {
    this.addProperty(FRBROO.R21_created, recording);
    return this;
  }

  public F29_RecordingEvent add(F21_RecordingWork recordingWork) {
    this.addProperty(FRBROO.R22_created_a_realisation_of, recordingWork);
    return this;
  }
}
