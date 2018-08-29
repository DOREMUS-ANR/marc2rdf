package org.doremus.marc2rdf.marcparser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Record {
  private final List<Record> subRecords;
  private final List<DataField> dataFields;
  private final List<ControlField> controlFields;
  private final List<Attr> attrList;
  private String id;
  private String type;
  private int level;
  private Record parent;

  public Record(String type, String id) {
    controlFields = new ArrayList<>();
    dataFields = new ArrayList<>();
    attrList = new ArrayList<>();
    subRecords = new ArrayList<>();
    this.type = type;
    this.id = id;
    this.level = 0;
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

  public boolean isANL() {
    ControlField leader = getControlfieldByCode("leader");
    return this.isBIB() && leader != null && leader.getData().charAt(8) == 'd';
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
    return new ArrayList<>(controlFields);
  }

  public List<Etiq> getDataFields() {
    return new ArrayList<>(dataFields);
  }

  public DataField getDatafieldByCode(int code) {
    return getDatafieldByCode(code + "");
  }

  public DataField getDatafieldByCode(String code) {
    return this.dataFields.stream().filter(field -> field.getEtiq().equals(code)).findFirst().orElse(null);
  }

  public List<DataField> getDatafieldsByCode(int code) {
    return getDatafieldsByCode(code + "");
  }

  public List<DataField> getDatafieldsByCode(String code) {
    return this.dataFields.stream()
      .filter(field -> field.getEtiq().equals(code))
      .collect(Collectors.toList());
  }

  public List<String> getDatafieldsByCode(int code, int subFieldCode) {
    return getDatafieldsByCode(code, String.valueOf(subFieldCode).charAt(0));
  }

  public List<String> getDatafieldsByCode(int code, char subFieldCode) {
    return getDatafieldsByCode("" + code, subFieldCode);
  }

  public List<String> getDatafieldsByCode(String code, char subFieldCode) {
    return this.getDatafieldsByCode(code).stream()
      .flatMap(field -> field.getSubfields(subFieldCode).stream())
      .map(Subfield::getData)
      .collect(Collectors.toList());
  }

  public String getDatafieldByCode(String code, char subFieldCode) {
    return this.getDatafieldsByCode(code, subFieldCode)
      .stream().findFirst().orElse(null);
  }

  public List<String> getDatafieldsByCodePropagate(int code, char subFieldCode) {
    return getDatafieldsByCodePropagate("" + code, subFieldCode);
  }

  public List<String> getDatafieldsByCodePropagate(String code, char subFieldCode) {
    List<String> results = getDatafieldsByCode(code, subFieldCode);
    if (results.size() > 0 || this.isRoot()) return results;
    else return this.parent.getDatafieldsByCodePropagate(code, subFieldCode);
  }

  public List<DataField> getDatafieldsByCodePropagate(int code) {
    return getDatafieldsByCodePropagate("" + code);
  }

  public List<DataField> getDatafieldsByCodePropagate(String code) {
    List<DataField> results = getDatafieldsByCode(code);
    if (results.size() > 0 || this.isRoot()) return results;
    else return this.parent.getDatafieldsByCodePropagate(code);
  }

  public ControlField getControlfieldByCode(String code) {
    return this.controlFields.stream()
      .filter(field -> field.getEtiq().equals(code))
      .findFirst().orElse(null);
  }

  public List<ControlField> getControlfieldsByCode(String code) {
    return this.controlFields.stream()
      .filter(field -> field.getEtiq().equals(code))
      .collect(Collectors.toList());
  }

  public Attr getAttrByName(String name) {
    return this.attrList.stream()
      .filter(at -> at.getName().equals(name))
      .findFirst().orElse(null);
  }

  public List<Etiq> getAllData() {
    List<Etiq> fields = new ArrayList<>(controlFields);
    fields.addAll(dataFields);
    return fields;
  }

  public int size() {
    return controlFields.size() + dataFields.size();
  }

  @Override
  public String toString() {
    List<String> lines = getAllData().stream()
      .map(Etiq::toString)
      .collect(Collectors.toList());
    return String.join("\n", lines);
  }

  void setLevel(String value) {
    if (value == null) return;
    try {
      this.level = Integer.parseInt(value);
    } catch (NumberFormatException nfe) {
      // do nothing
    }
  }

  public int getLevel() {
    return level;
  }

  void addSubRecord(Record subRecord) {
    this.subRecords.add(subRecord);
    subRecord.setParent(this);
  }

  public List<Record> getSubRecords() {
    return this.subRecords;
  }

  private void setParent(Record record) {
    this.parent = record;
  }

  private boolean isRoot() {
    return this.parent == null;
  }

}
