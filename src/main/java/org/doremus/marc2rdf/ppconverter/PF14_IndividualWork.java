package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class PF14_IndividualWork {
  private static final String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";

  private Model model;
  private URI uriF14;
  private Resource F14;

  public PF14_IndividualWork() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF14 = ConstructURI.build("Individual_Work", "F14");

    F14 = model.createResource(uriF14.toString());
    F14.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

  public Model getModel() {
    return model;
  }


  public Resource asResource() {
    return F14;
  }

  public PF14_IndividualWork add(PF22_SelfContainedExpression f22) {
    /**************************** Work: is realised through (expression) ********************/
    F14.addProperty(FRBROO.R9_is_realised_in, f22.asResource());
    return this;
  }

  public PF14_IndividualWork add(PF30_PublicationEvent f30) {
    /**************************** Publication Event: édition princeps ******************/
    F14.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public PF14_IndividualWork addPremiere(PF31_Performance f31) {
    /**************************** Performance: 1ère exécution *************************************/
    F14.addProperty(MUS.U5_had_premiere, f31.asResource());
    return this;
  }

  public PF14_IndividualWork add(PF50_ControlledAccessPoint accessPoint) {
    /**************************** Identification de l'œuvre *********************************/
    F14.addProperty(model.createProperty(cidoc + "P1_is_identified_by"), accessPoint.asResource());
//    accessPoint.asResource().addProperty(model.createProperty(cidoc + "P1i_identifies"), F14);
    return this;
  }

}
