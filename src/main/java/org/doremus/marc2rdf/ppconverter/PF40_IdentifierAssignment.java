package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/
public class PF40_IdentifierAssignment {
  private static final String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";

  private Model model;
  private URI uriF40;
  private final Resource F40;
  private final Record record;

  public PF40_IdentifierAssignment(Record record) throws URISyntaxException {
    this.record = record;
    this.model = ModelFactory.createDefaultModel();
    this.uriF40 = ConstructURI.build("Identifier_Assignment", "F40");

    F40 = model.createResource(uriF40.toString());
    F40.addProperty(RDF.type, FRBROO.F40_Identifier_Assignment);

    compute();
  }

  private void compute() {
    /**************************** Schéma général : agence ***********************************/
    // TODO Create the resource of Philarmonie ?
    F40.addProperty(model.createProperty(cidoc + "P14_carried_out_by"), model.createResource("http://data.doremus.org/Philharmonie_de_Paris"));

    /**************************** Work: identifier assignment (Identifier) ******************/
    F40.addProperty(FRBROO.R46_assigned, getIdentifier()); // L'identifiant de l'oeuvre


    //TODO check this (double P2)
    /**************************** Work: identifier assignment (type) ************************/
    F40.addProperty(model.createProperty(cidoc + "P2_has_Type"), "N° de notice"); // "N° de notice" par défaut pour toutes les notices

    /**************************** Work: was assigned by *************************************/
    String typeTitle = getTypeTitle();
    if (typeTitle != null)
      F40.addProperty(model.createProperty(cidoc + "P2_has_Type"), model.createLiteral(getTypeTitle(), "fr"));

    /**************************** Schéma général : règles ***********************************/
    String rule = getRule();
    if (rule != null) F40.addProperty(FRBROO.R52_used_rule, rule);

  }

  public Model getModel() throws URISyntaxException {
    return model;
  }

  public PF40_IdentifierAssignment add(PF22_SelfContainedExpression f22) {
    /**************************** Schéma général : Attribution ******************************/
    F40.addProperty(FRBROO.R45_assigned_to, f22.asResource());
//    f22.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public PF40_IdentifierAssignment add(PF14_IndividualWork f14) {
    /**************************** Work: was assigned by *************************************/
    F40.addProperty(FRBROO.R45_assigned_to, f14.asResource());
//   f14.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public PF40_IdentifierAssignment add(PF15_ComplexWork f15) {
    /**************************** Work: was assigned by *************************************/
    F40.addProperty(FRBROO.R45_assigned_to, f15.asResource());
//    f15.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public PF40_IdentifierAssignment add(PF50_ControlledAccessPoint accessPoint) {
    /**************************** Work: was assigned by *************************************/
    F40.addProperty(FRBROO.R46_assigned, accessPoint.asResource());
    return this;
  }


  /**************************
   * 4. Work: identifier assignment (Identifier)
   *********************/
  private String getIdentifier() {
    //TODO move (maybe to Individual/ComplexWork)

    return record.getIdentifier();
  }

  /**************************
   * Schéma général : règles
   **************************************/
  private String getRule() {
    if (record.isType("UNI:100")) return "Usages de la Philharmonie de Paris";
    if (record.isType("AIC:14"))
      return "NF Z 44-079 (Novembre 1993) - Documentation - Catalogage- Forme et structure des vedettes titres musicaux";
    return null;
  }

  /**************************
   * Type du titre
   ************************************************/
  private String getTypeTitle() {
    if (record.isType("UNI:100")) return "Point d'accès privilégié";
    if (record.isType("AIC:14")) return "Variante";
    return null;
  }

}
