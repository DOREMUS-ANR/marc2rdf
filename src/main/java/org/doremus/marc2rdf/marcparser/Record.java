package org.doremus.marc2rdf.marcparser;

import java.util.ArrayList;
import java.util.List;

public class Record {

  public List<DataField> dataFields;
  public List<ControlField> controlFields;
  public String id;
  public String type;

  public Record(String type, String id) {
    controlFields = new ArrayList<>();
    dataFields = new ArrayList<>();
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

  public void addControlField(Etiq field) {
    controlFields.add((ControlField) field);
  }

  public void addDataField(Etiq field) {
    dataFields.add((DataField) field);
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

  public DataField getDatafieldByCode(String code) {
    for (DataField field : this.dataFields) {
      if (field.getEtiq().equals(code)) {
        return field;
      }
    }
    return null;
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
