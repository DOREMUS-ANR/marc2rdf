package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.doremus.marc2rdf.marcparser.Record;

import java.net.URISyntaxException;

public class RecordConverter {
  private Model model;

  public RecordConverter(Record record, Model model) throws URISyntaxException {
    this.model = model;

    PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression(record);
    PF28_ExpressionCreation f28 = new PF28_ExpressionCreation(record);
    PF14_IndividualWork f14 = new PF14_IndividualWork();
    PF15_ComplexWork f15 = new PF15_ComplexWork(record);
    PF42_RepresentativeExpressionAssignment f42 = new PF42_RepresentativeExpressionAssignment();
    PF25_PerformancePlan f25 = new PF25_PerformancePlan();
    PF31_Performance f31 = new PF31_Performance(record);
    PF25_AutrePerformancePlan fa25 = new PF25_AutrePerformancePlan();
    PF31_AutrePerformance fa31 = new PF31_AutrePerformance(record);
    PF30_PublicationEvent f30 = new PF30_PublicationEvent(record);
    PF40_IdentifierAssignment f40 = new PF40_IdentifierAssignment(record);
    PF50_ControlledAccessPoint f50 = new PF50_ControlledAccessPoint();
    PF24_PublicationExpression f24 = new PF24_PublicationExpression();
    PF19_PublicationWork f19 = new PF19_PublicationWork();

    f28.add(f22).add(f14).add(f25).add(fa25);
    f15.add(f22).add(f14);
    f14.add(f22).add(f30).addPremiere(f31).add(f50);
    f40.add(f22).add(f14).add(f15).add(f50);
    fa25.add(f22);
    f22.add(f50);
    f24.add(f22);
    f42.add(f22).add(f15);
    f30.add(f24).add(f19);
    f19.add(f24);
    f31.add(f25);
    fa31.add(fa25);

    model.add(f22.getModel());
    model.add(f28.getModel());
    model.add(f15.getModel());
    model.add(f14.getModel());
    model.add(f42.getModel());
    model.add(f25.getModel());
    model.add(f31.getModel());
    model.add(fa25.getModel());
    model.add(fa31.getModel());
    model.add(f30.getModel());
    model.add(f40.getModel());
    model.add(f50.getModel());
    model.add(f24.getModel());
    model.add(f19.getModel());
  }

  public Model getModel() {
    return model;
  }

}
