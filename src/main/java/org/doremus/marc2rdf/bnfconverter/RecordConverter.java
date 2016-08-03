package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.doremus.marc2rdf.marcparser.Record;

import java.net.URISyntaxException;


public class RecordConverter {
  private Model model;

  public RecordConverter(Record record, Model model) throws URISyntaxException {
    this.model = model;

    F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(record);
    F28_ExpressionCreation f28 = new F28_ExpressionCreation(record);
    F14_IndividualWork f14 = new F14_IndividualWork();
    F15_ComplexWork f15 = new F15_ComplexWork();
    F40_IdentifierAssignment f40 = new F40_IdentifierAssignment(record);
    F24_PublicationExpression f24 = new F24_PublicationExpression();
    F42_RepresentativeExpressionAssignment f42 = new F42_RepresentativeExpressionAssignment();
    F30_PublicationEvent f30 = new F30_PublicationEvent(record);
    F31_Performance f31 = new F31_Performance(record);
    F19_PublicationWork f19 = new F19_PublicationWork();
    F25_PerformancePlan f25 = new F25_PerformancePlan();
    F25_AutrePerformancePlan fa25 = new F25_AutrePerformancePlan();
    F31_AutrePerformance fa31 = new F31_AutrePerformance(record);

    f28.add(f22).add(f14);
    f15.add(f22).add(f14);
    f14.add(f22).add(f30).addPremiere(f31);
    f40.add(f22).add(f14).add(f15);
    f24.add(f22);
    f42.add(f22).add(f15);
    f30.add(f24).add(f19);
    f19.add(f24);
    f31.add(f25);
    fa31.add(fa25);

    model.add(f22.getModel());
    model.add(f28.getModel());
    model.add(f14.getModel());
    model.add(f15.getModel());
    model.add(f40.getModel());
    model.add(f24.getModel());
    model.add(f42.getModel());
    model.add(f30.getModel());
    model.add(f31.getModel());
    model.add(f19.getModel());
    model.add(f25.getModel());
    model.add(fa25.getModel());
    model.add(fa31.getModel());
  }

  public Model getModel() {
    return model;
  }

}
