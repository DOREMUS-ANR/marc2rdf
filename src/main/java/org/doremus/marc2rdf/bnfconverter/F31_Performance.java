package org.doremus.marc2rdf.bnfconverter;

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

public class F31_Performance extends DoremusResource {
  private static final String performanceRegex = "([eé]xécution|représentation) (.+)?:";

  private boolean isPremiere;

  public F31_Performance(String note, Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);

    //check if it is a Premiere
    Pattern p = Pattern.compile(performanceRegex);
    Matcher m = p.matcher(note);
    m.find();
    isPremiere = m.group(2) == null;
    char flag = isPremiere? 'p':'f';

    this.identifier = record.getIdentifier() + flag;
    this.uri = ConstructURI.build("bnf", "F31", "Performance", this.identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F31_Performance);
    this.resource.addProperty(CIDOC.P3_has_note, note);
  }


  public F31_Performance add(F25_PerformancePlan plan) {
    /**************************** exécution du plan ******************************************/
    this.resource.addProperty(FRBROO.R25_performed, plan.asResource());
//    plan.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R25i_was_performed_by"), this.resource);
    return this;
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
