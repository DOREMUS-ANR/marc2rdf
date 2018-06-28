package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class PM46_SetOfTracks extends DoremusResource {

  public PM46_SetOfTracks(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, MUS.M46_Set_of_Tracks)
      .addProperty(DC.identifier, this.identifier)
      .addProperty(MUS.U227_has_content_type, "performed music", "en")
      .addProperty(MUS.U227_has_content_type, "two-dimensional moving image", "en");

    for (String trackCode : getTracks()) {
      PM24_Track track = new PM24_Track(trackCode);
      this.resource.addProperty(FRBROO.R5_has_component, track.asResource());
      model.add(track.getModel());
    }

    for (String title : getTitles()) {
      this.resource.addProperty(CIDOC.P102_has_title, title)
        .addProperty(RDFS.label, title);
    }

    for (String subtitle : record.getDatafieldsByCode(200, 'e')) {
      subtitle = subtitle.trim();
      this.resource.addProperty(MUS.U67_has_subtitle, subtitle);
    }


    String duration = PM24_Track.getDuration(this.record);
    if (duration != null)
      this.resource.addProperty(MUS.U53_has_duration, duration, XSDDatatype.XSDdayTimeDuration);
  }

  private List<String> getTitles() {
    return record.getDatafieldsByCode(200, 'a').stream()
      .map(String::trim)
      .collect(Collectors.toList());
  }

  private List<String> getTracks() {
    return record.getDatafieldsByCode(462, 3);
  }


  public PM46_SetOfTracks add(PM43_PerformedExpression exp) {
    this.asResource().addProperty(MUS.U51_is_partial_or_full_recording_of, exp.asResource());
    return this;
  }
}
