package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class BIBRecordConverter {
  private static final Pattern EXTRAIT_PATTERN = Pattern.compile("(extr(aits?|\\.)|choix)", Pattern.CASE_INSENSITIVE);
  private static final Pattern ARR_PATTERN = Pattern.compile("(Arr\\.|(Réduc|Orchestra)tion|Esquisse)", Pattern.CASE_INSENSITIVE);

  private final String ark;
  private final BIBRecordConverter mainRecordConv;
  public Resource provActivity, intermarcRes;
  private Model model;
  private Record record, mainRecord;
  private F24_PublicationExpression publicationExpression;
  private F3_ManifestationProductType manif;
  private M46_SetOfTracks tracks;
  private M29_Editing editing;
  private F31_Performance performance;
  private F25_PerformancePlan performancePlan;
  private F29_RecordingEvent recordingEvent;

  public BIBRecordConverter(Record record, Model model, BIBRecordConverter mainRecordConv) {
    this.record = record;
    this.model = model;
    this.mainRecordConv = mainRecordConv;

    this.mainRecord = (mainRecordConv == null) ? this.record : mainRecordConv.getRecord();
    this.ark = this.mainRecord.getAttrByName("IDPerenne").getData();

    // PROV-O tracing
    intermarcRes = BNF2RDF.computeProvIntermarc(ark, model);
    provActivity = BNF2RDF.computeProvActivity(record.getIdentifier(), intermarcRes, model);


    if (record.isANL()) convertANL();
    else convertBIB();
  }

  public BIBRecordConverter(Record record, Model model, BIBRecordConverter mainRecordConv,
                            DataField df, int index, String identifier) {
    // inner
    this.record = record;
    this.model = model;
    this.mainRecordConv = mainRecordConv;
    this.mainRecord = mainRecordConv.getRecord();

    this.ark = mainRecordConv.ark;
    System.out.println("--> " + identifier + (record.isANL() ? " ANL" + record.getLevel() : ""));

    // PROV-O tracing
    intermarcRes = BNF2RDF.computeProvIntermarc(ark, model);
    provActivity = BNF2RDF.computeProvActivity(record.getIdentifier(), intermarcRes, model);

    // work
    F22_SelfContainedExpression f22 = null;
    F28_ExpressionCreation f28 = null;
    F15_ComplexWork f15 = null;
    F14_IndividualWork f14_outer = null;
    boolean isArrangment = false;
    boolean isExtrait = false;
    boolean isSketch = false;
    boolean isScientificEdition = record.getDatafieldsByCode(700, 4)
      .stream().anyMatch("0360"::equals);

    if (df != null) {
      String l = df.getString('l');
      if (l == null) l = "";
      String _id = df.getString(3);
      f22 = new F22_SelfContainedExpression(_id);
      f28 = new F28_ExpressionCreation(_id);
      f15 = new F15_ComplexWork(_id);
      f14_outer = new F14_IndividualWork(_id);

      isExtrait = EXTRAIT_PATTERN.matcher(l).find();
      isSketch = l.contains("Esquisse");
      isArrangment = ARR_PATTERN.matcher(l).find();
    }

    if (isScientificEdition || isArrangment || f22 == null) {
      if (isExtrait) {
        F23_Expression_Fragment f23 = new F23_Expression_Fragment(record, identifier, index);
        f22.add(f23);
        f22 =f23;
      } else f22 = new F22_SelfContainedExpression(record, identifier, index);

      F14_IndividualWork f14 = new F14_IndividualWork(identifier);
      f28 = new F28_ExpressionCreation(record, identifier, isSketch, isArrangment);
      if (f15 == null) f15 = new F15_ComplexWork(record);

      if (isArrangment) {
        f14.addProperty(FRBROO.R2_is_derivative_of, f14_outer);
        Resource assignment = model.createResource(f14.getUri() + "/derivation_assignment")
          .addProperty(RDF.type, MUS.M39_Derivation_Type_Assignment)
          .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
          .addProperty(CIDOC.P41_classified, f14.asResource())
          .addProperty(CIDOC.P41_classified, f22.asResource());

        String derivation = f28.getDerivation();
        if (derivation != null) {
          Resource deriv = VocabularyManager.getVocabulary("derivation").findConcept(derivation, false);
          assignment.addProperty(CIDOC.P42_assigned, deriv);
          f14.setDerivationType(deriv);
        }
      }

      f14.add(f22);
      f28.add(f22).add(f14);
      f15.add(f14).add(f22);
      this.model.add(f15.getModel());
    }

    f22.extendFromMUS(record);

    Property prop = record.isMUS() ? CIDOC.P165_incorporates : isExtrait ?
      MUS.U59_has_partial_published_recording : MUS.U58_has_full_published_recording;
    mainRecordConv.publicationExpression.addProperty(prop, f22);


    if (!record.isDAV()) return;

    // handwritten score
    DataField manuscriptField = record.getDatafieldsByCode(325).stream().findFirst().orElse(null);
    F4_ManifestationSingleton manuscript = null;
    if (manuscriptField != null) {
      String manuId = manuscriptField.isCode('u') ? manuscriptField.getString('u') : identifier;
      manuscript = new F4_ManifestationSingleton(manuId, manuscriptField);
      manuscript.add(f22);
      f28.add(manuscript);
    }

    tracks = new M46_SetOfTracks(record, df, identifier);

    M43_PerformedExpression performedExpression = new M43_PerformedExpression(record, identifier, df);
    M44_PerformedWork performedWork = new M44_PerformedWork(record, identifier, df);
    M42_PerformedExpressionCreation performedExpressionCreation =
      new M42_PerformedExpressionCreation(record, identifier);

    performance = new F31_Performance(record, identifier);
    performancePlan = new F25_PerformancePlan(identifier);
    recordingEvent = new F29_RecordingEvent(record, identifier);
    if (!performance.hasTimeSpace() && mainRecordConv.performance.hasInformation()) {
      performance = mainRecordConv.performance;
      performancePlan = mainRecordConv.performancePlan;
      recordingEvent = mainRecordConv.recordingEvent;
    }

    F26_Recording recording = new F26_Recording(record, identifier);
    recordingEvent.addTimeSpan(performance.getTime());
    recordingEvent.setPlace(performance.getPlace());

    F21_RecordingWork recordingWork = new F21_RecordingWork(record, identifier);

    recording.getProducers().forEach(recordingEvent::addProducer);
    recordingWork.add(recording);
    recordingEvent.add(recording).add(recordingWork);


    performedExpressionCreation.addTimeSpan(performance.getTime());
    performedExpressionCreation.setPlace(performance.getPlace());
    performedExpressionCreation.setGeoContext(performance.getGeoContext());

    performedExpression.add(f22);

    f15.add(performedExpression).add(performedWork);
    this.model.add(f15.getModel()).add(f28.getModel());

    performedWork.add(performedExpression);
    performancePlan.add(f22);
    performedExpressionCreation.add(performedExpression).add(performedWork)
      .add(performancePlan).add(manuscript);
    performance.add(f22).add(performedExpressionCreation).add(performancePlan);
    recordingEvent.add(performance);
    tracks.add(performedExpression);

    mainRecordConv.publicationExpression.add(performedExpression);
    mainRecordConv.tracks.add(tracks);
    mainRecordConv.editing.add(tracks);

    for (DoremusResource r : Arrays.asList(tracks, recording, recordingEvent, performedExpression,
      performance)) {
      r.addProvenance(intermarcRes, provActivity);
      this.model.add(r.getModel());
    }

  }

  private Record getRecord() {
    return record;
  }

  private void convertANL() {
    BIBRecordConverter conv = null;

    List<Record> detailedANL = record.getSubRecords();
    if (detailedANL.isEmpty()) detailedANL = Collections.singletonList(record);

    for (Record r : detailedANL) {
      DataField df = r.getDatafieldByCode(144);
      if (df == null) df = r.getDatafieldByCode(744);
      conv = new BIBRecordConverter(r, model, this.mainRecordConv, df, -1, r.getIdentifier());
    }

    int i = 0;

    for (DataField df331 : record.getDatafieldsByCode(331)) {
      M24_Track track = new M24_Track(record.getIdentifier() + ++i, df331);
      conv.tracks.add(track);
    }

  }

  private void convertBIB() {
    this.manif = new F3_ManifestationProductType(record);
    if (record.isMUS()) {
      F32_Carrier_Production_Event cpe = new F32_Carrier_Production_Event(record);
      cpe.add(manif);
      if (cpe.hasInformation()) this.model.add(cpe.getModel());
    }

    this.publicationExpression = new F24_PublicationExpression(record);
    F30_PublicationEvent publicationEvent = new F30_PublicationEvent(record);
    F19_PublicationWork publicationWork = new F19_PublicationWork(record);

    if (record.isDAV()) {
      F17_AggregationWork aggregationWork = new F17_AggregationWork(record);
      this.tracks = new M46_SetOfTracks(record);
      F28_ExpressionCreation aggregationEvent = new F28_ExpressionCreation(record, record.getIdentifier() + "t");

      aggregationWork.add(tracks);
      aggregationEvent.add(aggregationWork).add(tracks).addProvenance(intermarcRes, provActivity);
      performance = new F31_Performance(record);
      performancePlan = new F25_PerformancePlan(record.getIdentifier());
      recordingEvent = new F29_RecordingEvent(record);

      editing = new M29_Editing(record);

      this.model.add(aggregationEvent.getModel());
    }
    publicationEvent.add(publicationExpression).add(publicationWork);
    publicationWork.add(publicationExpression);
    manif.add(publicationExpression);


    System.out.println("case " + getCase(record));
    switch (getCase(record)) {
      case 3:
        int i = 0;
        for (DataField df : record.getDatafieldsByCode(744)) {
          this.incorporate(new BIBRecordConverter(record, model, this, df, i, record.getIdentifier() + ++i));
        }
      case 1:
        this.incorporate(new BIBRecordConverter(record, model, this,
          record.getDatafieldByCode(144), -1, record.getIdentifier()));
    }

    for (DoremusResource r : Arrays.asList(manif, publicationExpression, publicationEvent, publicationWork, tracks, editing)) {
      if (r == null) continue;
      r.addProvenance(intermarcRes, provActivity);
      this.model.add(r.getModel());
    }
  }

  private void incorporate(BIBRecordConverter bibRecordConverter) {
    this.model.add(bibRecordConverter.getModel());
  }

  private static final List<String> CONDUCTOR_CODES = Arrays.asList("1080", "1040");

  public static int getCase(Record record) {
    if (record.isANL()) return 2;

    // case 1 or case 3
    if (record.getDatafieldsByCode(744).size() == 0)
      if (record.getDatafieldsByCode(144).size() > 0)
        return 1;
      else return -1;

    List<DataField> z101 = record.getDatafieldsByCode(101);
    List<DataField> z111 = record.getDatafieldsByCode(111);
    List<DataField> z701 = record.getDatafieldsByCode(701);
    List<DataField> z711 = record.getDatafieldsByCode(711);
    List<DataField> z100 = record.getDatafieldsByCode(100);
    List<DataField> z700 = record.getDatafieldsByCode(700);
    List<DataField> z110 = record.getDatafieldsByCode(110);
    List<DataField> z710 = record.getDatafieldsByCode(710);

    if (record.isDAV()) {
      z101.addAll(z701); // person
      z111.addAll(z711); // ensemble

      if (z101.size() > 1) return -1;
      if (z101.size() > 0 && z100.size() == 0) return -1;

      // conductors should not be counted
      z101 = z101.stream().filter(x -> {
        String code = x.getString('4');
        return !CONDUCTOR_CODES.contains(code);
      }).collect(Collectors.toList());

      z101.addAll(z111);
      return z101.size() > 1 ? -1 : 3;
    }

    z100.addAll(z110); // should be present 1
    z700.addAll(z710); // should be absent

    // composers should not be counted
    z700 = z700.stream().filter(x -> !"0220".equals(x.getString('4'))).collect(Collectors.toList());
    return (z100.size() == 1 && z700.size() == 0) ? 3 : -1;
  }


  public Model getModel() {
    return model;
  }

  public String getArk() {
    return ark;
  }


}
