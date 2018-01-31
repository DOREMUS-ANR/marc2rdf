package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PF29_RecordingEvent extends DoremusResource {
  private int countActivity;

  private final PF26_Recording f26_recording;
  private final PF21_RecordingWork f21_recording_work;

  public PF29_RecordingEvent(Record record) throws URISyntaxException {
    super(record);
    this.countActivity = 0;
    this.resource.addProperty(RDF.type, FRBROO.F29_Recording_Event)
      .addProperty(CIDOC.P32_used_general_technique, "video");

    this.f26_recording = new PF26_Recording(record);
    this.f21_recording_work = new PF21_RecordingWork(record);
    this.resource.addProperty(FRBROO.R21_created, f26_recording.asResource())
      .addProperty(FRBROO.R22_created_a_realisation_of, f21_recording_work.asResource());
    this.f21_recording_work.asResource()
      .addProperty(FRBROO.R13_is_realised_in, f26_recording.asResource());
    model.add(f26_recording.getModel());


    for (String note : record.getDatafieldsByCode(200, 'e'))
      this.addNote(note);
    for (String note : record.getDatafieldsByCode(300, 'e')) {
      if (note.contains("Prise de son") || note.contains("enregistré par"))
        this.addNote(note);
    }

    for (String producer : record.getDatafieldsByCode(911, 'a')) {
      producer = producer.trim();
      Resource prod;
      if (producer.equals("Philharmonie de Paris"))
        prod = model.createResource(PP2RDF.organizationURI);
      else {
        CorporateBody cb = new CorporateBody(producer);
        prod = cb.asResource();
        model.add(cb.getModel());
      }

      E7_Activity activity = new E7_Activity(this.uri + "/activity/" + ++countActivity, prod, "Producteur de vidéogramme");
      this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
      this.model.add(activity.getModel());
    }

    List<DataField> editors = record.getDatafieldsByCode(700);
    editors.addAll(record.getDatafieldsByCode(701));
    for (DataField df : editors) {
      if (!"300".equals(df.getString(4))) continue;
      Person editor = Person.fromUnimarcField(df);
      E7_Activity activity = new E7_Activity(this.uri + "/activity/" + ++countActivity, editor, "Réalisateur");
      this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
      this.model.add(activity.getModel());
    }

    TimeSpan ts = getDate();
    if (ts != null)
      this.resource.addProperty(CIDOC.P4_has_time_span, ts.asResource());

    getPlace();
  }

  private TimeSpan getDate() {
    return TimeSpan.fromUnimarcField(record.getDatafieldByCode(981));
  }


  private final static String PLACE_REGEX = "(Cité de la musique|Salle Pleyel|Philharmonie de Paris|Grande Halle de la Villette)(?: \\((.+)\\))?";
  private final static Pattern PLACE_PATTERN = Pattern.compile(PLACE_REGEX);

  private void getPlace() throws URISyntaxException {
    for (String note : record.getDatafieldsByCode(200, 'e')) {
      Matcher m = PLACE_PATTERN.matcher(note);
      if (!m.find()) continue;

      String place = m.group(1),
        room = m.group(2);

      E53_Place p1 = new E53_Place(place);
      this.model.add(p1.getModel());
      this.resource.addProperty(MUS.U7_foresees_place_at, p1.asResource());

      if (room != null) {
        E53_Place p2 = new E53_Place(room);
        p2.addSurroundingPlace(p1);
        this.model.add(p2.getModel());
        this.resource.addProperty(MUS.U7_foresees_place_at, p2.asResource());
      }
    }
  }
}
