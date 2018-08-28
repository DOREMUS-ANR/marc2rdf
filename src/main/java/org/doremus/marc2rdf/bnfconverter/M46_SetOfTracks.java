package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.Objects;
import java.util.stream.Stream;

public class M46_SetOfTracks extends BIBDoremusResource {

  public M46_SetOfTracks(Record record) {
    super(record);

    this.setClass(MUS.M46_Set_of_Tracks);
    this.addProperty(DC.identifier, this.identifier)
      .addProperty(MUS.U227_has_content_type, "performed music", "en");

    parseTitleAndStatements(false);

    parseContext(record).peek(x -> this.addProperty(MUS.U65_has_geographical_context, x.getGeoContext()))
      .forEach(x -> this.addProperty(MUS.U64_has_cultural_context, x));

    F24_PublicationExpression.parseCategorization(record)
      .peek(r -> this.addProperty(MUS.U19_is_categorized_as, r))
      .forEach(r -> model.createResource(this.uri + "/assignment/" + r.getLocalName())
        .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
        .addProperty(CIDOC.P141_assigned, r)
        .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
      );
  }

  static Stream<M40_Context> parseContext(Record record) {
    return record.getDatafieldsByCode(143).stream()
      .map(M40_Context::fromField)
      .filter(Objects::nonNull);
  }

  public M46_SetOfTracks(Record record, DataField datafield, int i) {
    // Expression globale résultant d'un editing
    super(record.getIdentifier() + i);
    this.record = record;

    this.setClass(MUS.M46_Set_of_Tracks);
    this.addProperty(DC.identifier, this.identifier)
      .addProperty(MUS.U227_has_content_type, "performed music", "en");

    int _case = BIBRecordConverter.getCase(record);

    String field1 = record.getControlfieldByCode("001").getData();

    if (_case == 1) {
      parseDuration();
      parseISRCId();
      titleFromField(245, false).forEach(this::addTitle);
    }

    if (_case == 2) {
      // order number
      String num = field1.substring(13, 16);
      if (record.getLevel() == 2) num = field1.substring(16, 19);
      this.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(num));

      // title and statements
      parseTitleAndStatements(false);
      parseResponsibilities(false);
      parseCastStatement();

      // duration
      parseDuration();

      // id ISRC
      parseISRCId();
    }

    if (_case == 3) {
      // title
      parseTitleField(datafield, true, true)
        .forEach(title -> this.addProperty(CIDOC.P102_has_title, title).addProperty(RDFS.label, title));
    }
  }

  private void parseISRCId() {
    for (DataField df : record.getDatafieldsByCode("030")) {
      String current = null;
      for (Subfield sf : df.getSubfields()) {
        switch (sf.getCode()) {
          case 'a':
            if (current != null) this.addComplexIdentifier(current, "ISRC", null);
            current = sf.getData().trim();
            break;
          case 'b':
            //noinspection StringConcatenationInLoop
            current += " (" + sf.getData().trim() + ")";
        }
      }
      if (current != null) this.addComplexIdentifier(current, "ISRC", null);
    }
  }

  private void parseDuration() {
    record.getDatafieldsByCode(245, 't')
      .forEach(duration -> this.addProperty(MUS.U53_has_duration, Utils.duration2iso(duration)));
  }

  public M46_SetOfTracks add(M43_PerformedExpression exp) {
    this.addProperty(MUS.U51_is_partial_or_full_recording_of, exp);
    return this;
  }

}
