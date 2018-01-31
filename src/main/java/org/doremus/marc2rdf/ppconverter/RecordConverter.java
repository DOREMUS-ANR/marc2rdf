package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.PROV;

import java.net.URISyntaxException;
import java.time.Instant;

public class RecordConverter {
  public final Resource provActivity, intermarcRes;

  private Record record;
  private Model model;
  private String identifier;

  private boolean converted;

  public RecordConverter(Record record, Model model) throws URISyntaxException {
    this(record, model, record.getIdentifier());
  }

  public RecordConverter(Record record, Model model, String identifier) throws URISyntaxException {
    this.record = record;
    this.model = model;
    this.identifier = identifier;
    this.converted = false;

    // PROV-O tracing
    intermarcRes = computeProvIntermarc(record.getIdentifier(), model);
    provActivity = computeProvActivity(record.getIdentifier(), intermarcRes, model);

    switch (record.getType()) {
      case "AIC:14": // TUM
      case "UNI:100": // Ouvres
        this.convertUNI100();
        break;
      case "UNI:4": // Concerts video
        this.convertUNI4();
        break;
      case "UNI:44": // Concerts video
        this.convertUNI44();
        break;
      default:
        System.out.println("Skipping not recognized PP notice type " + record.getType() + " for file " + record.getIdentifier());

    }
  }

  private void convertUNI4() throws URISyntaxException {
    if (!"UNI4C".equals(record.getDatafieldsByCode("019", 'a').get(0)))
      return;
    this.converted = true;

    PM46_SetOfTracks tracks = new PM46_SetOfTracks(record);
    PM29_Editing editing = new PM29_Editing(record);
    PF29_RecordingEvent recording = new PF29_RecordingEvent(record);
    editing.add(tracks);

    model.add(tracks.getModel())
    .add(editing.getModel())
    .add(recording.getModel());
  }

  private void convertUNI44() throws URISyntaxException {
    if (!"UNI44C".equals(record.getDatafieldsByCode("019", 'a').get(0)))
      return;
    this.converted = true;

    PM24_Track track = new PM24_Track(record);
    PM42_PerformedExpressionCreation perfExpression = new PM42_PerformedExpressionCreation(record);

    model.add(track.getModel());
    model.add(perfExpression.getModel());
  }

  private void convertUNI100() throws URISyntaxException {
    this.converted = true;

    PF28_ExpressionCreation f28 = new PF28_ExpressionCreation(record, identifier);
    PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression(record, identifier, f28.getComposerUris());
    PF14_IndividualWork f14 = new PF14_IndividualWork(record, identifier);
    PF15_ComplexWork f15 = new PF15_ComplexWork(record, identifier);
    PM45_DescriptiveExpressionAssignment f42 = new PM45_DescriptiveExpressionAssignment(record, identifier);
//    PF40_IdentifierAssignment f40 = new PF40_IdentifierAssignment(record);
//    PF50_ControlledAccessPoint f50 = new PF50_ControlledAccessPoint(record);

    addPrincepsPublication(f22, f14);
    addPerformances(f22, f14, f15, f28);

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

    Utils.catalogToUri(model, f28.getComposerUris());
  }

  private void addProvenanceTo(DoremusResource res) {
    res.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, model.createResource(PP2RDF.doremusURI))
      .addProperty(PROV.wasDerivedFrom, this.intermarcRes)
      .addProperty(PROV.wasGeneratedBy, this.provActivity);
  }

  private void addPerformances(PF22_SelfContainedExpression f22, PF14_IndividualWork f14,
                               PF15_ComplexWork f15, PF28_ExpressionCreation f28) throws URISyntaxException {
    int performanceCounter = 0;
    boolean hasPremiere = false;
    for (String performance : PM42_PerformedExpressionCreation.getPerformances(record)) {
      PM42_PerformedExpressionCreation m42 =
        new PM42_PerformedExpressionCreation(performance, identifier, ++performanceCounter, f28);
      PF25_PerformancePlan f25 = new PF25_PerformancePlan(m42.getIdentifier());

      m42.add(f25).add(f22);
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

  private void addPrincepsPublication(PF22_SelfContainedExpression f22, PF14_IndividualWork f14) throws URISyntaxException {
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

  private static Resource computeProvActivity(String identifier, Resource intermarc, Model model) throws
    URISyntaxException {
    return model.createResource(ConstructURI.build("pp", "prov", identifier).toString())
      .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
      .addProperty(PROV.used, intermarc)
      .addProperty(RDFS.comment, "Reprise et conversion de la notice MARC de la Philharmonie de Paris", "fr")
      .addProperty(RDFS.comment, "Resumption and conversion of the MARC record of the Philharmonie de Paris", "en")
      .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);

  }

  private static Resource computeProvIntermarc(String identifier, Model model) {
    return model.createResource("http://data.doremus.org/source/philharmonie/" + identifier)
      .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, model.createResource(PP2RDF.organizationURI));
  }

  public boolean isConverted() {
    return converted;
  }
}
