package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.CRO;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class F3_ManifestationProductType extends DoremusResource {
  private static final String DISTRIB_REGEX_DAV = "^distrib\\.";
  private static final String DISTRIB_REGEX_MUS = "\\[?di(strib|ff)\\.]?";

  private static final String CARRIER_DURATION_REGEX = "(\\d+) ([^(]+) \\((.+)\\)";
  private static final Pattern CARRIER_DURATION_PATTERN = Pattern.compile(CARRIER_DURATION_REGEX);
  private static final String PSPEED_REGEX = "\\d{2} t";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile("(\\d+(?:x\\d+)?) ?(.+)");
  private static final String BROCHURE_REGEX = "(?:^|[\\[(])(\\d+)]? p\\.?([\\])]|$)";
  private static final Pattern BROCHURE_PATTERN = Pattern.compile(BROCHURE_REGEX);

  private Pattern distribPattern;
  private int countPagination;

  public F3_ManifestationProductType(Record record) {
    super(record);
    this.setClass(FRBROO.F3_Manifestation_Product_Type);
    this.addProperty(DC.identifier, this.identifier);

    countPagination = 0;
    this.distribPattern = Pattern.compile(record.isMUS() ? DISTRIB_REGEX_MUS : DISTRIB_REGEX_DAV, Pattern.CASE_INSENSITIVE);

    this.addProperty(MUS.CLU206_should_have_media_type, parseMediaType());

    if (record.isDAV()) {
      // carrier type
      List<String> a051 = record.getDatafieldsByCode("051", 'a');
      record.getDatafieldsByCode("050", 'a')
        .forEach(a -> this.addProperty(MUS.CLU207_should_have_carrier_type, Carrier.fromField050a(a, a051)));

      // container
      for (String cont : record.getDatafieldsByCode("028", 'c')) {
        this.addProperty(MUS.CLU198_should_have_container, model.createResource()
          .addProperty(RDF.type, MUS.M171_Container)
          .addProperty(RDFS.label, cont.replaceAll("^conditionn[ée] dans", ""))
          .addProperty(CIDOC.P3_has_note, cont));
      }

      // sys requirements
      record.getDatafieldsByCode("337")
        .forEach(sr -> this.addProperty(MUS.U194_has_system_requirements,
          sr.getString('k') + " : " + sr.getString('a')));
    } else if (record.isMUS()) {
      // carrier type
      String carrier = "volume";
      if (record.getDatafieldsByCode("020", 'b').stream().anyMatch(x -> x.contains("en feuilles")))
        carrier = "feuille";
      else if (record.getDatafieldsByCode(280, 'a').stream()
        .map(F3_ManifestationProductType::extractPageNumbers)
        .anyMatch(i -> i > 0 && i <= 48))
        carrier = "brochure";

      this.addProperty(MUS.CLU207_should_have_carrier_type, carrier, "fr");

      // pagination
      record.getDatafieldsByCode(280, 'a').forEach(this::parsePagination);
    }

    // distribution rights
    Artist distributor = parseDistributor();
    String time = parseDistributionTime();
    if (distributor != null || time != null) {
      String drUri = this.uri + "/distribution_right";
      DistributionRight dr = new DistributionRight(drUri, distributor, time);
      this.addProperty(CIDOC.P104_is_subject_to, dr);
    }

    // carrier extent
    for (String ce : record.getDatafieldsByCode("280", 'a')) {
      Matcher m = CARRIER_DURATION_PATTERN.matcher(ce);
      if (!m.find()) continue;

      this.addProperty(MUS.U208_has_extent_of_carrier,
        record.isDAV() ? m.group(1) + " " + m.group(2) : m.group(0));

      if (record.isDAV()) {
        for (String d : m.group(3).split(","))
          this.addProperty(MUS.CLU53_should_have_duration, Utils.duration2iso(d), XSDDatatype.XSDdayTimeDuration);
        this.addDimension(m.group(1), m.group(2));
      }
    }

    record.getDatafieldsByCode("280", 'd').forEach(this::addDimension);

      ControlField control9 = record.getControlfieldByCode("009");
    if (control9 != null) parseAuxiliary(control9.getData());
  }

  private void parsePagination(String txt) {
    int multiplier = 1;
    if (txt.contains("chacune"))
      multiplier = Integer.parseInt(txt.substring(0, 2).trim());

    List<Integer> numbers = Arrays.stream(txt.split("[-,]"))
      .map(F3_ManifestationProductType::extractPageNumbers)
      .filter(n -> n > 0).collect(Collectors.toList());

    if (numbers.size() == 0) return;
    for (int i = 1; i < multiplier; i++)
      //noinspection CollectionAddedToSelf
      numbers.addAll(numbers);

    numbers.forEach(n -> this.addProperty(MUS.CLU210_should_have_pagination, createPagination(n)));
  }

  private Resource createPagination(Integer n) {
    return model.createResource(this.uri + "/pagination/" + ++countPagination)
      .addProperty(RDF.type, CIDOC.E54_Dimension)
      .addProperty(CIDOC.P90_has_value, model.createTypedLiteral(n))
      .addProperty(CIDOC.P91_has_unit, "page");
  }

  public F3_ManifestationProductType(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F3_Manifestation_Product_Type);
    countPagination = 0;
  }

  public static F3_ManifestationProductType fromField(DataField df, String identifier) {
    if (!("280".equals(df.getEtiq()))) return null;

    String field = df.getString('e');
    if (field == null || field.isEmpty()) return null;
    String carrier = field.split(":")[0];
    String dim = null;
    if (field.contains(";"))
      dim = field.substring(field.lastIndexOf(";") + 1).trim();

    if (carrier == null && dim == null)
      return null;

    F3_ManifestationProductType f3 = new F3_ManifestationProductType(identifier);
    f3.addProperty(MUS.U208_has_extent_of_carrier, carrier);
    f3.addDimension(dim);

    return f3;
  }

  private static Integer extractPageNumbers(String input) {
    Matcher m = BROCHURE_PATTERN.matcher(input);
    if (!m.find()) return -1;
    return Integer.parseInt(m.group(1));
  }

  private void addDimension(String text) {
    if (text == null) return;
    text = text.replaceAll("[.)]", "");
    Matcher m = DIMENSION_PATTERN.matcher(text);
    if (!m.find()) return;
    this.addDimension(m.group(1), m.group(2));
  }

  private void addDimension(String value, String unit) {
    this.addProperty(FRBROO.CLP43_should_have_dimension, model.createResource()
      .addProperty(RDF.type, CIDOC.E54_Dimension)
      .addProperty(CIDOC.P90_has_value, Utils.toSafeNumLiteral(value))
      .addProperty(CIDOC.P91_has_unit, unit));
  }

  private void parseAuxiliary(String control9) {
    // number of parts
    if (control9.length() >= 45)
      this.addProperty(FRBROO.CLP57_should_have_number_of_parts, Utils.toSafeNumLiteral(control9.substring(42, 45)));

    String note = null;
    String x = control9.substring(record.isDAV() ? 8 : 9, 10);
    if (x.startsWith(" ") || x.startsWith("#")) note = "pas de restriction de communication";

    switch (x) {
      case "11":
      case "1":
        note = "communication soumise à restriction: communication sur accord de l’ayant - droit";
        break;
      case "13":
      case "3":
        note = "communication soumise à restriction: communication interdite pendant une période déterminée";
        break;
      case "14":
      case "4":
        note = "communication soumise à restriction: non communicable";
        break;
    }

    if (note != null)
      this.addProperty(CIDOC.P104_is_subject_to, model.createResource(this.uri + "/communication_right")
        .addProperty(RDF.type, CRO.CommunicationRight)
        .addProperty(CIDOC.P3_has_note, note)
        .addProperty(RDFS.comment, note)
      );


    for (String field : record.getDatafieldsByCode(280, 'c')) {
      for (String ps : field.split(",")) {
        ps = ps.trim();
        char control = record.isDAV() ? control9.charAt(14) : 'z';
        switch (control) {
          case 'a':
            if (!ps.matches(PSPEED_REGEX)) {
              this.addProperty(MUS.U215_has_groove_caracteristics, ps);
              break;
            }
          case 'b':
            // playing speed
            if (ps.matches(PSPEED_REGEX))
              this.addProperty(MUS.CLU217_should_have_playing_speed, ps);
            break;
          case 'c':
            if (ps.matches("[AD][AD]D")) this.addProperty(MUS.U213_has_recording_method, ps);
            else this.addProperty(MUS.U212_has_technical_features, ps);
          default:
            this.addProperty(MUS.U212_has_technical_features, ps);
        }
      }
    }
  }

  private String parseMediaType() {
    ControlField cf = record.getControlfieldByCode("009");
    if (cf == null) return null;
    char c = cf.getData().charAt(0);
    switch (c) {
      case 'c':
        return "unmediated";
      case 'g':
        return "audio";
      case 'h':
        return "video";
      default:
        return null;
    }
  }

  private String parseDistributionTime() {
    String year = record.getDatafieldsByCode("044", 'c').stream()
      .filter(x -> x.startsWith("d"))
      .map(x -> x.substring(1).trim())
      .findFirst().orElse(null);
    if (year != null) return year;

    // search for time
    DataField df = record.getDatafieldByCode(260);
    if (df == null) return null;

    List<Subfield> sf = df.getSubfields();
    boolean found = false;

    for (Subfield s : sf) {
      if (found && 'd' == s.getCode()) {
        Matcher m = TimeSpan.YEAR_PATTERN.matcher(s.getData());
        found = m.find();
        if (found) return m.group(0);
      } else if ('c' == s.getCode()) {
        if (distribPattern.matcher(s.getData()).find())
          found = true;
      } else found = false;
    }

    return null;
  }

  private Artist parseDistributor() {
    Artist distributor = Person.fromIntermarcField(record.getDatafieldByCode(721));
    if (distributor != null)
      distributor = CorporateBody.fromIntermarcField(record.getDatafieldByCode(731));

    if (distributor == null) return null;

    // search for residence
    DataField df = record.getDatafieldByCode(260);
    if (record.isMUS() && df.isCode('r')) return distributor;

    List<Subfield> sf = df.getSubfields();
    Collections.reverse(sf);

    boolean found = false;
    for (Subfield s : sf) {
      if (found && 'a' == s.getCode()) {
        distributor.addResidence(s.getData());
      } else if ('c' == s.getCode()) {

        Matcher m = distribPattern.matcher(s.getData());
        if (m.find()) {
          distributor.addName(s.getData().replace(m.group(0), "").trim());
          found = true;
        }
      } else found = false;
    }

    return distributor;
  }

  public F3_ManifestationProductType add(F24_PublicationExpression publicationExpression) {
    this.addProperty(FRBROO.CLR6_should_carry, publicationExpression);
    return this;
  }
}
