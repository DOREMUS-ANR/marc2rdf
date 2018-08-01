package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;


public class M45_DescriptiveExpressionAssignment extends DoremusResource {
  public M45_DescriptiveExpressionAssignment(Record record) {
    super(record);

//    this.resource.addProperty(RDF.type, MUS.M45_Descriptive_Expression_Assignment);
    this.resource.addProperty(CIDOC.P14_carried_out_by, this.model.createResource(BNF2RDF.organizationURI));
  }
}
