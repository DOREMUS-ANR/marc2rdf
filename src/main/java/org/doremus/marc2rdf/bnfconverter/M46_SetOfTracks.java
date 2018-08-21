package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PM43_PerformedExpression;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.Objects;

public class M46_SetOfTracks extends DoremusResource {
  private boolean recordAsATrack = false;

  public M46_SetOfTracks(String identifier) {
    super(identifier);
    this.setClass(MUS.M46_Set_of_Tracks);
    this.addProperty(DC.identifier, this.identifier);
  }

  public M46_SetOfTracks(Record record) {
    super(record);

    this.setClass(MUS.M46_Set_of_Tracks);
    this.addProperty(DC.identifier, this.identifier)
      .addProperty(MUS.U227_has_content_type, "performed music", "en");

    record.getDatafieldsByCode(143).stream()
      .map(M40_Context::fromField)
      .filter(Objects::nonNull)
      .peek(x -> this.addProperty(MUS.U65_has_geographical_context, x.getGeoContext()))
      .forEach(x -> this.addProperty(MUS.U64_has_cultural_context, x));

    F24_PublicationExpression.parseCategorization(record)
      .peek(r -> this.addProperty(MUS.U19_is_categorized_as, r))
      .forEach(r -> model.createResource(this.uri + "/assignment/" + r.getLocalName())
        .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
        .addProperty(CIDOC.P141_assigned, r)
        .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
      );
  }

  public M46_SetOfTracks add(PM43_PerformedExpression exp) {
    this.asResource().addProperty(MUS.U51_is_partial_or_full_recording_of, exp.asResource());
    return this;
  }

  public void addTitle(Literal title) {
    this.addProperty(MUS.U167_has_title_proper, title);
    this.addProperty(RDFS.label, title);
  }

  public void addParallelTitle(Literal title) {
    this.addProperty(MUS.U168_has_parallel_title, title);
  }
}
