package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class PF30_PublicationEvent {

  static Model modelF30;
  static URI uriF30;

  public PF30_PublicationEvent() throws URISyntaxException {
    this.modelF30 = ModelFactory.createDefaultModel();
    this.uriF30 = getURIF30();
  }

  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";

  /********************************************************************************************/
  public URI getURIF30() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriF30 = uri.getUUID("Publication_Event", "F30", uuid.get());
    return uriF30;
  }

  public Model getModel() throws URISyntaxException, FileNotFoundException {

    Resource F30 = modelF30.createResource(uriF30.toString());
    F30.addProperty(RDF.type, FRBROO.F30_Publication_Event);

    /**************************** création d'une expression de publication *************/
    F30.addProperty(modelF30.createProperty(frbroo + "R24_created"), modelF30.createResource(PF24_PublicationExpression.uriF24.toString()));

    /**************************** réalisation d'une work *******************************/
    F30.addProperty(modelF30.createProperty(frbroo + "R19_created_a_realisation_of"), modelF30.createResource(PF19_PublicationWork.uriF19.toString()));

    /**************************** Publication Event: édition princeps ******************/
    F30.addProperty(MUS.U4i_was_princeps_publication_of, modelF30.createResource(PF14_IndividualWork.uriF14.toString()));

    /**************************** Expression: 1ère publication *************************/
    F30.addProperty(modelF30.createProperty(cidoc + "P3_has_note"), getNote(Converter.getFile()));


    return modelF30;
  }
  /********************************************************************************************/

  /***********************************
   * La note
   ***********************************/
  public static String getNote(String xmlFile) throws FileNotFoundException {
    StringBuilder buffer = new StringBuilder();
    InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
    MarcXmlReader reader = new MarcXmlReader(file);
    String note = "";
    while (reader.hasNext()) { // Parcourir le fichier MARCXML
      Record s = reader.next();
      for (int i = 0; i < s.dataFields.size(); i++) {
        if (s.dataFields.get(i).getEtiq().equals("919")) {
          if (s.dataFields.get(i).isCode('a')) {
            note = s.dataFields.get(i).getSubfield('a').getData();
          }
        }
      }
    }
    if (note.contains(". Editeur")) {
      int index = note.indexOf(". Editeur");
      note = note.substring(index + 1, note.length());
      buffer.append(note);
    }
    if (note.contains(". Première édition")) {
      int index = note.indexOf(". Première édition");
      note = note.substring(index + 1, note.length());
      buffer.append(note);
    }
    if (note.contains(". 1ère édition")) {
      int index = note.indexOf(". 1ère édition");
      note = note.substring(index + 1, note.length());
      buffer.append(note);
    }
    if (note.contains(". Publication")) {
      int index = note.indexOf(". Publication");
      note = note.substring(index + 1, note.length());
      buffer.append(note);
    }
    if (note.contains(". Première publication")) {
      int index = note.indexOf(". Première publication");
      note = note.substring(index + 1, note.length());
      buffer.append(note);
    }
    if (note.contains(". 1ere publication")) {
      int index = note.indexOf(". 1ere publication");
      note = note.substring(index + 1, note.length());
      buffer.append(note);
    }
    return buffer.toString();
  }
}
