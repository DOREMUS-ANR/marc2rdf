package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F4_ManifestationSingleton extends BIBDoremusResource {
  public F4_ManifestationSingleton(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F4_Manifestation_Singleton);
  }

  public F4_ManifestationSingleton(String identifier, DataField df) {
    // for field 325
    this(identifier);

    this.addProperty(CIDOC.P2_has_type, "score");
    this.addProperty(CIDOC.P2_has_type, "manuscript");

    // title
    String title = df.getString(df.isCode('a') ? 'a' : 'k');
    if (df.isCode('b'))
      title += ". " + df.getString('b');
    if (df.isCode('l'))
      title += " (" + df.getString('l') + ")";
    this.addProperty(RDFS.label, title)
      .addProperty(CIDOC.P102_has_title, title);

    // identifier
    this.addComplexIdentifier(df.getString('u'), "Shelfmark");

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

  public F4_ManifestationSingleton add(F22_SelfContainedExpression f22) {
    this.addProperty(CIDOC.P128_carries, f22);
    return this;
  }
}
