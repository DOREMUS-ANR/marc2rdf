package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class M24_Track extends DoremusResource {

  public M24_Track(String identifier) {
    super(identifier);
    this.setClass(MUS.M24_Track);
  }

  public M24_Track(String identifier, DataField df) {
    this(identifier);

    this.addProperty(MUS.U227_has_content_type, "sounds", "en");
    this.addProperty(MUS.U227_has_content_type, "performed music", "en");

    String title = M167_PublicationFragment.concatFields(df);
    this.addProperty(CIDOC.P102_has_title, title).addProperty(RDFS.label, title);

    this.addProperty(MUS.U53_has_duration, parseDuration(df), XSDDatatype.XSDdayTimeDuration);
  }

  private String parseDuration(DataField df) {
    String durationString = df.getString('l');
    if (durationString == null) return null;
    return Utils.duration2iso(durationString);
  }
}
