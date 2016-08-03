package org.doremus.marc2rdf.main;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class ConstructURI {

  public static URI build(String classe, String identificator) throws URISyntaxException {

    URIBuilder builder = new URIBuilder().setScheme("http").setHost("data.doremus.org")
      .setPath("/" + classe + "/" + identificator + "/" + generateUUID());

    return builder.build();
  }

  private static String generateUUID() {
    // FIXME the id is randomly generated
    // see https://github.com/DOREMUS-ANR/marc2rdf/issues/16
    return UUID.randomUUID().toString();
  }

}
