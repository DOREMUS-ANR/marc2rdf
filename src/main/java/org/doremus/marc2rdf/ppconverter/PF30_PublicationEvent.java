package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PF30_PublicationEvent extends DoremusResource {
  private static final String noteRegex = "\\. ((Editeur|(Premi|1)\u00e8re \u00e9dition|Publication|(Premi|1)ere publication).+)";

  public PF30_PublicationEvent(String note, Record record, String identifier) throws URISyntaxException {
    super(record, identifier);

    this.resource.addProperty(RDF.type, FRBROO.F30_Publication_Event);
    this.resource.addProperty(CIDOC.P3_has_note, note);
  }

  public PF30_PublicationEvent add(PF24_PublicationExpression f24) {
    /**************************** création d'une expression de publication *************/
    this.resource.addProperty(FRBROO.R24_created, f24.asResource());
    return this;
  }

  public PF30_PublicationEvent add(PF19_PublicationWork f19) {
    /**************************** réalisation d'une work *******************************/
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, f19.asResource());
    return this;
  }

  public static String getEditionPrinceps(Record record) {
    if (!record.isType("UNI:100")) return null;

    // TODO Check if this info is also on 909
    DataField field = record.getDatafieldByCode("919");
    if (field == null || !field.isCode('a')) return null;

    String note = field.getSubfield('a').getData();
    Pattern p = Pattern.compile(noteRegex);
    Matcher m = p.matcher(note);

    if (m.find()) return m.group(1);
    else return null;
  }
}
