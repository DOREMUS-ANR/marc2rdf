package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.ontology.CRO;

public class ProducersRights extends AbstractRight {
  public ProducersRights(String uri, Artist producer, String year) {
    super(uri, producer, year);
    this.setClass(CRO.ProducersRights);
  }

  public ProducersRights(String uri, String note, String year) {
    this(uri, producerFromNote(note), year);
    this.addNote(note);
  }

  private static Artist producerFromNote(String note) {
    String label = note.replaceAll(", .+", "");
    if (label.contains(":")) label = label.substring(label.lastIndexOf(":") + 1);
    label = label.trim();
    return new CorporateBody(label);
  }
}
