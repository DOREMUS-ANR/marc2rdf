package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PF30_PublicationEvent {
  private static final String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  private static final String noteRegex = "\\. ((Editeur|(Premi|1)\u00e8re \u00e9dition|Publication|(Premi|1)ere publication).+)";

  private Model modelF30;
  private final Resource F30;
  private URI uriF30;
  private final Record record;

  public PF30_PublicationEvent(Record record) throws URISyntaxException {
    this.record = record;
    this.modelF30 = ModelFactory.createDefaultModel();
    this.uriF30 = ConstructURI.build("Publication_Event", "F30");

    F30 = modelF30.createResource(uriF30.toString());
    F30.addProperty(RDF.type, FRBROO.F30_Publication_Event);

    /**************************** Expression: 1ère publication *************************/
    for (String note : getNote()) {
      F30.addProperty(modelF30.createProperty(cidoc + "P3_has_note"), note);
    }
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

  /***********************************
   * La note
   ***********************************/
  private List<String> getNote() {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> results = new ArrayList<>();
    Pattern p = Pattern.compile(noteRegex);

    for (DataField field : record.getDatafieldsByCode("919")) {
      if (!field.isCode('a')) continue;

      String note = field.getSubfield('a').getData();
      Matcher m = p.matcher(note);
      if (m.find()) results.add(m.group(1));
    }
    return results;
  }

  public Resource asResource() {
    return F30;
  }
}
