package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F4_Manifestation_Singleton extends BIBDoremusResource {
  public F4_Manifestation_Singleton(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F4_Manifestation_Singleton);
  }

  public F4_Manifestation_Singleton(DataField df) {
    // for field 325

    this(df.getString('u'));

    this.addProperty(CIDOC.P2_has_type, "score");
    this.addProperty(CIDOC.P2_has_type, "handwritten score");

    // title
    String title = df.getString(df.isCode('a') ? 'a' : 'k');
    if (df.isCode('b'))
      title += ". " + df.getString('b');
    if (df.isCode('l'))
      title += " (" + df.getString('l') + ")";
    this.addProperty(RDFS.label, title)
      .addProperty(CIDOC.P102_has_title, title);

    // identifier
    this.addComplexIdentifier(this.identifier, "Shelfmark");

    // actor
    String actor = df.getString('n');
    String place = df.getString('m');
    if (actor != null) {
      CorporateBody actorX = new CorporateBody(actor);
      actorX.interlink();
      if (place != null) actorX.addResidence(place);
      this.addProperty(CIDOC.P49_has_former_or_current_keeper, actorX);
    }
  }
}
