package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.PROV;

import java.net.URISyntaxException;
import java.time.Instant;

public class RecordConverter {
  public final Resource provActivity, intermarcRes;

  private Record record;
  private Model model;
  private String identifier;

  private PF22_SelfContainedExpression f22;
  public PF28_ExpressionCreation f28;
  private PF14_IndividualWork f14;
  private PF15_ComplexWork f15;

  public RecordConverter(Record record, Model model) throws URISyntaxException {
    this(record, model, record.getIdentifier());
  }

  public RecordConverter(Record record, Model model, String identifier) throws URISyntaxException {
    this.record = record;
    this.model = model;
    this.identifier = identifier;

    // PROV-O tracing
    // FIXME this is not the intermarc uri
    intermarcRes = model.createResource("http://data.doremus.org/source/philharmonie/" + record.getIdentifier())
      .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, model.createResource(PP2RDF.organizationURI));

    provActivity = model.createResource(ConstructURI.build("pp", "prov", identifier).toString())
      .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
      .addProperty(PROV.used, intermarcRes)
      .addProperty(RDFS.comment, "Reprise et conversion de la notice MARC de la Philharmonie de Paris", "fr")
      .addProperty(RDFS.comment, "Resumption and conversion of the MARC record of the Philharmonie de Paris", "en")
      .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);


    f28 = new PF28_ExpressionCreation(record, identifier);
    f22 = new PF22_SelfContainedExpression(record, identifier, f28);
    f14 = new PF14_IndividualWork(record, identifier);
    f15 = new PF15_ComplexWork(record, identifier);
    PM45_DescriptiveExpressionAssignment f42 = new PM45_DescriptiveExpressionAssignment(record, identifier);
//    PF40_IdentifierAssignment f40 = new PF40_IdentifierAssignment(record);
//    PF50_ControlledAccessPoint f50 = new PF50_ControlledAccessPoint(record);

    addPrincepsPublication();
    addPerformances();

    f28.add(f22).add(f14);
    f15.add(f22).add(f14);
    f14.add(f22);
//      f14.add(f50);
//    f40.add(f22).add(f14).add(f15).add(f50);
//    f22.add(f50);
    f42.add(f22).add(f15);

    for (DoremusResource res : new DoremusResource[]{f22, f28, f15, f14, f42}) {
      addProvenanceTo(res);
      model.add(res.getModel());
    }

  }


  private void addProvenanceTo(DoremusResource res) {
    res.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, model.createResource("http://data.doremus.org/organization/DOREMUS"))
      .addProperty(PROV.wasDerivedFrom, this.intermarcRes)
      .addProperty(PROV.wasGeneratedBy, this.provActivity);
  }

  private void addPerformances() throws URISyntaxException {
    int performanceCounter = 0;
    boolean hasPremiere = false;
    for (String performance : PM42_PerformedExpressionCreation.getPerformances(record)) {
      PM42_PerformedExpressionCreation m42 = new PM42_PerformedExpressionCreation(performance, record, identifier, ++performanceCounter, f28);
      PF25_PerformancePlan f25 = new PF25_PerformancePlan(m42.getIdentifier());

      m42.add(f25);
      f22.add(m42);
      f25.add(f22);
      f15.add(m42);
      if (!hasPremiere && m42.isPremiere()) {
        f14.addPremiere(m42);
        f22.addPremiere(m42);
        hasPremiere = true;
      }

      model.add(m42.getModel());
      model.add(f25.getModel());
    }

  }

  private void addPrincepsPublication() throws URISyntaxException {
    String edition = PF30_PublicationEvent.getEditionPrinceps(record);
    if (edition == null) return;

    PF30_PublicationEvent f30 = new PF30_PublicationEvent(edition, record, identifier);
    PF24_PublicationExpression f24 = new PF24_PublicationExpression(f30.getIdentifier());
    PF19_PublicationWork f19 = new PF19_PublicationWork(f30.getIdentifier());

    f30.add(f24).add(f19);
    f19.add(f24);
    f14.add(f30);
    f22.add(f30);
    f24.add(f22);

    model.add(f30.getModel());
    model.add(f24.getModel());
    model.add(f19.getModel());
  }

  public Model getModel() {
    return model;
  }

}
