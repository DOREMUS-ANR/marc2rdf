package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
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
      .addProperty(DCTerms.identifier, this.identifier)
      .addProperty(MUS.U227_has_content_type, "performed music", "en")
      .addProperty(MUS.U227_has_content_type, "two-dimensional moving image", "en");

    for (DataField trackField : getTracks()) {
      PM24_Track track = new PM24_Track(trackField);
      this.resource.addProperty(FRBROO.R5_has_component, track.asResource());
      model.add(track.getModel());
    }

    for (String title : getTitles()) {
      this.resource.addProperty(CIDOC.P102_has_title, title)
        .addProperty(RDFS.label, title);
    }

    String duration = getDuration();
    if (duration != null)
      this.resource.addProperty(MUS.U53_has_duration, duration, XSDDatatype.XSDdayTimeDuration);

  }

  private List<String> getTitles() {
    return record.getDatafieldsByCode(200, 'a').stream()
      .map(String::trim)
      .collect(Collectors.toList());
  }

  private List<DataField> getTracks() {
    return record.getDatafieldsByCode(462).stream()
      .filter(f -> f.isCode(3))
      .filter(f -> !f.getSubfield(3).getData().isEmpty())
      .collect(Collectors.toList());
  }


  private String getDuration() {
    for (String f : record.getDatafieldsByCode(215, 'a')) {
      if (f == null || f.isEmpty() || !f.matches(Utils.DURATION_REGEX)) continue;
      return Utils.duration2iso(f);
    }
    return null;
  }
}
