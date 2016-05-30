package org.doremus.marc2rdf.ppconverter;

import java.util.UUID;

public class GenerateUUID {

  public String get() {

    /*****************************************************************/
    UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    /*****************************************************************/
    return uid.randomUUID().toString();

  }
}
