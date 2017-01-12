package org.doremus.marc2rdf.marcparser;

public class Attr {
  private String name;
  private String data;

  public Attr(String name){
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }
}
