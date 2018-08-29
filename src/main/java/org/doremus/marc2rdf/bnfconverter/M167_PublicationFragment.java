package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class M167_PublicationFragment extends DoremusResource {
  public M167_PublicationFragment(String uri, DataField df) {
    super();
    regenerateResource(uri);
    this.setClass(MUS.M167_Publication_Expression_Fragment);

    String tsr = concatFields(df);
    String ts = concatFields(df, "aehi");
    String sr = concatFields(df, "fgj");

    System.out.println("-- " + ts);

    Resource titleRespStatement = model.createResource(this.uri + "/title_responsibility")
      .addProperty(RDF.type, MUS.M158_Title_and_Statement_of_Responsibility)
      .addProperty(RDFS.comment, tsr);
    this.addProperty(MUS.U220_has_title_and_statement_of_responsibility, titleRespStatement);
    Resource titleStatement = model.createResource(this.uri + "/title")
      .addProperty(RDF.type, MUS.M156_Title_Statement)
      .addProperty(RDFS.comment, ts);
    titleRespStatement.addProperty(CIDOC.P148_has_component, titleStatement);
    this.addProperty(MUS.U170_has_title_statement, titleStatement);
    this.addProperty(RDFS.label, ts);

    Resource respStatement = model.createResource(this.uri + "/responsibility")
      .addProperty(RDF.type, MUS.M157_Statement_of_Responsibility)
      .addProperty(RDFS.comment, sr);
    this.addProperty(MUS.U172_has_statement_of_responsibility_relating_to_title, respStatement);
    titleRespStatement.addProperty(CIDOC.P148_has_component, respStatement);

    for (String n : df.getStrings('n'))
      this.addProperty(MUS.U195_has_order_or_location_indication, n);
  }

  public static String concatFields(DataField df) {
    return concatFields(df, "aefghijln");
  }

  public static String concatFields(DataField df, String filter) {
    char previous = 'z';
    StringBuilder text = new StringBuilder();
    for (Subfield sf : df.getSubfields()) {
      String value = sf.getData().trim();

      if (!filter.contains(sf.getCode() + ""))
        continue;

      switch (sf.getCode()) {
        case 'a':
          text.append(value);
          break;
        case 'd':
          text.append(", ").append(value);
          break;
        case 'e':
          text.append(": ").append(value);
          break;
        case 'f':
          text.append(" / ").append(value);
          break;
        case 'g':
          text.append("; ").append(value);
          break;
        case 'h':
          text.append(". ").append(value);
          break;
        case 'i':
          text.append(previous == 'h' ? ", " : ". ");
          text.append(value);
          break;
        case 'j':
          if (text.length() != 0)
            text.append("fgj".contains("" + previous) ? "; " : " / ");
          text.append(value);
          break;
        case 'l':
          text.append("(").append(value).append(")");
          break;

      }
      previous = sf.getCode();

    }
    return text.toString();
  }
}
