package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.CRO;
import org.doremus.ontology.Schema;

public class DistributionRight extends DoremusResource {


  public DistributionRight(String uri, Artist distributor, String year) {
    super();
    this.regenerateResource(uri);
    this.setClass(CRO.DistributionRight);

    this.addProperty(CIDOC.P75i_is_possessed_by, distributor);

    if (year != null && !year.isEmpty()) {
      TimeSpan ts = new TimeSpan(year);
      ts.setUri(this.uri + "/interval");
      this.model.add(ts.getModel());

      this.addProperty(Schema.startTime, year)
        .addProperty(CIDOC.P141i_was_assigned_by, model.createResource(this.uri + "/assignment")
          .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
          .addProperty(CIDOC.P4_has_time_span, ts.asResource()));
    }

  }
}
