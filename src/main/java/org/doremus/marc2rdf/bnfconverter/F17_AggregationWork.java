package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class F17_AggregationWork extends DoremusResource {
  public F17_AggregationWork(Record record) {
    super(record);
    this.setClass(FRBROO.F17_Aggregation_Work);
  }

  public F17_AggregationWork add(M46_SetOfTracks tracks) {
    this.addProperty(FRBROO.R9_is_realised_in, tracks);
    return this;
  }
}
