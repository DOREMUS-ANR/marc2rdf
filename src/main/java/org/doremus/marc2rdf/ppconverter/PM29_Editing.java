package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.List;

public class PM29_Editing extends DoremusResource {
  private int countActivity;

  public PM29_Editing(Record record) {
    super(record);
    this.countActivity = 0;
    this.setClass(MUS.M29_Editing);

    for (String producer : getProducers()) {
      producer = producer.trim();
      if (producer.equals("Philharmonie de Paris")) this.addProducer(PP2RDF.PHILHARMONIE);
      else addProducer(new CorporateBody(producer));
    }
  }

  private List<String> getProducers() {
    if (this.record.isType("UNI:4"))
      return record.getDatafieldsByCode(911, 'a');
    else //UNI:2
      return record.getDatafieldsByCode(210, 'c');
  }

  private void addProducer(CorporateBody corporateBody) {
    this.model.add(corporateBody.getModel());
    addProducer(corporateBody.asResource());
  }

  private void addProducer(Resource producer) {
    String activityUri = this.uri + "/activity/" + ++countActivity;
    E7_Activity activity = new E7_Activity(activityUri, producer, "producteur (enregistrement phonographique)");
    this.addProperty(CIDOC.P9_consists_of, activity);
  }

  public PM29_Editing add(PM46_SetOfTracks tracks) {
    this.addProperty(FRBROO.R17_created, tracks);
    return this;
  }

  public PM29_Editing add(PF29_RecordingEvent recordingEvt) {
    this.addProperty(MUS.U29_edited, recordingEvt.getRecording());
    return this;
  }

  public PM29_Editing add(PF4_ManifestationSingleton support) {
    this.addProperty(FRBROO.R18_created, support);
    return this;
  }
}
