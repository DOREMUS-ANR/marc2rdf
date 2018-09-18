package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class F28_ExpressionCreation extends DoremusResource {
  public static final String MOTIVATIONS_REGEX = "(?i).*((comp(osé|.)|[ée]crit) (pour|à l'occasion de)|commande de).*";
  public static final String INFLUENCE_REGEX = "(?i).*inspir[eé].*";
  public static final String DATE_REGEX_1 = "Dates? de composition.+";
  public static final String DATE_REGEX_2 = ".*\\(.*comp.*\\)";
  private List<Person> composers;
  public F22_SelfContainedExpression expression;
  private F14_IndividualWork work;
  private String derivation;

  public F28_ExpressionCreation(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F28_Expression_Creation);
  }

  public F28_ExpressionCreation(Record record) {
    this(record, record.getIdentifier(), false, false);
  }

  public F28_ExpressionCreation(Record record, String identifier) {
    this(record, identifier, false, false);
  }

  public F28_ExpressionCreation(Record record, String identifier, boolean isSketch, boolean isArrangment) {
    this(identifier);
    this.record = record;

    if (record.isTUM()) parseTum();
    else if (record.isBIB())
      if (identifier.endsWith("t")) parseBIB(); // creation of aggregation work
      else parseANL(isSketch, isArrangment); // creation of musical work
  }


  private void parseANL(boolean isSketch, boolean isArrangment) {
    // composers and derivation type
    List<DataField> fields = new ArrayList<>();
    if (record.isDAV() || isArrangment || !isSketch) fields.addAll(record.getDatafieldsByCode(700));
    if (record.isMUS() && (isSketch || !isArrangment)) fields.addAll(record.getDatafieldsByCode(100));

    fields.stream()
      .map(Person::fromIntermarcField).filter(Objects::nonNull)
      .forEach(person -> {
        String function = person.getFunction();
        if (function == null) function = "arrangeur";

        switch (function) {
          case "adaptateur":
            derivation = "adaptation";
          case "arrangeur":
            derivation = "arrangement";
            break;
          case "harmonisateur":
            derivation = "harmonisation";
            break;
          case "transcripteur":
            derivation = "transcription";
            break;
          case "orchestrateur":
            derivation = "orchestration";
        }

        this.addActivity(person, function);
      });

    if (BIBRecordConverter.getCase(record) == 1) {
      record.getDatafieldsByCode(300, 'a').stream()
        .filter(txt -> txt.startsWith("Date de composition"))
        .forEach(this::addNote);
    }
  }

  private void parseBIB() {
    record.getDatafieldsByCode(700).stream()
      .filter(df -> "0180".equals(df.getString(4)))
      .map(Person::fromIntermarcField)
      .forEach(person -> addActivity(person, "collecteur"));
  }

  private void parseTum() {
    this.addTimeSpan(parseDateMachine());
    this.addNote(getDateText());

    this.composers = ArtistConverter.getArtistsInfo(record);
    composers.forEach(composer -> addActivity(composer, "compositeur"));

    // text authors
    List<DataField> tAut = this.record.getDatafieldsByCode(322);
    tAut.addAll(this.record.getDatafieldsByCode(320));
    for (DataField d : tAut) {
      Person author = Person.fromIntermarcField(d);
      if (author == null) continue;
      String function = null;
      if (d.getEtiq().equals("322"))
        switch (d.getIndicator1()) {
          case ' ':
          case '8':
            function = "auteur du texte";
            break;
          case '6':
            function = "librettiste";
            break;
          case '7':
            function = "parolier";
            break;
          case '9':
            function = "auteur de l'argument";
            break;
        }
      else switch (d.getIndicator1()) {
        case ' ':
          function = "auteur du texte";
          break;
        case '4':
          function = "librettiste";
          break;
      }

      if(function==null) continue;
      addActivity(author, function);
    }

    int motivationCount = 0;
    for (String mot : getMotivations()) {
      this.addProperty(CIDOC.P17_was_motivated_by,
        model.createResource(this.uri + "/motivation/" + ++motivationCount)
          .addProperty(RDF.type, CIDOC.E1_CRM_Entity)
          .addProperty(RDFS.label, mot, "fr")
          .addProperty(CIDOC.P3_has_note, mot, "fr")
      );
    }

    List<DataField> inspirations = this.record.getDatafieldsByCode(301);
    inspirations.addAll(this.record.getDatafieldsByCode(302));
    for (DataField insp : inspirations) {
      if (insp.getIndicator1() == '5' || insp.getIndicator1() == '7') return;
      if (!insp.isCode(3)) return;

      F15_ComplexWork targetWork = new F15_ComplexWork(insp.getString(3));
      this.addProperty(CIDOC.P15_was_influenced_by, targetWork);
    }

    int influenceCount = 0;
    for (String infl : getInfluences()) {
      this.addProperty(CIDOC.P15_was_influenced_by,
        model.createResource(this.uri + "/influence/" + ++influenceCount)
          .addProperty(RDF.type, CIDOC.E1_CRM_Entity)
          .addProperty(RDFS.label, infl, "fr")
          .addProperty(CIDOC.P3_has_note, infl, "fr")
      );
    }
  }

  private TimeSpan parseDateMachine() {
    for (ControlField field : record.getControlfieldsByCode("008")) {
      String fieldData = field.getData();

      //approximate date
      if (fieldData.charAt(36) == '?' || fieldData.charAt(46) == '?' ||
        fieldData.substring(30, 32).contains(".") ||
        fieldData.substring(40, 42).contains(".")) {

        String startString = fieldData.substring(28, 32).trim(); // could be 1723, 172. or 17..
        String endString = fieldData.substring(38, 42).trim(); // could be 1723, 172. or 17..

        if (!startString.matches("\\d.+")) startString = "";
        if (!endString.matches("\\d.+")) endString = "";

        if (startString.isEmpty() && endString.isEmpty()) continue;

        // there is at least one of the two
        if (startString.isEmpty()) startString = endString;
        else if (endString.isEmpty()) endString = startString;

        return new TimeSpan(startString, endString);
      }
      // known date
      else {
        String startYear = fieldData.substring(28, 32);

        if (startYear.matches("\\d+.+")) { // at least a digit is specified
          startYear = startYear.trim();
        } else startYear = "";

        String startMonth = fieldData.substring(32, 34).replaceAll("[.-]", "").trim();
        String startDay = fieldData.substring(34, 36).replaceAll("[.-]", "").trim();

        String endYear = fieldData.substring(38, 42);
        if (endYear.matches("\\d+.+")) { // at least a digit is specified
          endYear = fieldData.substring(38, 42).trim();
        } else endYear = "";

        String endMonth = fieldData.substring(42, 44).replaceAll("[.-]", "").trim();
        String endDay = fieldData.substring(44, 46).replaceAll("[.-]", "").trim();

        if (startYear.isEmpty() && endYear.isEmpty()) continue;

        return new TimeSpan(startYear, startMonth, startDay, endYear, endMonth, endDay);
      }
    }

    return null;
  }

  private String getDateText() {
    return record.getDatafieldsByCode("600", 'a').stream()
      .filter(date -> date.matches(DATE_REGEX_1) || date.matches(DATE_REGEX_2))
      .findFirst().orElse(null);
  }

  public List<Person> getComposers() {
    return this.composers;
  }

  public List<String> getMotivations() {
    return record.getDatafieldsByCode("600", 'a').stream()
      .filter(s -> s.matches(MOTIVATIONS_REGEX)).collect(Collectors.toList());
  }

  public List<String> getInfluences() {
    return record.getDatafieldsByCode("600", 'a').stream()
      .filter(s -> s.matches(INFLUENCE_REGEX)).collect(Collectors.toList());
  }

  public List<String> getComposerUris() {
    return this.composers.stream()
      .map(Artist::getUri).collect(Collectors.toList());
  }

  public F28_ExpressionCreation add(F17_AggregationWork f17) {
    this.addProperty(FRBROO.R19_created_a_realisation_of, f17.asResource());
    return this;
  }

  public F28_ExpressionCreation add(F22_SelfContainedExpression expression) {
    this.expression = expression;
    this.addProperty(FRBROO.R17_created, expression);
    return this;
  }

  public F28_ExpressionCreation add(F14_IndividualWork f14) {
    this.work = f14;
    this.addProperty(FRBROO.R19_created_a_realisation_of, f14);
    return this;
  }

  public F28_ExpressionCreation add(M46_SetOfTracks tracks) {
    this.addProperty(FRBROO.R17_created, tracks);
    return this;
  }

  public F28_ExpressionCreation add(F4_ManifestationSingleton manuscript) {
    this.addProperty(FRBROO.R18_created, manuscript);
    return this;
  }

  public F14_IndividualWork getWork() {
    return work;
  }

  public F22_SelfContainedExpression getExpression() {
    return expression;
  }

  public String getDerivation() {
    return derivation;
  }
}
