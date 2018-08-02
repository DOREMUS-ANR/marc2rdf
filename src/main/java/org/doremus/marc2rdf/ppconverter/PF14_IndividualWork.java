package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class PF14_IndividualWork extends DoremusResource {
  public PF14_IndividualWork(Record record, String identifier) {
    this(identifier);
    this.record = record;
  }

  public PF14_IndividualWork(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

  public PF14_IndividualWork add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R9_is_realised_in, f22.asResource());
    return this;
  }

  public PF14_IndividualWork add(PF30_PublicationEvent f30) {
    this.resource.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public PF14_IndividualWork addPremiere(PM42_PerformedExpressionCreation premiere) {
    this.resource.addProperty(MUS.U5_had_premiere, premiere.getMainPerformance());
    return this;
  }

  public PF14_IndividualWork add(PF50_ControlledAccessPoint accessPoint) {
    this.resource.addProperty(CIDOC.P1_is_identified_by, accessPoint.asResource());
    return this;
  }

  public PF14_IndividualWork add(PF14_IndividualWork child) {
    this.resource.addProperty(CIDOC.P148_has_component, child.asResource());
    return this;
  }

}
