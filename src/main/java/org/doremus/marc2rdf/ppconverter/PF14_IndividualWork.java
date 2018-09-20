package org.doremus.marc2rdf.ppconverter;

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
    this.setClass(FRBROO.F14_Individual_Work);
  }

  public PF14_IndividualWork add(PF22_SelfContainedExpression f22) {
    this.addProperty(FRBROO.R9_is_realised_in, f22);
    return this;
  }

  public PF14_IndividualWork add(PF30_PublicationEvent f30) {
    this.addProperty(MUS.U4_had_princeps_publication, f30);
    return this;
  }

  public PF14_IndividualWork addPremiere(PM42_PerformedExpressionCreation premiere) {
    this.addProperty(MUS.U5_had_premiere, premiere.getMainPerformance());
    return this;
  }

  public PF14_IndividualWork add(PF50_ControlledAccessPoint accessPoint) {
    this.addProperty(CIDOC.P1_is_identified_by, accessPoint);
    return this;
  }

  public PF14_IndividualWork add(PF14_IndividualWork child) {
    this.addProperty(CIDOC.P148_has_component, child);
    return this;
  }

}
