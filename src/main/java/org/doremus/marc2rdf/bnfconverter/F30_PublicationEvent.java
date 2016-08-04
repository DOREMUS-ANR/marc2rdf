package org.doremus.marc2rdf.bnfconverter;

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

public class F30_PublicationEvent {
  private final Record record;

  private Model modelF30 = ModelFactory.createDefaultModel();
  private final Resource F30;
  private URI uriF30;

  public F30_PublicationEvent(Record record) throws URISyntaxException {
    this.record = record;
    this.modelF30 = ModelFactory.createDefaultModel();
    this.uriF30 = ConstructURI.build("Publication_Event", "F30");

    F30 = modelF30.createResource(uriF30.toString());
    F30.addProperty(RDF.type, FRBROO.F30_Publication_Event);
  }

  public Model getModel() throws URISyntaxException {

    /**************************** Expression: 1ère publication *************************/
    String edition = getNote();
    if (edition != null) F30.addProperty(CIDOC.P3_has_note, edition);

    return modelF30;
  }

  public F30_PublicationEvent add(F24_PublicationExpression expression) {
    /**************************** création d'une expression de publication *************/
    F30.addProperty(FRBROO.R24_created, expression.asResource());
    return this;
  }

  public F30_PublicationEvent add(F19_PublicationWork work) {
    /**************************** création d'une expression de publication *************/
    F30.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    return this;
  }

  public Resource asResource() {
    return F30;
  }

  /***********************************
   * La note
   ***********************************/
  private String getNote() {
    for (DataField field : record.getDatafieldsByCode("600")) {
      if (!field.isCode('a')) continue;

      String edition = field.getSubfield('a').getData();
      if ((edition.contains("éd.")) || (edition.contains("édition"))) return edition;
    }
    return null;
  }
}
