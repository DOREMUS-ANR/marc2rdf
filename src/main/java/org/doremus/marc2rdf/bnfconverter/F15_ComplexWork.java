package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

/***
 * Correspond à l'oeuvre musicale
 ***/
public class F15_ComplexWork {
  private Model model;
  private URI uriF15;
  private Resource F15;

  public F15_ComplexWork() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF15 = ConstructURI.build("Complex_Work", "F15");

    F15 = model.createResource(uriF15.toString());
    F15.addProperty(RDF.type, FRBROO.F15_Complex_Work);
  }

  public Resource asResource() {
    return F15;
  }

  public Model getModel() throws URISyntaxException {
    return model;
  }

  public F15_ComplexWork add(F22_SelfContainedExpression f22) {
    /**************************** Work: has representative Expression ***********************/
    F15.addProperty(FRBROO.R40_has_representative_expression, f22.asResource());
    f22.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R40i_is_representative_expression_of"), F15);

    return this;
  }

  public F15_ComplexWork add(F14_IndividualWork f14) {
    /**************************** Work: has member  (Work) **********************************/
    F15.addProperty(FRBROO.R10_has_member, f14.toString());
    f14.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R10i_is_member_of"), F15);

    return this;
  }
}
