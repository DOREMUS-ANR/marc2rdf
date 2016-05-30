package org.doremus.marc2rdf.ppconverter;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class ConstructURI {

  public URI getUUID(String classe, String identificator, String uuid) throws URISyntaxException {

    /**************************************************************************/
    URIBuilder builder = new URIBuilder().setScheme("http").setHost("data.doremus.org")
      .setPath("/" + classe + "/" + identificator + "/" + uuid);
    URI uri = builder.build();
    /**************************************************************************/
    return uri;

  }

}
