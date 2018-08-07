package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

public class PM43_PerformedExpression extends DoremusResource {
  public PM43_PerformedExpression(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, MUS.M43_Performed_Expression);
  }

  public PM43_PerformedExpression(Record record) {
    super(record);
    this.resource.addProperty(RDF.type, MUS.M43_Performed_Expression);

    String rdaType = PP2RDF.guessType(record);
    if (rdaType != null)
      this.resource.addProperty(MUS.U227_has_content_type, rdaType, "en");

    // title
    for (String title : record.getDatafieldsByCode(200, 'a'))
      this.resource.addProperty(RDFS.label, title.trim())
        .addProperty(CIDOC.P102_has_title, title.trim());

    // subtitle
    for (String title : record.getDatafieldsByCode(200, 'e'))
      this.resource.addProperty(MUS.U67_has_subtitle, title.trim());

    // categorization
    PF31_Performance.parseCategorization(this);

    // genre
    for (String s : record.getDatafieldsByCode(610, 'a')) {
      Resource genre = VocabularyManager.searchInCategory(s, null, "genre", false);
      if (genre != null) this.resource.addProperty(MUS.U12_has_genre, genre);
    }

    // lang
    for (String lang : record.getDatafieldsByCode(101, 'a')) {
      String code = Utils.toISO2Lang(lang);
      if (code != null) this.resource.addProperty(CIDOC.P72_has_language, code);
    }

    // generic notes
    PF31_Performance.getGenericNotes(record).forEach(this::addNote);

  }
}
