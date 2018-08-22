package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F3_ManifestationProductType extends DoremusResource {
  private static final String CARRIER_DURATION_REGEX = "(\\d+) ([^(]+) \\((.+)\\)";
  private static final Pattern CARRIER_DURATION_PATTERN = Pattern.compile(CARRIER_DURATION_REGEX);
  private static final String PSPEED_REGEX = "\\d{2} t";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile("(\\d+(?:x\\d+)?) ?(.+)");

  public F3_ManifestationProductType(Record record) {
    super(record);
    this.setClass(FRBROO.F3_Manifestation_Product_Type);

    this.addProperty(MUS.CLU206_should_have_media_type, parseMediaType());


    // carrier type
    List<String> a051 = record.getDatafieldsByCode("051", 'a');
    for (String a : record.getDatafieldsByCode("050", 'a'))
      this.addProperty(MUS.CLU207_should_have_carrier_type, Carrier.fromField050a(a, a051));


    // distribution rights
    Artist distributor = parseDistributor();
    String time = parseDistributionTime();
    if (distributor != null || time != null) {
      String drUri = this.uri + "/distribution_rigth";
      DistributionRight dr = new DistributionRight(drUri, distributor, time);
      this.addProperty(CIDOC.P104_is_subject_to, dr);
    }

    // container
    for (String cont : record.getDatafieldsByCode("028", 'c')) {
      this.addProperty(MUS.CLU198_should_have_container, model.createResource()
        .addProperty(RDF.type, MUS.M171_Container)
        .addProperty(RDFS.label, cont.replaceAll("^conditionn[ée] dans", ""))
        .addProperty(CIDOC.P3_has_note, cont));
    }

    // carrier extent
    for (String ce : record.getDatafieldsByCode("280", 'a')) {
      Matcher m = CARRIER_DURATION_PATTERN.matcher(ce);
      if (!m.find()) continue;

      this.addProperty(MUS.U208_has_extent_of_carrier, m.group(1) + " " + m.group(2));

      for (String d : m.group(3).split(","))
        this.addProperty(MUS.CLU53_should_have_duration, Utils.duration2iso(d), XSDDatatype.XSDdayTimeDuration);
      this.addDimension(m.group(1), m.group(2));
    }

    // sys requirements
    for (DataField sr : record.getDatafieldsByCode("337")) {
      this.addProperty(MUS.U194_has_system_requirements, sr.getString('k') + " : " + sr.getString('a'));
    }

    ControlField control9 = record.getControlfieldByCode("009");
    if (control9 != null) parseAuxiliary(control9.getData());
  }


  public F3_ManifestationProductType(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F3_Manifestation_Product_Type);
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
    String x = control9.substring(8, 10);
    if (x.startsWith(" ") || x.startsWith("#"))
      note = "pas de restriction de communication";
    if (x.equals("11"))
      note = "communication soumise à restriction: communication sur accord de l’ayant - droit";
    if (x.equals("13"))
      note = "communication soumise à restriction: communication interdite pendant une période déterminée";
    if (x.equals("14"))
      note = "communication soumise à restriction: non communicable";

    if (note != null)
      this.addProperty(CIDOC.P104_is_subject_to, model.createResource(this.uri + "/communication_right")
        .addProperty(RDF.type, CRO.CommunicationRight)
        .addProperty(CIDOC.P3_has_note, note)
        .addProperty(RDFS.comment, note)
      );


    for (String field : record.getDatafieldsByCode(280, 'c')) {
      for (String ps : field.split(",")) {
        ps = ps.trim();
        switch (control9.charAt(14)) {
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
        if (s.getData().toLowerCase().startsWith("distrib."))
          found = true;
      } else found = false;
    }

    return null;
  }

  private Artist parseDistributor() {
    Artist distributor = Person.fromUnimarcField(record.getDatafieldByCode(721));
    if (distributor != null)
      distributor = CorporateBody.fromUnimarcField(record.getDatafieldByCode(731));

    if (distributor == null) return null;

    // search for residence
    DataField df = record.getDatafieldByCode(260);
    List<Subfield> sf = df.getSubfields();
    Collections.reverse(sf);


    boolean found = false;
    for (Subfield s : sf) {
      if (found && 'a' == s.getCode()) {
        distributor.addResidence(s.getData());
      } else if ('c' == s.getCode()) {
        if (s.getData().toLowerCase().startsWith("distrib.")) {
          distributor.addName(s.getData().replaceAll("^distrib\\.", "").trim());
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
