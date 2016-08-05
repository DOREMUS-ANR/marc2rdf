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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F31_Performance {
  private static final String performanceRegex = "([eé]xécution|représentation) (.+)?:";

  private Model model;
  private final Resource F31;
  private URI uriF31;
  private boolean isPremiere;

  public F31_Performance(String note) throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF31 = ConstructURI.build("Performance", "F31");

    //check if it is a Premiere
    Pattern p = Pattern.compile(performanceRegex);
    Matcher m = p.matcher(note);
    m.find();
    isPremiere = m.group(2) == null;

    F31 = model.createResource(uriF31.toString());
    F31.addProperty(RDF.type, FRBROO.F31_Performance);
    F31.addProperty(CIDOC.P3_has_note, note);
  }

  public Resource asResource() {
    return F31;
  }

  public F31_Performance add(F25_PerformancePlan plan) {
    /**************************** exécution du plan ******************************************/
    F31.addProperty(FRBROO.R25_performed, plan.asResource());
//    plan.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R25i_was_performed_by"), F31);
    return this;
  }

  public Model getModel() {
    return model;
  }

  public static List<String> getPerformances(Record record) {
    List<String> notes = new ArrayList<>();
    Pattern p = Pattern.compile(performanceRegex);


    for (DataField field : record.getDatafieldsByCode("600")) {
      if (!field.isCode('a')) continue;

      String note = field.getSubfield('a').getData();
      Matcher m = p.matcher(note);

      if (m.find())  notes.add(note);
    }
    return notes;
  }

  public boolean isPremiere() {
    return isPremiere;
  }
}
