package org.doremus.marc2rdf.bnfconverter;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/
public class F40_IdentifierAssignment extends DoremusResource {
  public F40_IdentifierAssignment(Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);

    this.uri = ConstructURI.build("bnf", "F40", "Identifier_Assignment", this.identifier);

    this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F40_Identifier_Assignment);

    /**************************** Schéma général : agence ***********************************/
    URI agency = getBiblioAgency();
    if (agency != null)
      this.resource.addProperty(CIDOC.P14_carried_out_by, model.createResource(agency.toString()));

    /**************************** Schéma général : règles ***********************************/
    this.resource.addProperty(FRBROO.R52_used_rule, "NF Z 44-079 (Novembre 1993) - Documentation - Catalogage- Forme et structure des vedettes titres musicaux");
  }

  public F40_IdentifierAssignment add(F22_SelfContainedExpression expression) {
    /**************************** Schéma général : Attribution ******************************/
    this.resource.addProperty(FRBROO.R45_assigned_to, expression.asResource());
//    expression.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public F40_IdentifierAssignment add(F14_IndividualWork work) {
    /**************************** Work: was assigned by *************************************/
    this.resource.addProperty(FRBROO.R45_assigned_to, work.asResource());
//    work.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public F40_IdentifierAssignment add(F15_ComplexWork work) {
    /**************************** Work: was assigned by *************************************/
    this.resource.addProperty(FRBROO.R45_assigned_to, work.asResource());
//    work.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }


  /********
   * L'agence bibliographique qui a attribué l'identifiant
   *****************/
  private URI getBiblioAgency() {
    try {
      for (ControlField field : record.getControlfieldsByCode("001")) {
        String agency = field.getData().substring(0, 5);

        if (agency.equals("FRBNF")) {
          URIBuilder builder = new URIBuilder().setPath("http://isni.org/isni/0000000121751303");
          return builder.build();
        }
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**************************
   * 4. Work: identifier assignment (Identifier)
   *********************/
  public String getIdentifier() {
    //TODO move (maybe to Individual/ComplexWork)
    ControlField field = record.getControlfieldByCode("003");
    if (field == null) return null;

    return field.getData();
  }
}
