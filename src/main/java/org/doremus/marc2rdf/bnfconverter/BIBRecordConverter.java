package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
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
  public Resource provActivity, intermarcRes;
  private Model model;
  private Record record, mainRecord;
  private F24_PublicationExpression publicationExpression;
  private F3_ManifestationProductType manif;
  private M46_SetOfTracks tracks;

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

    // work
    String l = df.getString('l');
    String _id = df.getString(3);
    F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(_id);
    F28_ExpressionCreation f28 = new F28_ExpressionCreation(_id);
    F15_ComplexWork f15 = new F15_ComplexWork(_id);

    Property prop = EXTRAIT_PATTERN.matcher(l).find() ?
      MUS.U59_has_partial_published_recording : MUS.U58_has_full_published_recording;

    if (l.toLowerCase().contains("arr.")) {
      F28_ExpressionCreation derivedCreation = getDerivedWork();
      F14_IndividualWork derived = derivedCreation.getWork();
      f22 = derivedCreation.getExpression();
      derived.addProperty(FRBROO.R2_is_derivative_of, new F14_IndividualWork(_id));
      f15.add(derived);
    }

    // handwritten score
    DataField manuscriptField = record.getDatafieldsByCode(325).stream().findFirst().orElse(null);
    F4_Manifestation_Singleton manuscript = null;
    if (manuscriptField != null) {
      manuscript = new F4_Manifestation_Singleton(df);
      manuscript.add(f22);
      f28.add(manuscript);
    }

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
    sot.add(performedExpression);

    mainRecordConv.publicationExpression.add(performedExpression).addProperty(prop, f22);
    mainRecordConv.tracks.add(performedExpression);

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


  private F28_ExpressionCreation getDerivedWork() {
    F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(record.getIdentifier() + "d");
    F28_ExpressionCreation f28 = new F28_ExpressionCreation(record.getIdentifier() + "d");
    F14_IndividualWork f14 = new F14_IndividualWork(record.getIdentifier() + "d");

    Resource assignment = model.createResource(f14.getUri() + "/derivation_assignment")
      .addProperty(RDF.type, MUS.M39_Derivation_Type_Assignment)
      .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
      .addProperty(CIDOC.P41_classified, f14.asResource())
      .addProperty(CIDOC.P41_classified, f22.asResource());


    // title
    List<Literal> titles = BIBDoremusResource.parseTitleField(record.getDatafieldByCode(245), true, false);
    titles.forEach(f22::setTitle);
    // title translation
    titles = BIBDoremusResource.parseTitleField(record.getDatafieldByCode(247), true, false);
    titles.forEach(f22::setVariantTitle);

    // casting
    M6_Casting casting = new M6_Casting(record, f22.getUri() + "/casting/1");
    if (casting.doesContainsInfo()) f22.addProperty(MUS.U13_has_casting, casting);

    // composers and derivation type
    record.getDatafieldsByCode(700)
      .forEach(df -> {
        Person person = ArtistConverter.parseArtistField(df);
        String role = "arrangeur";
        String deriv = null;

        switch (df.getString(4)) {
          case "0010":
          case "0011":
          case "0013":
            role = "adaptateur";
            deriv = "adaptation";
            break;
          case "0050":
          case "0051":
          case "0053":
            deriv = "arrangement";
            break;
          case "0430":
            role = "harmonisateur";
            deriv = "harmonisation";
            break;
          case "0730":
            role = "transcripteur";
            deriv = "transcription";
            break;
          case "0780":
            role = "orchestrateur";
            deriv = "orchestration";
        }

        f28.addActivity(person, role);
        if (deriv != null) {
          f14.setDerivationType(deriv);
          assignment.addProperty(CIDOC.P42_assigned, deriv);
        }
      });

    f14.add(f22);
    f28.add(f22).add(f14);
    return f28;
  }
}
