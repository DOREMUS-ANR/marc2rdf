package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.E53_Place;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.Objects;

public class F29_RecordingEvent extends BIBDoremusResource {

  public F29_RecordingEvent(Record record, Record mainRecord, int i) {
    super(record, mainRecord, i);
    this.setClass(FRBROO.F29_Recording_Event);

    getDatafieldsByCodeFull(352, 'a').stream()
      .filter(x -> x.toLowerCase().contains("prod."))
      .forEach(this::addNote);

    getDatafieldsByCodeFull(700).stream()
      .filter(df -> "3160".equals(df.getString(4)))
      .map(ArtistConverter::parseArtistField)
      .forEach(p -> this.addActivity(p, "collecteur"));

    this.addProperty(CIDOC.P125_used_object_of_type, parseRecordingMethod(), "fr");

    // 280 c
    String field280c = get280c();
    String spatialization = "stereo";
    if (field280c.contains("mono")) spatialization = "mono";
    else if (field280c.contains("quadriphonie")) spatialization = "quadriphonie";
    this.addProperty(MUS.U225_used_sound_spatialization_technique, spatialization);

    if (field280c.contains("dolby") || field280c.contains("dbx"))
      this.addProperty(MUS.U192_used_noise_reduction_technique, "Dual-ended systems", "en");
  }

  private String get280c() {
    List<String> field280 = getDatafieldsByCodeFull(280, 'c');
    if (field280.size() == 0) return "";
    return field280.get(0).toLowerCase();
  }

  private String parseRecordingMethod() {
    return getDatafieldsByCodeFull("009", 'g').stream()
      .map(x -> x.charAt(5))
      .map(c -> {
        switch (c) {
          case 'a':
            return "acoustique";
          case 'b':
            return "électrique";
          case 'c':
            return "numérique";
          case 'd':
            return "analogique";
          case 'm':
            return "multiple";
          default:
            return null;
        }
      }).filter(Objects::nonNull)
      .findFirst().orElse(null);
  }

  public F29_RecordingEvent add(F26_Recording recording) {
    this.addProperty(FRBROO.R21_created, recording);
    return this;
  }

  public F29_RecordingEvent add(F21_RecordingWork recordingWork) {
    this.addProperty(FRBROO.R22_created_a_realisation_of, recordingWork);
    return this;
  }

  public void addProducer(Artist producer) {
    this.addActivity(producer, "producteur (enregistrement phonographique)");
  }

  public F29_RecordingEvent add(F31_Performance performance) {
    this.addProperty(FRBROO.R20_recorded, performance);
    return this;
  }

  public void setPlace(E53_Place place) {
    this.addProperty(CIDOC.P7_took_place_at, place);
  }
}
