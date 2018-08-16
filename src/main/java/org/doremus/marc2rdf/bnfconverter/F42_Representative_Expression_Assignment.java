package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F42_Representative_Expression_Assignment extends DoremusResource {
  public F42_Representative_Expression_Assignment(Record record) {
    super(record);

    this.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);
    this.addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF);
  }

}
