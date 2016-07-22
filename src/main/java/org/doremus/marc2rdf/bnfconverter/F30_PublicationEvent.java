package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class F30_PublicationEvent {

  static Model modelF30 = ModelFactory.createDefaultModel();
  static URI uriF30 = null;

  public F30_PublicationEvent() throws URISyntaxException {
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
    F30.addProperty(modelF30.createProperty(frbroo + "R24_created"), modelF30.createResource(F24_PublicationExpression.uriF24.toString()));

    /**************************** réalisation d'une work *******************************/
    F30.addProperty(modelF30.createProperty(frbroo + "R19_created_a_realisation_of"), modelF30.createResource(F19_PublicationWork.uriF19.toString()));

    /**************************** Publication Event: édition princeps ******************/
    F30.addProperty(MUS.U4i_was_princeps_publication_of, modelF30.createResource(F14_IndividualWork.uriF14.toString()));

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
    MarcXmlReader reader = new MarcXmlReader(file, BNF2RDF.bnfXmlHandlerBuilder);
    while (reader.hasNext()) { // Parcourir le fichier MARCXML
      Record s = reader.next();
      for (int i = 0; i < s.dataFields.size(); i++) {
        if (s.dataFields.get(i).getEtiq().equals("600")) {
          if (s.dataFields.get(i).isCode('a')) {
            String edition = s.dataFields.get(i).getSubfield('a').getData();
            if ((edition.contains("éd.")) || (edition.contains("édition"))) {
              buffer.append(edition);
            }
          }
        }
      }
    }
    return buffer.toString();
  }
}
