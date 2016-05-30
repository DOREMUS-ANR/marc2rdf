package org.doremus.marc2rdf.ppparser;

import java.util.ArrayList;
import java.util.List;

public class DataField extends Etiq {

  private List<Subfield> subfields = new ArrayList<Subfield>();

  public DataField(String etiq) {
    super(etiq);
  }

  public void addSubfield(Subfield subfield) {
    subfields.add(subfield);
  }

  public List<Subfield> getSubfields() {
    return subfields;
  }

  public boolean isCode(char c) {
    for (Subfield subfield : subfields) {
      if (subfield.getCode() == c)
        return true;
    }
    return false;
  }

  /************
   * Recuperer une sous-zone d'une certaine zone
   *************/
  public Subfield getSubfield(char code) {
    for (Subfield subfield : subfields) {
      if (subfield.getCode() == code)
        return subfield;
    }
    return null;
  }

  /***************
   * Recuperer la valeur d'une zone
   ***********************/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    sb.append(' ');
    for (Subfield sf : subfields) {
      sb.append(sf.toString());
    }
    return sb.toString();
  }
  /*********************************************************************/
}
