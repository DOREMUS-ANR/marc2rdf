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


public class PF31_Performance {
  private static final String noteRegex = "\\. ((Editeur|(Premi|1)\u00e8re \u00e9dition|Publication|(Premi|1)ere publication|Cr\u00e9ation fran\u00e7aise).+)";
  private static final String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";

  private Model model;
  private final Record record;
  private final Resource F31;
  private URI uriF31;


  public PF31_Performance(Record record) throws URISyntaxException {
    this.record = record;
    this.model = ModelFactory.createDefaultModel();
    this.uriF31 = ConstructURI.build("Performance", "F31");

    F31 = model.createResource(uriF31.toString());
    F31.addProperty(RDF.type, FRBROO.F31_Performance);

    /**************************** Performance: 1ère exécution *******************************/
    for (String note : getNote())
      F31.addProperty(model.createProperty(cidoc + "P3_has_note"), note);
  }

  public Model getModel() {
    return model;
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
      if (m.find()) note = note.replaceAll(m.group(1), "").trim();

      if (!note.isEmpty()) results.add(note);
    }
    return results;
  }


  public Resource asResource() {
    return F31;
  }

  public PF31_Performance add(PF25_PerformancePlan f25) {
    /**************************** exécution du plan ******************************************/
    F31.addProperty(FRBROO.R25_performed, F31);
//    f25.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R25i_was_performed_by"), F31);
    return this;
  }
}
