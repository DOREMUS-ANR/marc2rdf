package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.E53_Place;
import org.doremus.marc2rdf.main.GeoNames;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class F29_RecordingEvent extends BIBDoremusResource {
  private final TimeSpan timeSpan;
  private final E53_Place place;

  public F29_RecordingEvent(Record record, Record mainRecord) {
    super(record, mainRecord);
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
    String field280c = getDatafieldsByCodeFull(280, 'c').get(0).toLowerCase();
    String spatialization = "stereo";
    if (field280c.contains("mono")) spatialization = "mono";
    else if (field280c.contains("quadriphonie")) spatialization = "quadriphonie";
    this.addProperty(MUS.U225_used_sound_spatialization_technique, spatialization);

    if (field280c.contains("dolby") || field280c.contains("dbx"))
      this.addProperty(MUS.U192_used_noise_reduction_technique, "Dual-ended systems", "en");

    this.timeSpan = getDatafieldsByCodeFull(314).stream()
      .map(this::parseDate)
      .filter(Objects::nonNull)
      .findFirst().orElse(null);
    this.addTimeSpan(timeSpan);

    this.place = getDatafieldsByCodeFull(314).stream()
      .map(this::parsePlace)
      .filter(Objects::nonNull)
      .findFirst().orElse(null);
    this.addProperty(CIDOC.P7_took_place_at, this.place);
  }

  private E53_Place parsePlace(DataField df) {
    String country = df.getString('p');
    String city = df.getString('a');
    String building = df.getString('c');

    E53_Place pCountry = null, pCity = null, pBuilding;
    if (country != null)
      pCountry = E53_Place.withUri(GeoNames.getCountryUri(country));
    if (city != null) {
      pCity = new E53_Place(city, country, null);
      pCity.addSurroundingPlace(pCountry);
    }
    if (building != null) {
      pBuilding = new E53_Place(building + " (" + city + ")", country, null);
      pBuilding.addSurroundingPlace(pCity);
      return pBuilding;
    }
    return pCity != null ? pCity : pCountry;
  }

  private TimeSpan parseDate(DataField df) {
    if (!df.isCode('d')) return null;
    List<String> dates = df.getStrings('d').stream()
      .flatMap(txt -> {
        String year = txt.substring(0, 4);
        String month = "00", day = "00";
        if (txt.length() > 6) month = txt.substring(4, 6);
        if (txt.length() == 8) day = txt.substring(6);
        if (month.equals("00")) month = null;
        if (day.equals("00")) day = null;
        return Stream.of(year, month, day);
      }).collect(Collectors.toList());

    if (dates.size() == 6)
      return new TimeSpan(dates.get(0), dates.get(1), dates.get(2), dates.get(3), dates.get(4), dates.get(5));
    return new TimeSpan(dates.get(0), dates.get(1), dates.get(2));
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
}
