package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/
public class PF40_IdentifierAssignment extends DoremusResource {
  public PF40_IdentifierAssignment(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F40_Identifier_Assignment);

    /**************************** Schéma général : agence ***********************************/
    // TODO Create the resource of Philarmonie ? or http://isni.org/isni/0000000121834950 ?
    this.resource.addProperty(CIDOC.P14_carried_out_by, model.createResource("http://data.doremus.org/Philharmonie_de_Paris"));

//    /**************************** Work: identifier assignment (Identifier) ******************/
//    this.resource.addProperty(FRBROO.R46_assigned, record.getIdentifier()); // L'identifiant de l'oeuvre
//
//    //TODO check this (double P2)
//    /**************************** Work: identifier assignment (type) ************************/
//    this.resource.addProperty(CIDOC.P2_has_type, "N° de notice"); // "N° de notice" par défaut pour toutes les notices

    /**************************** Work: was assigned by *************************************/
    String typeTitle = getTypeTitle();
    if (typeTitle != null)
      this.resource.addProperty(CIDOC.P2_has_type, model.createLiteral(getTypeTitle(), "fr"));

    /**************************** Schéma général : règles ***********************************/
    String rule = getRule();
    if (rule != null) this.resource.addProperty(FRBROO.R52_used_rule, rule);

  }

  public PF40_IdentifierAssignment add(PF22_SelfContainedExpression f22) {
    /**************************** Schéma général : Attribution ******************************/
    this.resource.addProperty(FRBROO.R45_assigned_to, f22.asResource());
//    f22.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public PF40_IdentifierAssignment add(PF14_IndividualWork f14) {
    /**************************** Work: was assigned by *************************************/
    this.resource.addProperty(FRBROO.R45_assigned_to, f14.asResource());
//   f14.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public PF40_IdentifierAssignment add(PF15_ComplexWork f15) {
    /**************************** Work: was assigned by *************************************/
    this.resource.addProperty(FRBROO.R45_assigned_to, f15.asResource());
//    f15.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R45i_was_assigned_by"), F40);
    return this;
  }

  public PF40_IdentifierAssignment add(PF50_ControlledAccessPoint accessPoint) {
    /**************************** Work: was assigned by *************************************/
    this.resource.addProperty(FRBROO.R46_assigned, accessPoint.asResource());
    return this;
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
