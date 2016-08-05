package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PF30_PublicationEvent {
  private static final String noteRegex = "\\. ((Editeur|(Premi|1)\u00e8re \u00e9dition|Publication|(Premi|1)ere publication).+)";

  private Model modelF30;
  private final Resource F30;
  private URI uriF30;

  public PF30_PublicationEvent(String note) throws URISyntaxException {
    this.modelF30 = ModelFactory.createDefaultModel();
    this.uriF30 = ConstructURI.build("Publication_Event", "F30");

    F30 = modelF30.createResource(uriF30.toString());
    F30.addProperty(RDF.type, FRBROO.F30_Publication_Event);
    F30.addProperty(CIDOC.P3_has_note, note);
  }

  public Model getModel() throws URISyntaxException {
    return modelF30;
  }

  public PF30_PublicationEvent add(PF24_PublicationExpression f24) {
    /**************************** création d'une expression de publication *************/
    F30.addProperty(FRBROO.R24_created, modelF30.createResource(f24.asResource()));
    return this;
  }

  public PF30_PublicationEvent add(PF19_PublicationWork f19) {
    /**************************** réalisation d'une work *******************************/
    F30.addProperty(FRBROO.R19_created_a_realisation_of, f19.asResource());
    return this;
  }

  public Resource asResource() {
    return F30;
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
