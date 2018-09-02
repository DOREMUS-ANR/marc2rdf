package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.Schema;

public abstract class AbstractRight extends DoremusResource {
  private final Artist holder;

  public AbstractRight(String uri, Artist holder, String year) {
    super();
    this.regenerateResource(uri);

    this.holder = holder;
    this.addProperty(CIDOC.P75i_is_possessed_by, holder);

    if (year != null && !year.isEmpty()) {
      TimeSpan ts = new TimeSpan(year);

      ts.setUri(this.uri + "/interval");
      if(ts.asResource() ==null) return;
      this.model.add(ts.getModel());

      this.addProperty(Schema.startTime, year, XSDDatatype.XSDgYear)
        .addProperty(CIDOC.P141i_was_assigned_by, model.createResource(this.uri + "/assignment")
          .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
          .addProperty(CIDOC.P4_has_time_span, ts.asResource()));
    }

  }

  public Artist getHolder() {
    return this.holder;
  }
}
