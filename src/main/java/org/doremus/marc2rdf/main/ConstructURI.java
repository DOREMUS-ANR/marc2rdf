package org.doremus.marc2rdf.main;

import net.sf.junidecode.Junidecode;
import org.apache.http.client.utils.URIBuilder;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.UUID;

public class ConstructURI {
  private static URIBuilder builder = new URIBuilder().setScheme("http").setHost("data.doremus.org");

  public static URI build(String db, String className, String identifier) throws URISyntaxException {
    String seed = db + identifier + className;
    return builder.setPath("/" + getCollectionName(className) + "/" + generateUUID(seed)).build();
  }

  public static URI build(String className, String name) throws URISyntaxException {
    String _uuid = generateUUID(className + norm(name).toLowerCase());
    return builder.setPath("/" + getCollectionName(className) + "/" + _uuid).build();
  }

  public static URI build(String className, String firstName, String lastName, String birthDate) throws URISyntaxException {
    String seed = norm(firstName + lastName + birthDate);
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

  private static String norm(String input) {
    // remove punctuation
    String seed = input.replaceAll("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()]", " ");
    // ascii transliteration
    seed = Junidecode.unidecode(seed);
    return seed;
  }

  private static String getCollectionName(String className) {
    switch (className) {
      case "F22_SelfContainedExpression":
      case "F25_PerformancePlan":
      case "M43_PerformedExpression":
        return "expression";
      case "F28_ExpressionCreation":
      case "F30_PublicationEvent":
      case "M45_DescriptiveExpressionAssignment":
      case "F42_RepresentativeExpressionAssignment":
      case "F42_Representative_Expression_Assignment":
      case "F29_RecordingEvent":
        return "event";
      case "F14_IndividualWork":
      case "F15_ComplexWork":
      case "F19_PublicationWork":
      case "M44_PerformedWork":
      case "F21_RecordingWork":
      case "F20_PerformanceWork":
      case "F17_AggregationWork":
        return "work";
      case "F24_PublicationExpression":
        return "publication";
      case "F31_Performance":
      case "M42_PerformedExpressionCreation":
      case "M28_Individual_Performance":
        return "performance";
      case "E21_Person":
      case "F11_CorporateBody":
      case "M54_Label_Name":
        return "artist";
      case "E4_Period":
        return "period";
      case "E53_Place":
        return "place";
      case "M29_Editing":
        return "editing";
      case "F26_Recording":
        return "recording";
      case "M46_Set_of_Tracks":
      case "M46_SetOfTracks":
      case "F4_ManifestationSingleton":
      case "F3_ManifestationProductType":
        return "manifestation";
      case "M24_Track":
        return "track";
      case "M40_Context":
        return "context";
      case "M6_Casting":
        return "casting";
      case "M5_Genre":
        return "genre";
      case "prov":
        return "activity";
      default:
        throw new RuntimeException("[ConstructURI.java] Class not assigned to a collection: " + className);
    }
  }
}
