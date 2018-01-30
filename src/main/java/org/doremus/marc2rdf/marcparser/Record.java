package org.doremus.marc2rdf.marcparser;

import java.util.ArrayList;
import java.util.List;

public class Record {

  private List<DataField> dataFields;
  private List<ControlField> controlFields;
  private List<Attr> attrList;
  private String id;
  private String type;

  public Record(String type, String id) {
    controlFields = new ArrayList<>();
    dataFields = new ArrayList<>();
    attrList = new ArrayList<>();
    this.type = type;
    this.id = id;
  }

  public String getIdentifier() {
    return this.id;
  }

  public String getType() {
    return this.type;
  }

  public boolean isType(String type) {
    return this.type.equals(type);
  }

  public boolean isTUM() {
    return this.isType("Authority")  // TUM BnF
      || this.isType("AIC:14")// TUM PP
      || this.isType("UNI:100"); // Ouvres PP
  }

  public boolean isBIB() {
    return this.isType("Bibliographic");
  }


  void addControlField(Etiq field) {
    controlFields.add((ControlField) field);
  }

  void addDataField(Etiq field) {
    dataFields.add((DataField) field);
  }

  void addAttr(Attr attr) {
    attrList.add(attr);
  }

  public List<Etiq> getControlFields() {
    List<Etiq> fields = new ArrayList<>();
    fields.addAll(controlFields);
    return fields;
  }

  public List<Etiq> getDataFields() {
    List<Etiq> fields = new ArrayList<>();
    fields.addAll(dataFields);
    return fields;
  }

  public DataField getDatafieldByCode(int code) {
    return getDatafieldByCode(code + "");
  }

  public DataField getDatafieldByCode(String code) {
    for (DataField field : this.dataFields) {
      if (field.getEtiq().equals(code)) {
        return field;
      }
    }
    return null;
  }

  public List<DataField> getDatafieldsByCode(int code) {
    return getDatafieldsByCode(code + "");
  }

  public List<DataField> getDatafieldsByCode(String code) {
    List<DataField> results = new ArrayList<>();
    for (DataField field : this.dataFields) {
      if (field.getEtiq().equals(code)) {
        results.add(field);
      }
    }
    return results;
  }

  public List<String> getDatafieldsByCode(int code, int subFieldCode) {
    return getDatafieldsByCode(code, String.valueOf(subFieldCode).charAt(0));
  }

  public List<String> getDatafieldsByCode(int code, char subFieldCode) {
    return getDatafieldsByCode("" + code, subFieldCode);
  }

  public List<String> getDatafieldsByCode(String code, char subFieldCode) {
    List<String> results = new ArrayList<>();
    for (DataField field : this.getDatafieldsByCode(code)) {
      for (Subfield sf : field.getSubfields(subFieldCode))
        results.add(sf.getData());
    }
    return results;
  }

  public ControlField getControlfieldByCode(String code) {
    for (ControlField field : this.controlFields) {
      if (field.getEtiq().equals(code)) {
        return field;
      }
    }
    return null;
  }

  public List<ControlField> getControlfieldsByCode(String code) {
    List<ControlField> results = new ArrayList<>();
    for (ControlField field : this.controlFields) {
      if (field.getEtiq().equals(code)) {
        results.add(field);
      }
    }
    return results;
  }

  public Attr getAttrByName(String name) {
    for (Attr at : this.attrList) {
      if (at.getName().equals(name)) {
        return at;
      }
    }
    return null;
  }

  public List<Etiq> getAllData() {
    List<Etiq> fields = new ArrayList<>();
    fields.addAll(controlFields);
    fields.addAll(dataFields);
    return fields;
  }

  public int size() {
    return controlFields.size() + dataFields.size();
  }

  /*******************
   * Affichage final de la notice
   ***************************/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Etiq field : getControlFields()) {
      sb.append(field.toString());
      sb.append('\n');
    }
    for (Etiq field : getDataFields()) {
      sb.append(field.toString());
      sb.append('\n');
    }
    return sb.toString();
  }

}
