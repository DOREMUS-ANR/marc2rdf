package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public class F30_PublicationEvent extends DoremusResource {
  public F30_PublicationEvent(String edition, Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);
    this.model = ModelFactory.createDefaultModel();

    this.uri = ConstructURI.build("bnf", "F30", "Publication_Event", this.identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F30_Publication_Event);
    this.resource.addProperty(CIDOC.P3_has_note, edition);
  }

  public F30_PublicationEvent add(F24_PublicationExpression expression) {
    /**************************** création d'une expression de publication *************/
    this.resource.addProperty(FRBROO.R24_created, expression.asResource());
    return this;
  }

  public F30_PublicationEvent add(F19_PublicationWork work) {
    /**************************** création d'une expression de publication *************/
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    return this;
  }

  public static String getEditionPrinceps(Record record) {
    // search edition priceps in the record
    for (String edition : record.getDatafieldsByCode("600", 'a')) {
      if ((edition.contains("éd.")) || (edition.contains("édition"))) return edition;
    }
    return null;
  }
}
