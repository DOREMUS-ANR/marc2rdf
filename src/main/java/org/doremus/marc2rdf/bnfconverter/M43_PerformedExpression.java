package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.Objects;

public class M43_PerformedExpression extends BIBDoremusResource {
  public M43_PerformedExpression(String identifier) {
    super(identifier);
    this.setClass(MUS.M43_Performed_Expression);
    this.addProperty(MUS.U227_has_content_type, "performed music", "en");
  }


  public M43_PerformedExpression(Record record, String identifier, DataField datafield) {
    this(identifier);
    this.record = record;

    titleFromField(245, false).forEach(this::addTitle);
    int _case = BIBRecordConverter.getCase(record);

    if (_case < 3) {
      titleFromField(245, false).forEach(this::addTitle);
      titleFromField(247, false).forEach(this::addVariantTitle);
      titleFromField(750, false).forEach(this::addVariantTitle);
      titleFromField(751, false).forEach(this::addVariantTitle);
      record.getDatafieldsByCode(300, 'a').forEach(this::addNote);
    }

    if (_case != 2)
      M46_SetOfTracks.parseContext(record)
        .peek(x -> this.addProperty(MUS.U65_has_geographical_context, x.getGeoContext()))
        .forEach(x -> this.addProperty(MUS.U64_has_cultural_context, x));

    if (_case == 3)
      parseTitleField(datafield, true, true).forEach(this::addTitle);


    // language
    this.addProperty(CIDOC.P72_has_language, Utils.toISO2Lang(parseLanguage()));


    // categorization
    F24_PublicationExpression.parseCategorization(record)
      .peek(r -> this.addProperty(MUS.U19_is_categorized_as, r))
      .forEach(r -> model.createResource(this.uri + "/assignment/" + r.getLocalName())
        .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
        .addProperty(CIDOC.P141_assigned, r)
        .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
      );

    // genre
    record.getDatafieldsByCode(143).stream()
      .map(M5_Genre::fromField)
      .filter(Objects::nonNull)
      .peek(x -> this.addProperty(MUS.U12_has_genre, x))
      .peek(x -> this.addProperty(MUS.U12_has_genre, x.getSubGenre()))
      .flatMap(x -> x.getContext().stream())
      .forEach(ctx -> this.addProperty(MUS.U63_has_religious_context, ctx));
  }

  private String parseLanguage() {
    ControlField field8 = record.getControlfieldByCode("008");
    if (field8 == null) return null;

    String langCode = field8.getData().substring(31, 34);
    if (langCode.equals("zxx") || langCode.equals("und")) return null;
    if (langCode.matches("m(mm|ul)"))
      langCode = record.getDatafieldByCode("041", 'a');
    return langCode;

  }

  @Override
  public void addTitle(Literal title) {
    this.addProperty(CIDOC.P102_has_title, title);
    this.addProperty(RDFS.label, title);
  }

  public void addVariantTitle(Literal title) {
    this.addProperty(MUS.U168_has_parallel_title, title);
  }


  public M43_PerformedExpression add(F22_SelfContainedExpression f22) {
    this.addProperty(MUS.U54_is_performed_expression_of, f22);
    return this;
  }
}
