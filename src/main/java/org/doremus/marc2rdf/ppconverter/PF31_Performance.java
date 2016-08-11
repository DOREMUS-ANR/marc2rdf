package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PF31_Performance extends DoremusResource {
  private static final String noteRegex = "\\. ((Editeur|(Premi|1)\u00e8re \u00e9dition|Publication|(Premi|1)ere publication).+)";
  private static final String frenchCreationRegex = "(Cr\u00e9ation fran\u00e7aise.+)";

  private boolean isPremiere;

  public PF31_Performance(String note, Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);

    //check if it is a Premiere
    Pattern p = Pattern.compile(frenchCreationRegex);
    Matcher m = p.matcher(note);
    isPremiere = !m.find();
    char flag = isPremiere ? 'p' : 'f';

    this.identifier = record.getIdentifier() + flag;
    this.uri = ConstructURI.build("philharmonie", "F31", "Performance", this.identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F31_Performance);
    this.resource.addProperty(CIDOC.P3_has_note, note);
  }

  public PF31_Performance add(PF25_PerformancePlan f25) {
    /**************************** exécution du plan ******************************************/
    this.resource.addProperty(FRBROO.R25_performed, f25.asResource());
//    f25.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R25i_was_performed_by"), F31);
    return this;
  }

  public static List<String> getPerformances(Record record) {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> results = new ArrayList<>();
    Pattern p = Pattern.compile(noteRegex);
    Pattern p2 = Pattern.compile(frenchCreationRegex);

    for (DataField field : record.getDatafieldsByCode("919")) {
      if (!field.isCode('a')) continue;

      String note = field.getSubfield('a').getData().trim();

      Matcher m = p.matcher(note);
      if (m.find()) note = note.replaceAll(m.group(1), "").trim();

      m = p2.matcher(note);
      if (m.find()) { // Creation francaise
        results.add(m.group(1).trim());
        note = note.replace(m.group(1), "").trim();
      }

      //premiere
      if (!note.isEmpty()) results.add(note);
    }
    return results;

  }

  public boolean isPremiere() {
    return isPremiere;
  }
}
