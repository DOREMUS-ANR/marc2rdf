package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

public class F19_PublicationWork extends DoremusResource {
  public F19_PublicationWork(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F19_Publication_Work);
  }

  public F19_PublicationWork(Record record) {
    super(record);
    this.setClass(FRBROO.F19_Publication_Work);
  }

  public F19_PublicationWork add(F24_PublicationExpression expression) {
    this.addProperty(FRBROO.R3_is_realised_in, expression);
    return this;
  }
}
