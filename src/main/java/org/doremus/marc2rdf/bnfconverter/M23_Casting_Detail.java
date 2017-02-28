package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.MUS;
import org.doremus.vocabulary.VocabularyManager;

public class M23_Casting_Detail {
  private final Model model;

  private Resource mop;
  private int num;
  private boolean solo;


  public M23_Casting_Detail(String code, String num) {
    this(code, num, false);
  }

  public M23_Casting_Detail(String code, String num, boolean isSoloist) {
    if (num != null && !num.trim().isEmpty())
      this.num = Integer.parseInt(num.trim());
    else this.num = -1;

    this.mop = VocabularyManager.getVocabulary("mop-iaml").getConcept(code);
    this.solo = isSoloist;

    this.model = ModelFactory.createDefaultModel();
  }

  public Resource asResource(String uri) {
    Resource M23CastingDetail = model.createResource(uri)
      .addProperty(RDF.type, MUS.M23_Casting_Detail)
      .addProperty(MUS.U2_foresees_use_of_medium_of_performance_of_type, mop);

    if (num != -1) M23CastingDetail.addProperty(MUS.U30_foresees_quantity_of_mop, model.createTypedLiteral(num));

    if (solo)
      M23CastingDetail.addProperty(
        MUS.U36_foresees_responsibility_of_type, model.createLiteral("soliste", "fr"));

    return M23CastingDetail;
  }
}
