package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
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
      .addProperty(PROV.wasAttributedTo, model.createResource(PP2RDF.doremusURI))
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
}
