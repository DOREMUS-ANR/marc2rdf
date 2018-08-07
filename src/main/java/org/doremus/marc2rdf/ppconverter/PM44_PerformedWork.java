package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.bnfconverter.F14_IndividualWork;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class PM44_PerformedWork extends DoremusResource {
  public PM44_PerformedWork(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, MUS.M44_Performed_Work);
  }

  public PM44_PerformedWork(Record record) {
    super(record);
    this.resource.addProperty(RDF.type, MUS.M44_Performed_Work);

    if (record.isType("UNI:62")) parseUni62();
    else if (record.isType("UNI:42")) parseUni42();
  }

  private void parseUni62() {
    this.resource.addProperty(MUS.U95_has_hierarchical_level, "part", "en");
    if (titleContains("mouvement"))
      this.resource.addProperty(MUS.U95_has_hierarchical_level, "movement", "en");
  }

  private void parseUni42() {
    if (!titleContains("extrait"))
      this.resource.addProperty(MUS.U95_has_hierarchical_level, "single work", "en");

    if (record.getDatafieldsByCode(449).size() != 1) return;
    String derivation = getDerivationType();
    if (derivation != null) {
      String targetId = record.getDatafieldsByCode(449, 3).get(0);
      this.asResource()
        .addProperty(FRBROO.R2_is_derivative_of, new F14_IndividualWork(targetId).asResource())
        .addProperty(MUS.U47_has_derivation_type, derivation);
    }
  }

  private String getDerivationType() {
    for (String s : record.getDatafieldsByCode(610, 'a')) {
      if (s.matches("(?i)(transcription|arrangement|orchestration)"))
        return s;
    }

    for (String s : record.getDatafieldsByCode(200, 'a')) {
      if (s.toLowerCase().startsWith("improvisation sur"))
        return "arrangement";
    }

    for (String code : "700, 701, 702, 710, 711, 712".split(", ")) {
      for (String s : record.getDatafieldsByCode(code, '4'))
        switch (s) {
          case "85":
            return "transcription";
          case "030":
            return "arrangement";
          case "m49":
            return "orchestration";
        }
    }

    return null;
  }


  private boolean titleContains(String word) {
    return record.getDatafieldsByCode(200, 'a').stream()
      .map(String::toLowerCase)
      .anyMatch(x -> x.contains(word));
  }

}
