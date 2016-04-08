package org.doremus.marc2rdf.ppConverter;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;

public class ConstructURI {

	public static URI getUUID(String classe, String identificator, String uuid) throws URISyntaxException  {
		
         /**************************************************************************/   
		 URIBuilder builder = new URIBuilder().setScheme("http").setHost("data.doremus.org")
         .setPath("/"+classe +"/"+ identificator +"/"+ uuid);
		 URI uri = builder.build();
 		/**************************************************************************/
		 return uri;
		 
	}	
	
}
