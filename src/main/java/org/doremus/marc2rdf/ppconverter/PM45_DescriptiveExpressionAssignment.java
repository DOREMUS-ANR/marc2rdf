package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class PM45_DescriptiveExpressionAssignment extends DoremusResource{
  public PM45_DescriptiveExpressionAssignment(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, MUS.M45_Descriptive_Expression_Assignment);
    this.resource.addProperty(CIDOC.P14_carried_out_by, this.model.createResource(PP2RDF.organizationURI));
  }

  public PM45_DescriptiveExpressionAssignment add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P141_assigned, f22.asResource());
    return this;
  }

  public PM45_DescriptiveExpressionAssignment add(PF15_ComplexWork f15) {
    this.resource.addProperty(CIDOC.P140_assigned_attribute_to, f15.asResource());
    return this;
  }
}
