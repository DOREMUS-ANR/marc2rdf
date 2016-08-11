package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class F42_RepresentativeExpressionAssignment extends DoremusResource {
  public F42_RepresentativeExpressionAssignment(Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);

    this.uri = ConstructURI.build("bnf", "F42", "Representative_Expression_Assignment", this.identifier);

    this.resource = this.model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);

    /**************************** Responsable de l'attribution **********************************/
    this.resource.addProperty(FRBROO.R44_carried_out_by, this.model.createResource("http://isni.org/isni/0000000121751303"));
  }

  public F42_RepresentativeExpressionAssignment add(F22_SelfContainedExpression expression) {
    /**************************** Attribution à ***********************************************/
    this.resource.addProperty(FRBROO.R51_assigned, expression.asResource());
    return this;
  }

  public F42_RepresentativeExpressionAssignment add(F15_ComplexWork f15) {
    /**************************** Attribution à ***********************************************/
    this.resource.addProperty(FRBROO.R50_assigned_to, f15.asResource());
    return this;
  }


}
