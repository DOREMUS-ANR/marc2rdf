package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Attr;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.PROV;

import java.net.URISyntaxException;
import java.util.Arrays;


public class BIBRecordConverter {
  private final String ark;
  public Resource provActivity, intermarcRes;
  private Model model;
  private Record record;

  private F22_SelfContainedExpression f22;
  public F28_ExpressionCreation f28;
  private F14_IndividualWork f14;
  private F15_ComplexWork f15;


  public BIBRecordConverter(Record record, Model model, String extArk) {
    this.record = record;
    this.model = model;

    Attr arkField = record.getAttrByName("IDPerenne");
    this.ark = (arkField == null) ? extArk : arkField.getData();

    // PROV-O tracing
    intermarcRes = BNF2RDF.computeProvIntermarc(ark, model);
    provActivity = BNF2RDF.computeProvActivity(record.getIdentifier(), intermarcRes, model);

    if (BNF2RDF.getRecordCode(record).equals("g")) convertDAV();

//    for (DataField field : record.getDatafieldsByCode(143)) {
//      if (!field.isCode('a')) continue;
//      String subA = field.getSubfield('a').getData();
//      if (!"Traditions".equals(subA)) continue;
//      System.out.println(record.getIdentifier());
//      System.out.println(field);
//    }
//    // Instantiate work and expression
//    f28 = new F28_ExpressionCreation(record);
//    f22 = new F22_SelfContainedExpression(record, f28);
//    f14 = new F14_IndividualWork(record);
//    f15 = new F15_ComplexWork(record);
////    F40_IdentifierAssignment f40 = new F40_IdentifierAssignment(record);
//    M45_DescriptiveExpressionAssignment f42 = new M45_DescriptiveExpressionAssignment(record);
//
//    f28.add(f22).add(f14);
//    f15.add(f22).add(f14);
//    f14.add(f22);
////    f40.add(f22).add(f14).add(f15);
//    f42.add(f22).add(f15);
//
//    addPrincepsPublication();
//    addPerformances();
//    linkToMovements();
//
//
//
//    for (DoremusResource res : new DoremusResource[]{f22, f28, f15, f14, f42}) {
//      addProvenanceTo(res);
//      model.add(res.getModel());
//    }

  }

  private void convertDAV() {
    F3_ManifestationProductType manif = new F3_ManifestationProductType(record);
    F24_PublicationExpression publicationExpression = new F24_PublicationExpression(record);
    manif.add(publicationExpression);

    for (DoremusResource r : Arrays.asList(manif, publicationExpression)) {
      r.addProvenance(intermarcRes, provActivity);
      this.model.add(r.getModel());
    }
  }


  private void addProvenanceTo(DoremusResource res) {
    res.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo,BNF2RDF.BnF)
      .addProperty(PROV.wasDerivedFrom, this.intermarcRes)
      .addProperty(PROV.wasGeneratedBy, this.provActivity);
  }

  private void addPerformances() throws URISyntaxException {
    int performanceCounter = 0;

    for (String performance : M42_PerformedExpressionCreation.getPerformances(record)) {
      M42_PerformedExpressionCreation m42 = new M42_PerformedExpressionCreation(performance, record, f28, ++performanceCounter);
      F25_PerformancePlan f25 = new F25_PerformancePlan(m42.getIdentifier());

      m42.add(f25).add(f22);
      f25.add(f22);
      f15.add(m42);

      if (m42.isPremiere()) {
        f14.addPremiere(m42);
        f22.addPremiere(m42);
      }

      model.add(m42.getModel());
      model.add(f25.getModel());
    }
  }

  private void addPrincepsPublication() throws URISyntaxException {
    int pubCounter = 0;

    for (String edition : F30_PublicationEvent.getEditionPrinceps(record)) {

      F30_PublicationEvent f30 = new F30_PublicationEvent(edition.trim(), record, f28, ++pubCounter);
      F24_PublicationExpression f24 = new F24_PublicationExpression(f30.getIdentifier());
      F19_PublicationWork f19 = new F19_PublicationWork(f30.getIdentifier());

      f30.add(f24).add(f19);
      f19.add(f24);
      f24.add(f22);

      if (pubCounter > 1) {
        f14.add(f30);
        f22.add(f30);
      }

      model.add(f24.getModel());
      model.add(f30.getModel());
      model.add(f19.getModel());
    }
  }

  private void linkToMovements() {
    for (String code : record.getDatafieldsByCode("302", '3')) {
      f14.addMovement(new F14_IndividualWork(code));
      f22.addMovement(new F22_SelfContainedExpression(code));
    }
  }

  public Model getModel() {
    return model;
  }

  public String getArk() {
    return ark;
  }


}
