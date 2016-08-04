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
import java.util.ArrayList;
import java.util.List;

public class F31_AutrePerformance {
  private Model model;
  private final Resource F31;
  private URI uriF31;
  private Record record;


  public F31_AutrePerformance(Record record) throws URISyntaxException {
    this.record = record;

    this.model = ModelFactory.createDefaultModel();
    this.uriF31 = ConstructURI.build("Performance", "F31");

    F31 = model.createResource(uriF31.toString());
    F31.addProperty(RDF.type, FRBROO.F31_Performance);
  }

  public Model getModel() throws URISyntaxException {
    /**************************** Performance: 1ère exécution *******************************/
    // FIXME this should create different performances
    for (String note : getNote())
      F31.addProperty(CIDOC.P3_has_note, note);

    return model;
  }

  /***********************************
   * L'exécution
   ***********************************/
  private List<String> getNote() {
    List<String> notes = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("600")) {
      if (!field.isCode('a')) continue;

      String note = field.getSubfield('a').getData();
      String[] identifiers = {"exécution", "éxécution", "représentation"};

      for (String id : identifiers) {
        if (note.contains(id)) {
          String st = note.substring(note.lastIndexOf(id) + id.length());
          if (!st.startsWith(" :")) notes.add(note);
        }
      }
    }
    return notes;
  }

  public F31_AutrePerformance add(F25_AutrePerformancePlan plan) {
    /**************************** exécution du plan ******************************************/
    F31.addProperty(FRBROO.R25_performed, plan.asResource());
//    plan.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R25i_was_performed_by"), F31);//
    return this;
  }
}
