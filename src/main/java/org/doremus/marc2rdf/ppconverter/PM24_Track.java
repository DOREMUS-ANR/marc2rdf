package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PM24_Track extends DoremusResource {

  public PM24_Track(String identifier) {
    super(identifier);

    this.resource.addProperty(RDF.type, MUS.M24_Track)
      .addProperty(DC.identifier, this.identifier);
  }

  public PM24_Track(Record record) {
    super(record);

    this.resource.addProperty(RDF.type, MUS.M24_Track)
      .addProperty(DC.identifier, this.identifier);

    this.resource
      .addProperty(MUS.U227_has_content_type, record.isType("UNI:44") ? "two-dimensional moving image" : "sounds", "en");

    String rdaType = PP2RDF.guessType(record);
    if (rdaType != null) this.resource.addProperty(MUS.U227_has_content_type, rdaType, "en");

    for (String title : getTitles())
      this.resource.addProperty(CIDOC.P102_has_title, title).addProperty(RDFS.label, title);

    String orderNum = getOrderNum();
    if (orderNum != null)
      this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNum));

    String duration = getDuration(this.record);
    if (duration != null)
      this.resource.addProperty(MUS.U53_has_duration, duration, XSDDatatype.XSDdayTimeDuration);
  }


  private List<String> getTitles() {
    return record.getDatafieldsByCode(200, 'a').stream()
      .map(String::trim)
      .collect(Collectors.toList());
  }

  private String getOrderNum() {
    int _l = record.isType("UNI:44") ? 2 : 5;
    return record.getDatafieldsByCode(856, 'g').stream()
      .filter(Objects::nonNull)
      .filter(f -> !f.isEmpty())
      .findFirst()
      .map(f -> String.valueOf(f.substring(f.length() - _l)))
      .orElse(null);
  }

  static String getDuration(Record record) {
    return record.getDatafieldsByCode(215, 'a').stream()
      .filter(Objects::nonNull)
      .filter(f -> !f.isEmpty())
      .filter(f -> f.matches(Utils.DURATION_REGEX))
      .findFirst().map(Utils::duration2iso)
      .orElse(null);
  }


  public PM24_Track add(PM43_PerformedExpression expression) {
    this.resource.addProperty(MUS.U51_is_partial_or_full_recording_of, expression.asResource());
    return this;
  }
}
