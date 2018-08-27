package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class BIBRecordConverter {
  private final String ark;
  public Resource provActivity, intermarcRes;
  private Model model;
  private Record record, mainRecord;

  public BIBRecordConverter(Record record, Model model, Record mainRecord) {
    this.record = record;
    this.model = model;

    this.mainRecord = (mainRecord == null) ? this.record : mainRecord;
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
    this.mainRecord = mainRecordConv.getRecord();

    this.ark = this.mainRecord.getAttrByName("IDPerenne").getData();

    // PROV-O tracing
    intermarcRes = BNF2RDF.computeProvIntermarc(ark, model);
    provActivity = BNF2RDF.computeProvActivity(record.getIdentifier(), intermarcRes, model);

    M46_SetOfTracks sot = new M46_SetOfTracks(record, df, i);

    F26_Recording recording = new F26_Recording(record, mainRecord, i);
    F29_RecordingEvent recordingEvent = new F29_RecordingEvent(record, mainRecord, i);
    recording.getProducers().forEach(recordingEvent::addProducer);
    F21_RecordingWork recordingWork = new F21_RecordingWork(record, i);

    recordingEvent.add(recording).add(recordingWork);
    recordingWork.add(recording);
//    editing.add(sot);

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

    performedExpressionCreation.add(performedExpression).add(performedWork);
    performedWork.add(performedExpression);
    performance.add(performedExpressionCreation).add(performancePlan);
    recordingEvent.add(performance);

    for (DoremusResource r : Arrays.asList(sot, recording, recordingEvent, performedExpression,
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
    BIBRecordConverter conv = new BIBRecordConverter(record, model, this, null, 0);
    // TODO fields 331
    // TODO Niv=2
  }

  private void convertDAV() {
    F3_ManifestationProductType manif = new F3_ManifestationProductType(record);

    F24_PublicationExpression publicationExpression = new F24_PublicationExpression(record);
    F30_PublicationEvent publicationEvent = new F30_PublicationEvent(record);
    F19_PublicationWork publicationWork = new F19_PublicationWork(record);

    F17_AggregationWork aggregationWork = new F17_AggregationWork(record);
    M46_SetOfTracks tracks = new M46_SetOfTracks(record);
    F28_ExpressionCreation aggregationEvent = new F28_ExpressionCreation(record);

    publicationEvent.add(publicationExpression).add(publicationWork);
    publicationWork.add(publicationExpression);
    manif.add(publicationExpression);
    aggregationWork.add(tracks);
    aggregationEvent.add(aggregationWork).add(tracks);

    M29_Editing editing = new M29_Editing(record);

    System.out.println(getCase(record));
    switch (getCase(record)) {
      case 1:
        new BIBRecordConverter(record, model, this, null, 0);
        break;
      case 3:
        int i = 0;
        BIBRecordConverter conv = new BIBRecordConverter(record, model, this, record.getDatafieldByCode(144), 0);
        for (DataField df : record.getDatafieldsByCode(744)) {
          conv = new BIBRecordConverter(record, model, this, record.getDatafieldByCode(144), 0);
        }
    }

    for (DoremusResource r : Arrays.asList(manif, publicationExpression, publicationEvent, publicationWork,
      aggregationEvent, aggregationWork, tracks, editing)) {
      r.addProvenance(intermarcRes, provActivity);
      this.model.add(r.getModel());
    }
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
