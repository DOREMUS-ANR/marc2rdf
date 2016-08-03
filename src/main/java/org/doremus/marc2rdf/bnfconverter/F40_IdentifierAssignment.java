package org.doremus.marc2rdf.bnfconverter;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/
public class F40_IdentifierAssignment {
  private static final String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  private Model model = ModelFactory.createDefaultModel();
  private final Record record;
  private URI uriF40;
  private Resource F40;

  public F40_IdentifierAssignment(Record record) throws URISyntaxException {
    this.record = record;
    this.model = ModelFactory.createDefaultModel();
    this.uriF40 = ConstructURI.build("Identifier_Assignment", "F40");

    F40 = model.createResource(uriF40.toString());
    F40.addProperty(RDF.type, FRBROO.F40_Identifier_Assignment);
  }

  public Model getModel() {
    /**************************** Schéma général : agence ***********************************/
    URI agency = getBiblioAgency();
    if (agency != null)
      F40.addProperty(model.createProperty(cidoc + "P14_carried_out_by"), model.createResource(agency.toString()));

    /**************************** Schéma général : règles ***********************************/
    F40.addProperty(FRBROO.R52_used_rule, "NF Z 44-079 (Novembre 1993) - Documentation - Catalogage- Forme et structure des vedettes titres musicaux");

    return model;
  }

  public F40_IdentifierAssignment add(F22_SelfContainedExpression expression) {
    /**************************** Schéma général : Attribution ******************************/
    F40.addProperty(FRBROO.R45_assigned_to, expression.asResource());
    expression.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);

    return this;
  }

  public F40_IdentifierAssignment add(F14_IndividualWork work) {
    /**************************** Work: was assigned by *************************************/
    F40.addProperty(FRBROO.R45_assigned_to, work.asResource());
    work.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);

    return this;
  }

  public F40_IdentifierAssignment add(F15_ComplexWork work) {
    /**************************** Work: was assigned by *************************************/
    F40.addProperty(FRBROO.R45_assigned_to, work.asResource());
    work.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);

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
