package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/***
 * Correspond à l'oeuvre musicale
 ***/
public class F15_ComplexWork extends DoremusResource {

  public F15_ComplexWork(Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);

    this.uri = ConstructURI.build("bnf", "F15", "Complex_Work", record.getIdentifier());

    this.resource = model.createResource(uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F15_Complex_Work);
  }

  public F15_ComplexWork add(F22_SelfContainedExpression f22) {
    /**************************** Work: has representative Expression ***********************/
    this.resource.addProperty(FRBROO.R40_has_representative_expression, f22.asResource());
//    f22.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R40i_is_representative_expression_of"), F15);
    return this;
  }

  public F15_ComplexWork add(F14_IndividualWork f14) {
    /**************************** Work: has member  (Work) **********************************/
    this.resource.addProperty(FRBROO.R10_has_member, f14.toString());
//    f14.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R10i_is_member_of"), F15);
    return this;
  }
}
