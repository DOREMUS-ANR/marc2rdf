package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.ontology.MUS;

public class M23_Casting_Detail {
  private final Model model;

  private Resource mop;
  private int num;
  private boolean solo;


  public M23_Casting_Detail(String code, String num) {
    this(code, num, false);
  }

  public M23_Casting_Detail(String code, String num, boolean isSoloist) {
    this.num = Integer.parseInt(num);
    this.mop = Converter.mopVocabulary.findConcept(code);
    this.solo = isSoloist;

    this.model = ModelFactory.createDefaultModel();
  }

  public Resource asResource(String uri) {
    Resource M23CastingDetail = model.createResource(uri);
    M23CastingDetail.addProperty(RDF.type, MUS.M23_Casting_Detail)
      .addProperty(MUS.U2_foresees_use_of_medium_of_performance_of_type, mop)
      .addProperty(MUS.U9_has_quantity, model.createTypedLiteral(num));

    if (solo)
      M23CastingDetail.addProperty(
        MUS.U36_foresees_responsibility_of_type, model.createLiteral("soliste", "fr"));

    return M23CastingDetail;
  }
}
