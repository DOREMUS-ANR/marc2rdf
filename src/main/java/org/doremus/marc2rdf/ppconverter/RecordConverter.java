package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.doremus.marc2rdf.marcparser.Record;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class RecordConverter {
  private Record record;
  private Model model;

  PF22_SelfContainedExpression f22;
  PF28_ExpressionCreation f28;
  PF14_IndividualWork f14;
  PF15_ComplexWork f15;

  public RecordConverter(Record record, Model model) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    this.record = record;
    this.model = model;

    f22 = new PF22_SelfContainedExpression(record);
    f28 = new PF28_ExpressionCreation(record);
    f14 = new PF14_IndividualWork(record);
    f15 = new PF15_ComplexWork(record);
    PF42_RepresentativeExpressionAssignment f42 = new PF42_RepresentativeExpressionAssignment(record);
    PF40_IdentifierAssignment f40 = new PF40_IdentifierAssignment(record);
    PF50_ControlledAccessPoint f50 = new PF50_ControlledAccessPoint(record);

    addPrincepsPublication();
    addPerformances();

    f28.add(f22).add(f14);
    f15.add(f22).add(f14);
    f14.add(f22).add(f50);
    f40.add(f22).add(f14).add(f15).add(f50);
    f22.add(f50);
    f42.add(f22).add(f15);


    model.add(f22.getModel());
    model.add(f28.getModel());
    model.add(f15.getModel());
    model.add(f14.getModel());
    model.add(f42.getModel());
    model.add(f40.getModel());
    model.add(f50.getModel());
  }

  private void addPerformances() throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    // TODO missing F20_PerformanceWork: check mapping rules
    // F28 Expression Creation R19 created a realisation of F20 Performance Work R12 is realised in F25 Performance Plan

    for (String performance : PF31_Performance.getPerformances(record)) {
      PF31_Performance f31 = new PF31_Performance(performance, record);
      PF25_PerformancePlan f25 = new PF25_PerformancePlan(f31.getIdentifier());

      f31.add(f25);
      f25.add(f22);
      if (f31.isPremiere()) f14.addPremiere(f31);

      model.add(f31.getModel());
      model.add(f25.getModel());

      model.add(f25.getModel());
      model.add(f31.getModel());
    }

  }

  private void addPrincepsPublication() throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    String edition = PF30_PublicationEvent.getEditionPrinceps(record);
    if (edition == null) return;

    PF30_PublicationEvent f30 = new PF30_PublicationEvent(edition, record);
    PF24_PublicationExpression f24 = new PF24_PublicationExpression(f30.getIdentifier());
    PF19_PublicationWork f19 = new PF19_PublicationWork(f30.getIdentifier());

    f30.add(f24).add(f19);
    f19.add(f24);
    f14.add(f30);
    f24.add(f22);

    model.add(f30.getModel());
    model.add(f24.getModel());
    model.add(f19.getModel());
  }

  public Model getModel() {
    return model;
  }

}
