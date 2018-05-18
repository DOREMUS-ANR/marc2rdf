package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class E7_Activity extends DoremusResource {

  public E7_Activity(String uri) {
    super();

    this.uri = uri;
    this.resource = model.createResource(this.uri)
      .addProperty(RDF.type, CIDOC.E7_Activity);
  }

  public E7_Activity(String uri, Resource carrier, Resource function) {
    this(uri);

    setCarrier(carrier);
    setFunction(function);
  }

  E7_Activity(String uri, Resource carrier, String function) {
    this(uri);
    setCarrier(carrier);
    setFunction(functionFromLabel(function));
  }

  E7_Activity(String uri, Artist carrier, String function) {
    this(uri, carrier.asResource(), function);
    this.model.add(carrier.getModel());
  }

  private void setCarrier(Resource carrier) {
    if (carrier != null)
      this.resource.addProperty(CIDOC.P14_carried_out_by, carrier);
  }

  private void setFunction(Resource function) {
    if (function != null)
      this.resource.addProperty(MUS.U31_had_function, function);
  }

  private Resource functionFromLabel(String label) {
    return model.createResource(this.uri + "/function")
      .addProperty(RDF.type, MUS.M31_Actor_Function)
      .addProperty(RDFS.label, label)
      .addProperty(CIDOC.P1_is_identified_by, label);
  }
}
