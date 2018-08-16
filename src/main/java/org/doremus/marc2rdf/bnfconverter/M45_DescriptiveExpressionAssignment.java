package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;


public class M45_DescriptiveExpressionAssignment extends DoremusResource {
  public M45_DescriptiveExpressionAssignment(Record record) {
    super(record);

    this.setClass(MUS.M45_Descriptive_Expression_Assignment);
    this.addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF);
  }
}
