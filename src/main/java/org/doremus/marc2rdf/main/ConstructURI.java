package org.doremus.marc2rdf.main;

import org.apache.http.client.utils.URIBuilder;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.UUID;

public class ConstructURI {
  private static URIBuilder builder = new URIBuilder().setScheme("http").setHost("data.doremus.org");

  public static URI build(String db, String className, String identifier) throws URISyntaxException {
    String seed = db + className + identifier;
    return builder.setPath("/" + getCollectionName(className) + "/" + generateUUID(seed)).build();
  }

  private static String generateUUID(String seed) {
    // source: https://gist.github.com/giusepperizzo/630d32cc473069497ac1
    try {
      String hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(seed.getBytes("UTF-8")));
      UUID uuid = UUID.nameUUIDFromBytes(hash.getBytes());
      return uuid.toString();
    } catch (Exception e) {
      System.err.println("[ConstructURI.java]" + e.getLocalizedMessage());
      return "";
    }
  }

  private static String getCollectionName(String className) {
    switch (className) {
      case "F22_SelfContainedExpression":
      case "F25_PerformancePlan":
        return "expression";
      case "F28_ExpressionCreation":
      case "F30_PublicationEvent":
      case "F42_RepresentativeExpressionAssignment":
        return "event";
      case "F14_IndividualWork":
      case "F15_ComplexWork":
      case "F19_PublicationWork":
        return "work";
      case "F24_PublicationExpression":
        return "publication";
      case "F31_Performance":
        return "performance";
      default:
        throw new RuntimeException("[ConstructURI.java] Class not assigned to a collection: " + className);
    }
  }
}
