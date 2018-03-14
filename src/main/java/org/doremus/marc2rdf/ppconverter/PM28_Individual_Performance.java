package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class PM28_Individual_Performance extends DoremusResource {
  public PM28_Individual_Performance(URI baseUri, int id) throws URISyntaxException {
    super();
    this.uri = new URI(baseUri + "/" + id);

    this.resource = model.createResource(this.uri.toString())
      .addProperty(RDF.type, MUS.M28_Individual_Performance);
  }

  public void set(Artist actor, String mop, String function, String character, String note, boolean isPrincipal) {
    this.set(actor, model.createLiteral(mop), function, character, note, isPrincipal);
  }

  public void set(Artist actor, RDFNode mop, String function, String character, String note, boolean isPrincipal) {
    this.setActor(actor);
    this.setMop(mop);
    this.setFunctionByCode(function);
    this.setCharacter(character);
    this.addNote(note);
    if (isPrincipal) this.setAsPrincipal();
  }

  public void setMop(RDFNode mop) {
    if (mop != null)
      this.resource.addProperty(MUS.U1_used_medium_of_performance, mop);
  }

  public void setActor(Artist actor) {
    this.resource.addProperty(CIDOC.P14_carried_out_by, actor.asResource());
    this.model.add(actor.getModel());
  }

  public void setCharacter(String character) {
    if (character == null || character.isEmpty()) return;

    String charUri = this.uri + "/character";
    boolean startsUppercase = Character.isUpperCase(character.codePointAt(0));
    String[] parts = character.split(",", startsUppercase ? 2 : 1);
    String name = parts[0].trim();

    Resource charResource = model.createResource(charUri)
      .addProperty(RDF.type, FRBROO.F38_Character)
      .addProperty(RDFS.label, name);

    if (parts.length > 1) {
      String text = parts[1].trim().replace("+", "'");
      charResource.addProperty(RDFS.comment, text, "fr")
        .addProperty(CIDOC.P3_has_note, text, "fr");
    }

    this.resource.addProperty(MUS.U27_performed_character, charResource);
  }

  public void setFunctionByCode(String code) {
    if (code != null)
      this.setFunction(PP2RDF.FUNCTION_MAP.get(code));
  }

  public void setFunction(String label) {
    Resource function = model.createProperty(this.uri + "/function")
      .addProperty(RDF.type, MUS.M31_Actor_Function)
      .addProperty(RDFS.label, label, "fr")
      .addProperty(CIDOC.P1_is_identified_by, label, "fr");
    this.resource.addProperty(MUS.U31_had_function, function);
  }

  public void setAsPrincipal() {
    String label = "Artiste principal";
    Resource status = model.createProperty(this.uri + "/status")
      .addProperty(RDF.type, MUS.M49_Performer_Status)
      .addProperty(RDFS.label, label, "fr")
      .addProperty(CIDOC.P1_is_identified_by, label, "fr");
    this.resource.addProperty(MUS.U81_had_performer_status, status);
  }
}
