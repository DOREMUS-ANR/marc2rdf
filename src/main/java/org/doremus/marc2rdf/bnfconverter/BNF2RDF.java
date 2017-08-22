package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class BNF2RDF {
  public static final MarcXmlHandler.MarcXmlHandlerBuilder bnfXmlHandlerBuilder = new MarcXmlHandler.MarcXmlHandlerBuilder();
  public static final String organizationURI = "http://data.doremus.org/organization/BnF";
  /******
   * Constants that represents the kind of record
   ******/
  private static final char TUM = 'u';
  private static final char PERSON = 'p';
  private static final char ORGANIZATION = 'c'; //collectivité

  public static Model convert(String file) throws URISyntaxException, FileNotFoundException {

    /************* Creer un modele vide **************************/
    Model model = ModelFactory.createDefaultModel();

    MarcXmlReader reader = new MarcXmlReader(file, BNF2RDF.bnfXmlHandlerBuilder);

    if (reader.getRecords() == null || reader.getRecords().size() == 0)
      System.out.println("Exception occurred parsing file " + file);

    boolean somethingHasBeenConverted = false;

    for (Record r : reader.getRecords()) {
      // TODO implement mapping for notice of type BIB
      if (r.type == null || !r.isType("Authority")) return null;

      ControlField leader = r.getControlfieldByCode("leader");
      if (leader == null) continue;

      char code = leader.getData().charAt(9);

      switch (code) {
        case TUM:
          somethingHasBeenConverted = true;
          new RecordConverter(r, model);
          break;
        case PERSON:
          somethingHasBeenConverted = true;
          new ArtistConverter(r, model);
          break;
        case ORGANIZATION:
          //TODO
          break;
        default:
          System.out.println("Not recognized kind of Authority record: " + code);
      }
    }

    if (!somethingHasBeenConverted) return null;


    // Remove empty nodes
    String query = "delete where {?x ?p \"\" }";
    UpdateAction.parseExecute(query, model);

    return model;
  }
}

