package org.doremus.marc2rdf.bnfparser;

import java.util.ArrayList;
import java.util.List;

public class Record {

  public List<DataField> dataFields;
  public List<ControlField> controlFields;
  public String id;
  public String type;

  public Record(String type, String id) {
    controlFields = new ArrayList<ControlField>();
    dataFields = new ArrayList<DataField>();
    this.type = type;
    this.id = id;
  }

  public String getIdentifier() {
    return this.id;
  }

  public String getType() {
    return this.type;
  }

  public void addControlField(Etiq field) {
    controlFields.add((ControlField) field);
  }

  public void addDataField(Etiq field) {
    dataFields.add((DataField) field);
  }

  public List<Etiq> getControlFields() {
    List<Etiq> fields = new ArrayList<Etiq>();
    fields.addAll(controlFields);
    return fields;
  }

  public List<Etiq> getDataFields() {
    List<Etiq> fields = new ArrayList<Etiq>();
    fields.addAll(dataFields);
    return fields;
  }

  public List<Etiq> getAllData() {
    List<Etiq> fields = new ArrayList<Etiq>();
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
  /*****************************************************************************/
}
