package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class BIBRecordConverter {
  private static final Pattern EXTRAIT_PATTERN = Pattern.compile("(extr(ait|\\.)|choix)", Pattern.CASE_INSENSITIVE);

  private final String ark;
  private final BIBRecordConverter mainRecordConv;
  public Resource provActivity, intermarcRes;
  private Model model;
  private Record record, mainRecord;
  private F24_PublicationExpression publicationExpression;
  private F3_ManifestationProductType manif;
  private M46_SetOfTracks tracks;
  private M29_Editing editing;

  public BIBRecordConverter(Record record, Model model, BIBRecordConverter mainRecordConv) {
    this.record = record;
    this.model = model;
    this.mainRecordConv = mainRecordConv;

    this.mainRecord = (mainRecordConv == null) ? this.record : mainRecordConv.getRecord();
    this.ark = this.mainRecord.getAttrByName("IDPerenne").getData();

    // PROV-O tracing
    intermarcRes = BNF2RDF.computeProvIntermarc(ark, model);
    provActivity = BNF2RDF.computeProvActivity(record.getIdentifier(), intermarcRes, model);

    if (BNF2RDF.getRecordCode(record).equals("g"))
      if (isANL(record)) convertANL();
      else convertDAV();
  }

  public BIBRecordConverter(Record record, Model model, BIBRecordConverter mainRecordConv, DataField df, int i) {
    // inner
    this.record = record;
    this.model = model;
    this.mainRecordConv = mainRecordConv;
    this.mainRecord = mainRecordConv.getRecord();

    System.out.println("--> " + record.getIdentifier() + "|" + i + (isANL(record) ? " ANL" + record.getLevel() : ""));
    this.ark = mainRecordConv.ark;

    // PROV-O tracing
    intermarcRes = BNF2RDF.computeProvIntermarc(ark, model);
    provActivity = BNF2RDF.computeProvActivity(record.getIdentifier(), intermarcRes, model);

    // work
    F22_SelfContainedExpression f22 = null;
    F28_ExpressionCreation f28 = null;
    F15_ComplexWork f15 = null;
    F14_IndividualWork f14_outer = null;
    boolean isArrangment = false;
    Property prop = MUS.U58_has_full_published_recording;

    if (df != null) {
      String l = df.getString('l');
      if (l == null) l = "";
      String _id = df.getString(3);
      f22 = new F22_SelfContainedExpression(_id);
      f28 = new F28_ExpressionCreation(_id);
      f15 = new F15_ComplexWork(_id);
      f14_outer = new F14_IndividualWork(_id);
      prop = EXTRAIT_PATTERN.matcher(l).find() ?
        MUS.U59_has_partial_published_recording : MUS.U58_has_full_published_recording;

      isArrangment = l.toLowerCase().contains("arr.");
    }
    if (isArrangment || f22 == null) {
      f22 = new F22_SelfContainedExpression(record);
      F14_IndividualWork f14 = new F14_IndividualWork(record);
      f28 = new F28_ExpressionCreation(record);
      if (f15 == null) f15 = new F15_ComplexWork(record);

      f14.add(f22);
      f28.add(f22).add(f14);
      f15.add(f14).add(f22);

      if (isArrangment) {
        f14.addProperty(FRBROO.R2_is_derivative_of, f14_outer);
        Resource assignment = model.createResource(f14.getUri() + "/derivation_assignment")
          .addProperty(RDF.type, MUS.M39_Derivation_Type_Assignment)
          .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
          .addProperty(CIDOC.P41_classified, f14.asResource())
          .addProperty(CIDOC.P41_classified, f22.asResource());

        String deriv = f28.getDerivation();
        if (deriv != null) {
          assignment.addProperty(CIDOC.P42_assigned, deriv);
          f14.setDerivationType(deriv);
        }
      }
    }

    // handwritten score
    DataField manuscriptField = record.getDatafieldsByCode(325).stream().findFirst().orElse(null);
    F4_ManifestationSingleton manuscript = null;
    if (manuscriptField != null) {
      String manuId = manuscriptField.isCode('u') ? manuscriptField.getString('u') : record.getIdentifier() + i;
      manuscript = new F4_ManifestationSingleton(manuId, manuscriptField);
      manuscript.add(f22);
      f28.add(manuscript);
    }

    tracks = new M46_SetOfTracks(record, df, i);

    F26_Recording recording = new F26_Recording(record, mainRecord, i);
    F29_RecordingEvent recordingEvent = new F29_RecordingEvent(record, mainRecord, i);
    recording.getProducers().forEach(recordingEvent::addProducer);
    F21_RecordingWork recordingWork = new F21_RecordingWork(record, i);

    recordingEvent.add(recording).add(recordingWork);
    recordingWork.add(recording);

    M43_PerformedExpression performedExpression = new M43_PerformedExpression(record, record.getIdentifier() + i, df);
    M44_PerformedWork performedWork = new M44_PerformedWork(record, record.getIdentifier() + i, df);
    M42_PerformedExpressionCreation performedExpressionCreation = new M42_PerformedExpressionCreation(record,
      record.getIdentifier() + i);

    F31_Performance performance = new F31_Performance(record, record.getIdentifier() + i);
    F25_PerformancePlan performancePlan = new F25_PerformancePlan(record.getIdentifier() + i, df);

    recordingEvent.addTimeSpan(performance.getTime());
    recordingEvent.setPlace(performance.getPlace());
    performedExpressionCreation.addTimeSpan(performance.getTime());
    performedExpressionCreation.setPlace(performance.getPlace());
    performedExpressionCreation.setGeoContext(performance.getGeoContext());

    performedExpression.add(f22);
    performance.add(f22);

    f15.add(performedExpression).add(performedWork);
    this.model.add(f15.getModel()).add(f28.getModel());

    performedWork.add(performedExpression);
    performancePlan.add(f22);
    performedExpressionCreation.add(performedExpression).add(performedWork)
      .add(performancePlan).add(manuscript);
    performance.add(performedExpressionCreation).add(performancePlan);
    recordingEvent.add(performance);
    tracks.add(performedExpression);

    mainRecordConv.publicationExpression.add(performedExpression).addProperty(prop, f22);
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

  public static boolean isANL(Record record) {
    ControlField leader = record.getControlfieldByCode("leader");
    return leader.getData().charAt(8) == 'd';
  }

  private void convertANL() {
    DataField df = record.getDatafieldByCode(144);
    if (df == null) df = record.getDatafieldByCode(744);
    BIBRecordConverter conv = new BIBRecordConverter(record, model, this.mainRecordConv, df, 0);

    int i = 0;

    for (DataField df331 : record.getDatafieldsByCode(331)) {
      M24_Track track = new M24_Track(record.getIdentifier() + ++i, df331);
      conv.tracks.add(track);
    }

  }

  private void convertDAV() {
    this.manif = new F3_ManifestationProductType(record);

    this.publicationExpression = new F24_PublicationExpression(record);
    F30_PublicationEvent publicationEvent = new F30_PublicationEvent(record);
    F19_PublicationWork publicationWork = new F19_PublicationWork(record);

    F17_AggregationWork aggregationWork = new F17_AggregationWork(record);
    this.tracks = new M46_SetOfTracks(record);
    F28_ExpressionCreation aggregationEvent = new F28_ExpressionCreation(record);

    publicationEvent.add(publicationExpression).add(publicationWork);
    publicationWork.add(publicationExpression);
    manif.add(publicationExpression);
    aggregationWork.add(tracks);
    aggregationEvent.add(aggregationWork).add(tracks);

    editing = new M29_Editing(record);

    System.out.println("case " + getCase(record));
    switch (getCase(record)) {
      case 3:
        int i = 0;
        for (DataField df : record.getDatafieldsByCode(744)) {
          this.incorporate(new BIBRecordConverter(record, model, this, df, ++i));
        }
      case 1:
        this.incorporate(new BIBRecordConverter(record, model, this, record.getDatafieldByCode(144), 0));
    }

    for (DoremusResource r : Arrays.asList(manif, publicationExpression, publicationEvent, publicationWork,
      aggregationEvent, aggregationWork, tracks, editing)) {
      r.addProvenance(intermarcRes, provActivity);
      this.model.add(r.getModel());
    }
  }

  private void incorporate(BIBRecordConverter bibRecordConverter) {
    this.model.add(bibRecordConverter.getModel());
  }

  private static final List<String> CONDUCTOR_CODES = Arrays.asList("1080", "1040");

  public static int getCase(Record record) {
    if (isANL(record)) return 2;

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


  public Model getModel() {
    return model;
  }

  public String getArk() {
    return ark;
  }


}
