package org.doremus.marc2rdf.marcparser;

import java.util.ArrayList;
import java.util.List;

public class DataField extends Etiq {

  private char ind1;
  private char ind2;
  private boolean useInds;
  private List<Subfield> subfields = new ArrayList<>();

  public DataField(String etiq) {
    super(etiq);
    this.useInds = false;
  }

  public DataField(String etiq, char ind1, char ind2) {
    this(etiq);
    this.ind1 = ind1;
    this.ind2 = ind2;
    this.useInds = true;
  }

  public char getIndicator1() {
    return ind1;
  }

  public char getIndicator2() {
    return ind2;
  }

  public void addSubfield(Subfield subfield) {
    subfields.add(subfield);
  }

  public List<Subfield> getSubfields() {
    return subfields;
  }

  public boolean isCode(int c) {
    return isCode(String.valueOf(c).charAt(0));
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
  public Subfield getSubfield(int c) {
    return getSubfield(String.valueOf(c).charAt(0));
  }

  public Subfield getSubfield(char code) {
    for (Subfield subfield : this.subfields) {
      if (subfield.getCode() == code)
        return subfield;
    }
    return null;
  }

  public List<Subfield> getSubfields(char code) {
    List<Subfield> results = new ArrayList<>();
    for (Subfield subfield : this.subfields) {
      if (subfield.getCode() == code) results.add(subfield);
    }
    return results;
  }

  /******************
   * Check if a certain subfield is present with a certain value
   *******************/
  public boolean hasSubfieldValue(char code, String value) {
    for (Subfield s : getSubfields(code))
      if (s.getData().equals(value))
        return true;
    return false;
  }


  /***************
   * Recuperer la valeur d'une zone
   ***********************/
  public String getString(int c) {
    return getString(String.valueOf(c).charAt(0));
  }

  public String getString(char code) {
    if (!isCode(code)) return null;
    String content = getSubfield(code).getData();
    content = content.trim();
    if (content.isEmpty()) return null;
    else return content;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    sb.append(' ');

    if (useInds) {
      sb.append(getIndicator1());
      sb.append(getIndicator2());
    }

    for (Subfield sf : subfields) {
      sb.append(sf.toString());
    }

    return sb.toString();
  }


}
