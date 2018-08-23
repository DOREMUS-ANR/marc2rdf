package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class M44_PerformedWork extends DoremusResource {
  public M44_PerformedWork(String identifier) {
    super(identifier);
    this.setClass(MUS.M44_Performed_Work);
  }

  public M44_PerformedWork(Record record, String identifier, DataField df) {
    this(identifier);
    this.record = record;


  }

  public M44_PerformedWork add(M43_PerformedExpression expression) {
    this.addProperty(FRBROO.R9_is_realised_in, expression);
    return this;
  }
}
