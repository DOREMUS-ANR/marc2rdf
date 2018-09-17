package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

public class M23_Casting_Detail extends DoremusResource {
  private Resource mop;
  private int num;
  private boolean solo;


  public M23_Casting_Detail(String code, String num) {
    this(code, num, false);
  }

  public M23_Casting_Detail(String code, String num, boolean isSoloist) {
    this(VocabularyManager.getVocabulary("mop-iaml").getConcept(code), num, isSoloist);
  }

  public M23_Casting_Detail(Resource mop, String num, boolean isSoloist) {
    super();
    this.mop = mop;
    this.solo = isSoloist;

    if (num != null && num.trim().matches("\\d+")) this.num = Integer.parseInt(num.trim());
    else this.num = -1;
  }

  public Resource asResource() {
    if (this.uri == null) throw new RuntimeException("Casting Detail without URI");
    this.resource = model.createResource(uri);
    this.setClass(MUS.M23_Casting_Detail);
    this.addProperty(MUS.U2_foresees_use_of_medium_of_performance, mop);

    if (num != -1) this.addProperty(MUS.U30_foresees_quantity_of_mop, model.createTypedLiteral(num));
    if (solo) this.addProperty(MUS.U36_foresees_responsibility, "soliste", "fr");
    return this.resource;
  }
}
