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

    this.addProducer(model.createResource(PP2RDF.organizationURI));
    for (String producer : record.getDatafieldsByCode(911, 'a')) {
      producer = producer.trim();
      if (producer.equals("Philharmonie de Paris")) continue;

      addProducer(new CorporateBody(producer));
    }
  }

  private void addProducer(CorporateBody corporateBody) {
    this.model.add(corporateBody.getModel());
    addProducer(corporateBody.asResource());
  }

  private void addProducer(Resource producer) {
    try {
      E7_Activity activity = new E7_Activity(this.uri + "/activity/" + ++countActivity, producer, "Producteur de vidéogramme");
      this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
      this.model.add(activity.getModel());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public PM29_Editing add(PM46_SetOfTracks tracks) {
    this.resource.addProperty(FRBROO.R17_created, tracks.asResource());
    return this;
  }
}
