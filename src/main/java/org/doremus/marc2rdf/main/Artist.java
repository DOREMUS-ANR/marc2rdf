package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;

public abstract class Artist {
  protected final Model model;
  public Resource resource;
  protected String uri;

  protected Artist() {
    this.model = ModelFactory.createDefaultModel();
  }

  public Model getModel() {
    return model;
  }

  public abstract String getFullName();

  public Resource asResource() {
    return this.resource;
  }

  public void addProvenance(Resource intermarcRes, Resource provActivity) {
    this.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, PP2RDF.DOREMUS)
      .addProperty(PROV.wasDerivedFrom, intermarcRes)
      .addProperty(PROV.wasGeneratedBy, provActivity);
  }

  public String getUri() {
    return uri;
  }

  protected void setUri(String uri) {
    if (uri.equals(this.uri)) return;
    this.uri = uri;
    if (this.resource != null)
      this.resource = ResourceUtils.renameResource(this.resource, this.uri);
  }

  public void addResidence(String data) {
    if (data == null || data.isEmpty()) return;
    data = data.trim().replaceAll("^\\[(.+)]$", "$1").trim();
    E53_Place place = new E53_Place(data);
    this.resource.addProperty(CIDOC.P74_has_current_or_former_residence, place.asResource());
    this.model.add(place.getModel());
  }

  public abstract void addName(String name);

  public abstract boolean hasName(String value);

  protected abstract void interlink();


  public static Artist fromString(String txt) {
    if (txt == null) return null;

    Artist artist;
    String _txt = txt.toLowerCase();
    if (_txt.equals("l'eic") || _txt.startsWith("les ") || _txt.contains("philarmo") || _txt.contains
      ("orchestr") ||
      _txt.contains("ensemble") || _txt.contains("membre") || _txt.contains("trio") ||
      _txt.contains("quartet") || _txt.contains("quatuor") || _txt.contains("choeur")) {
      txt = txt.replaceAll("^(l['+]|l[ae]s? )", "").trim();

      artist = new CorporateBody(txt);
    } else artist = new Person(txt);

    artist.interlink();
    return artist;

  }

}
