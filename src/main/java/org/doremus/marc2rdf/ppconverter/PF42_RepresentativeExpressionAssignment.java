package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class PF42_RepresentativeExpressionAssignment  extends DoremusResource{
  private static final String philarmonie = "http://data.doremus.org/organization/Philharmonie_de_Paris";

  public PF42_RepresentativeExpressionAssignment(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);

    /**************************** Responsable de l'attribution **********************************/
    this.resource.addProperty(FRBROO.R44_carried_out_by, this.model.createResource(philarmonie));
  }

  public PF42_RepresentativeExpressionAssignment add(PF22_SelfContainedExpression f22) {
    /**************************** Attribution à ***********************************************/
    this.resource.addProperty(FRBROO.R51_assigned, f22.asResource());
    return this;
  }

  public PF42_RepresentativeExpressionAssignment add(PF15_ComplexWork f15) {
    /**************************** Attribution à ***********************************************/
    this.resource.addProperty(FRBROO.R50_assigned_to, f15.asResource());
    return this;
  }
}
