package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;
import java.util.List;

public class PF4_ManifestationSingleton extends DoremusResource {
  public PF4_ManifestationSingleton(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);

    this.resource
      .addProperty(RDF.type, FRBROO.F4_Manifestation_Singleton)
      .addProperty(DCTerms.identifier, identifier);

    record.getDatafieldsByCode(200, 'a')
      .forEach(this::addTitle);
  }

  private void addTitle(String title) {
    this.resource.addProperty(CIDOC.P102_has_title, title)
      .addProperty(RDFS.label, title);
  }


  public PF4_ManifestationSingleton add(List<PF22_SelfContainedExpression> playedWorks) {
    for (PF22_SelfContainedExpression f22 : playedWorks)
      this.add(f22);
    return this;
  }

  private PF4_ManifestationSingleton add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P128_carries, f22.asResource());
    return this;
  }
}
