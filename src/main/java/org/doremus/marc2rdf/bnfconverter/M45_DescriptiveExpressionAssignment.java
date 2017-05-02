package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class M45_DescriptiveExpressionAssignment extends DoremusResource {
  public M45_DescriptiveExpressionAssignment(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, MUS.M45_Descriptive_Expression_Assignment);
    this.resource.addProperty(CIDOC.P14_carried_out_by, this.model.createResource(BNF2RDF.organizationURI));
  }

  public M45_DescriptiveExpressionAssignment add(F22_SelfContainedExpression expression) {
    this.resource.addProperty(CIDOC.P141_assigned, expression.asResource());
    return this;
  }

  public M45_DescriptiveExpressionAssignment add(F15_ComplexWork f15) {
    this.resource.addProperty(CIDOC.P140_assigned_attribute_to, f15.asResource());
    return this;
  }


}
