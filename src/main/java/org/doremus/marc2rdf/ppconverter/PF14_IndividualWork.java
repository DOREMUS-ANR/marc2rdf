package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class PF14_IndividualWork extends DoremusResource {
  public PF14_IndividualWork(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);

    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

  public PF14_IndividualWork add(PF22_SelfContainedExpression f22) {
    /**************************** Work: is realised through (expression) ********************/
    this.resource.addProperty(FRBROO.R9_is_realised_in, f22.asResource());
    return this;
  }

  public PF14_IndividualWork add(PF30_PublicationEvent f30) {
    /**************************** Publication Event: édition princeps ******************/
    this.resource.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public PF14_IndividualWork addPremiere(PF31_Performance f31) {
    /**************************** Performance: 1ère exécution *************************************/
    this.resource.addProperty(MUS.U5_had_premiere, f31.asResource());
    return this;
  }

  public PF14_IndividualWork add(PF50_ControlledAccessPoint accessPoint) {
    /**************************** Identification de l'œuvre *********************************/
    this.resource.addProperty(CIDOC.P1_is_identified_by, accessPoint.asResource());
//    accessPoint.asResource().addProperty(model.createProperty(cidoc + "P1i_identifies"), F14);
    return this;
  }

}
