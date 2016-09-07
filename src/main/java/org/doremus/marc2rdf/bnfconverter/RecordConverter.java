package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.doremus.marc2rdf.marcparser.Record;

import java.net.URISyntaxException;


public class RecordConverter {
  private Model model;
  private Record record;

  private F22_SelfContainedExpression f22;
  private F28_ExpressionCreation f28;
  private F14_IndividualWork f14;
  private F15_ComplexWork f15;


  public RecordConverter(Record record, Model model) throws URISyntaxException {
    this.record = record;
    this.model = model;

    // Instantiate work and expression
    f22 = new F22_SelfContainedExpression(record);
    f28 = new F28_ExpressionCreation(record);
    f14 = new F14_IndividualWork(record);
    f15 = new F15_ComplexWork(record);
//    F40_IdentifierAssignment f40 = new F40_IdentifierAssignment(record);
    F42_RepresentativeExpressionAssignment f42 = new F42_RepresentativeExpressionAssignment(record);

    addPrincepsPublication();
    addPerformances();

    f28.add(f22).add(f14);
    f15.add(f22).add(f14);
    f14.add(f22);
//    f40.add(f22).add(f14).add(f15);
    f42.add(f22).add(f15);

    model.add(f22.getModel());
    model.add(f28.getModel());
    model.add(f14.getModel());
    model.add(f15.getModel());
//    model.add(f40.getModel());
    model.add(f42.getModel());
  }

  private void addPerformances() throws URISyntaxException {
    // TODO missing F20_PerformanceWork: check mapping rules
    // F28 Expression Creation R19 created a realisation of F20 Performance Work R12 is realised in F25 Performance Plan

    for(String performance: F31_Performance.getPerformances(record)) {
      F31_Performance f31 = new F31_Performance(performance, record);
      F25_PerformancePlan f25 = new F25_PerformancePlan(f31.getIdentifier());

      f31.add(f25);
      f25.add(f22);
      if(f31.isPremiere()) f14.addPremiere(f31);

      model.add(f31.getModel());
      model.add(f25.getModel());
    }
  }

  private void addPrincepsPublication() throws URISyntaxException {
    String edition = F30_PublicationEvent.getEditionPrinceps(record);
    if (edition == null) return;

    F30_PublicationEvent f30 = new F30_PublicationEvent(edition, record);
    F24_PublicationExpression f24 = new F24_PublicationExpression(f30.getIdentifier());
    F19_PublicationWork f19 = new F19_PublicationWork(f30.getIdentifier());

    f30.add(f24).add(f19);
    f19.add(f24);

    f14.add(f30);
    f24.add(f22);

    model.add(f24.getModel());
    model.add(f30.getModel());
    model.add(f19.getModel());
  }

  public Model getModel() {
    return model;
  }

}
