package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

/***
 * Correspond à l'oeuvre musicale
 ***/
public class PF15_ComplexWork {
  private Model model;
  private URI uriF15;
  private Resource F15;
  private Record record;

  public PF15_ComplexWork(Record record) throws URISyntaxException {
    this.record = record;
    this.model = ModelFactory.createDefaultModel();
    this.uriF15 = ConstructURI.build("Complex_Work", "F15");

    F15 = model.createResource(uriF15.toString());
    F15.addProperty(RDF.type, FRBROO.F15_Complex_Work);
  }

  public Model getModel() throws URISyntaxException {
    return model;
  }

  public Resource asResource() {
    return F15;
  }

  public PF15_ComplexWork add(PF22_SelfContainedExpression f22) {
    /**************************** Work: has representative Expression ***********************/
    if (record.isType("UNI:100")) {
      F15.addProperty(FRBROO.R40_has_representative_expression, f22.asResource());
      //f22.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R40i_is_representative_expression_of"), F15);
    }
    return this;
  }

  public PF15_ComplexWork add(PF14_IndividualWork f14) {
    /**************************** Work: has member  (Work) **********************************/
    F15.addProperty(FRBROO.R10_has_member, f14.asResource());
//    f14.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R10i_is_member_of"), F15);
    return this;
  }

}
