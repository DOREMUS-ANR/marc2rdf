package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class F14_IndividualWork {
  private Model model;
  private URI uriF14;
  private Resource F14;

  public F14_IndividualWork() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF14 = ConstructURI.build("Individual_Work", "F14");

    F14 = model.createResource(uriF14.toString());
    F14.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

  public Resource asResource() {
    return this.F14;
  }

  public F14_IndividualWork addPremiere(F31_Performance premiere) {
    /**************************** Work: had premiere ****************************************/
    F14.addProperty(MUS.U5_had_premiere, premiere.asResource());

    return this;
  }

  public F14_IndividualWork add(F22_SelfContainedExpression expression) {
    /**************************** Work: is realised through (expression) ********************/
    F14.addProperty(FRBROO.R9_is_realised_in, expression.asResource());
    return this;
  }

  public F14_IndividualWork add(F30_PublicationEvent publication) {
    /**************************** Work: had princeps publication ****************************/
    F14.addProperty(MUS.U4_had_princeps_publication, publication.asResource());
//    publication.asResource().addProperty(MUS.U4i_was_princeps_publication_of, F14);
    return this;
  }

  public Model getModel() {
    return model;
  }
}
