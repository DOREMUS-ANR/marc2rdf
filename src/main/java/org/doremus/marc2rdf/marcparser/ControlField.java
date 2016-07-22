package org.doremus.marc2rdf.marcparser;

public class ControlField extends Etiq {

  private String data;

  public ControlField(String tag) {
    super(tag);
  }

  public ControlField(String tag, String data) {
    super(tag);
    this.setData(data);
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  public String toString() {
    return super.toString() + " " + getData();
  }

}
