package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class PM29_Editing extends DoremusResource {
  private int countActivity;

  public PM29_Editing(Record record) throws URISyntaxException {
    super(record);
    this.countActivity = 0;
    this.resource.addProperty(RDF.type, MUS.M29_Editing);

    for (String producer : record.getDatafieldsByCode(911, 'a')) {
      producer = producer.trim();
      if (producer.equals("Philharmonie de Paris"))
        this.addProducer(model.createResource(PP2RDF.organizationURI));
      else addProducer(new CorporateBody(producer));
    }
  }

  private void addProducer(CorporateBody corporateBody) {
    this.model.add(corporateBody.getModel());
    addProducer(corporateBody.asResource());
  }

  private void addProducer(Resource producer) {
    String activityUri = this.uri + "/activity/" + ++countActivity;
    E7_Activity activity = new E7_Activity(activityUri, producer, "audio producer");
    this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
    this.model.add(activity.getModel());
  }

  public PM29_Editing add(PM46_SetOfTracks tracks) {
    this.resource.addProperty(FRBROO.R17_created, tracks.asResource());
    return this;
  }

  public PM29_Editing add(PF29_RecordingEvent recordingEvt) {
    this.resource.addProperty(MUS.U29_edited, recordingEvt.getRecording().asResource());
    return this;
  }

  public PM29_Editing add(PF4_ManifestationSingleton support) {
    this.resource.addProperty(FRBROO.R18_created, support.asResource());
    return this;
  }
}
