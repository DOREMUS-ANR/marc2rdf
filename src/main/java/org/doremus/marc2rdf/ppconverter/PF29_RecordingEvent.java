package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PF29_RecordingEvent extends DoremusResource {
  private final static String PLACE_REGEX = "(Cité de la musique|Salle Pleyel|Philharmonie de Paris|Grande Halle de " +
    "la Villette).*(?: \\((?:(\\d{2}h\\d{2}), )?(.+)\\))?";
  private final static Pattern PLACE_PATTERN = Pattern.compile(PLACE_REGEX);

  private int countActivity;

  private final PF26_Recording f26_recording;
  private final PF21_RecordingWork f21_recording_work;
  private TimeSpan timeSpan;
  private List<E53_Place> places;

  public PF29_RecordingEvent(Record record) {
    super(record);
    this.countActivity = 0;
    this.places = new ArrayList<>();

    this.setClass(FRBROO.F29_Recording_Event);
    this.addProperty(CIDOC.P32_used_general_technique, record.isType("UNI:2") ? "audio" : "video");

    this.f26_recording = new PF26_Recording(record);
    this.f21_recording_work = new PF21_RecordingWork(record);
    this.addProperty(FRBROO.R21_created, f26_recording);
    this.addProperty(FRBROO.R22_created_a_realisation_of, f21_recording_work);
    this.f21_recording_work.addProperty(FRBROO.R13_is_realised_in, f26_recording);

    record.getDatafieldsByCode(200, 'e').forEach(this::addNote);

    record.getDatafieldsByCode(300, 'e').stream()
      .filter(note -> note.contains("Prise de son") || note.contains("enregistré par"))
      .forEach(this::addNote);

    String function = (record.isType("UNI:4")) ?
      "producteur de vidéogramme" : "producteur (enregistrement phonographique)";

    for (String producer : getProducers()) {
      producer = producer.trim();
      Resource prod;
      switch (producer) {
        case "Philharmonie de Paris":
          prod = PP2RDF.PHILHARMONIE;
          break;
        case "Radio France":
          prod = PP2RDF.RADIO_FRANCE;
          break;
        default:
          CorporateBody cb = new CorporateBody(producer);
          prod = cb.asResource();
          model.add(cb.getModel());
          break;
      }

      E7_Activity activity = new E7_Activity(this.uri + "/activity/" + ++countActivity, prod, function);
      this.addProperty(CIDOC.P9_consists_of, activity);
    }

    if (record.isType("UNI:4")) {
      List<DataField> editors = record.getDatafieldsByCode(700);
      editors.addAll(record.getDatafieldsByCode(701));
      for (DataField df : editors) {
        if (!"300".equals(df.getString(4))) continue;
        Person editor = Person.fromUnimarcField(df);
        E7_Activity activity = new E7_Activity(this.uri + "/activity/" + ++countActivity, editor, "Réalisateur");
        this.addProperty(CIDOC.P9_consists_of, activity);
      }
    }

    timeSpan = getDate();
    if (timeSpan != null) {
      this.timeSpan.setUri(this.uri + "/time");
      this.addTimeSpan(timeSpan);
    }

    computePlaces();
  }

  private List<String> getProducers() {
    if (record.isType("UNI:4"))
      return record.getDatafieldsByCode(911, 'a');

    // UNI:2
    for (String s : record.getDatafieldsByCode(300, 'a')) {
      s = s.toLowerCase();
      if (s.contains("france musique") || s.contains("Radio France"))
        return Collections.singletonList("Radio France");
    }
    return record.getDatafieldsByCode(210, 'c').stream()
      .map(String::toLowerCase)
      .filter(s -> s.equals("Cité de la musique") || s.equals("Philharmonie de Paris"))
      .collect(Collectors.toList());
  }

  private TimeSpan getDate() {
    String time = null;
    for (String note : record.getDatafieldsByCode(200, 'e')) {
      Matcher m = PLACE_PATTERN.matcher(note);
      if (!m.find()) continue;
      time = m.group(2);
      if (time != null) {
        time = time.replace('h', ':');
        break;
      }
    }

    return TimeSpan.fromUnimarcField(record.getDatafieldByCode(981), time);
  }


  private void computePlaces() {
    for (String note : record.getDatafieldsByCode(200, 'e')) {
      Matcher m = PLACE_PATTERN.matcher(note);
      if (!m.find()) continue;

      String place = m.group(1),
        room = m.group(3);

      E53_Place p1 = new E53_Place(place);
      this.setPlace(p1);

      if (room != null) {
        E53_Place p2 = new E53_Place(room);
        p2.addSurroundingPlace(p1);
        this.setPlace(p2);
      }
    }
  }

  public PF26_Recording getRecording() {
    return f26_recording;
  }

  public PF29_RecordingEvent add(PF31_Performance performance) {
    this.addProperty(FRBROO.R20_recorded, performance);
    return this;
  }

  public PF29_RecordingEvent add(PF4_ManifestationSingleton support) {
    this.resource.addProperty(FRBROO.R18_created, support.asResource());
    support.addProperty(CIDOC.P128_carries, this.getRecording());
    return this;
  }


  public TimeSpan getTime() {
    return timeSpan;
  }

  public void setPlace(E53_Place place) {
    this.addProperty(CIDOC.P7_took_place_at, place);
    this.places.add(place);
  }

  public List<E53_Place> getPlaces() {
    return places;
  }

}
