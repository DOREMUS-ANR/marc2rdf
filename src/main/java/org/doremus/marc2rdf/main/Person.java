package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.isnimatcher.ISNIRecord;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;
import org.doremus.ontology.Schema;
import org.geonames.Toponym;

import java.io.IOException;
import java.net.URISyntaxException;

public class Person extends Artist {
  private String firstName, lastName, birthDate, deathDate, lang;
  private String birthPlace, deathPlace;
  private TimeSpan timeSpan;

  public Person(String firstName, String lastName, String birthDate, String deathDate, String lang) {
    super();
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.deathDate = deathDate;
    this.lang = lang;

    if (lastName == null) {
      throw new RuntimeException("Missing artist value: null | " + lastName + " | " + birthDate);
    }

    try {
      this.uri = ConstructURI.build("E21_Person", firstName, lastName, birthDate).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    initResource();
  }

  public Person(String firstName, String lastName, String birthDate) {
    this(firstName, lastName, birthDate, null, null);
  }

  public Person(String firstName, String lastName, String birthDate, String lang) {
    this(firstName, lastName, birthDate, null, lang);
  }


  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  private boolean hasBirthDate() {
    return this.getBirthDate() != null && !this.getBirthDate().isEmpty();
  }

  public String getBirthDate() {
    return birthDate;
  }

  public String getBirthYear() {
    if (!hasBirthDate()) return null;
    String year = birthDate;
    if (birthDate.length() > 4) year = year.substring(0, 4);
    return year.replaceAll("\\?", ".");
  }

  public String getDeathDate() {
    if (deathDate == null || deathDate.replaceAll("\\.", "").isEmpty())
      return null;
    return deathDate;
  }

  public void setBirthPlace(String place) {
    if (place == null) return;
    this.birthPlace = place;
    if (resource == null) return;

    Toponym place_toponym = GeoNames.query(place);
    E53_Place placeEntity = (place_toponym == null) ? new E53_Place(place) : new E53_Place(place);

    this.resource.addProperty(Schema.birthPlace, placeEntity.asResource());
    this.model.add(placeEntity.getModel());

  }

  public void setDeathPlace(String place) {
    if (place == null) return;
    this.deathPlace = place;
    if (resource == null) return;

    // if not country specified
    String country = null;
    if (this.birthPlace != null && !place.contains("(") && this.birthPlace.contains("(")) {
      // add the one of the birth place
      country = this.birthPlace.split("[()]")[1];
    }

    Toponym place_toponym = GeoNames.query(place, country);
    if (place_toponym == null) GeoNames.query(place);
    E53_Place placeEntity = new E53_Place(place);

    this.resource.addProperty(Schema.deathPlace, placeEntity.asResource());
    this.model.add(placeEntity.getModel());
  }

  public String getLang() {
    return lang;
  }


  public String getFullName() {
    String fullName = this.getLastName();
    if (this.getFirstName() != null) fullName = this.getFirstName() + " " + this.getLastName();
    return fullName;
  }

  public String getIdentification() {
    String identification = this.getLastName();
    if (this.getFirstName() != null) identification += ", " + this.getFirstName();
    if (this.getBirthDate() != null) {
      identification += " (" + this.getBirthDate();
      if (this.getDeathDate() != null) identification += "-" + this.getDeathDate();
      identification += ")";
    }
    return identification;
  }

  private Resource initResource() {
    this.resource = model.createResource(this.getUri());
    resource.addProperty(RDF.type, CIDOC.E21_Person);

    addProperty(FOAF.firstName, this.getFirstName(), lang);
    addProperty(FOAF.surname, this.getLastName(), lang);
    addProperty(FOAF.name, this.getFullName(), lang);
    addProperty(RDFS.label, this.getFullName(), lang);
    addProperty(CIDOC.P131_is_identified_by, this.getIdentification(), lang);

    addDate(this.getBirthDate(), false);
    addDate(this.getDeathDate(), true);

    return resource;
  }

  public void addDate(String date, boolean isDeath) {
    this.timeSpan = cleanDate(date);
    if (timeSpan == null) return;

    String url = this.uri + (isDeath ? "/death" : "/birth");
    timeSpan.setUri(url + "/interval");
    addProperty(isDeath ? CIDOC.P100i_died_in : CIDOC.P98i_was_born,
      model.createResource(url)
        .addProperty(RDF.type, isDeath ? CIDOC.E69_Death : CIDOC.E67_Birth)
        .addProperty(CIDOC.P4_has_time_span, timeSpan.asResource())
    );

    if (timeSpan.getStart() != null)
      this.resource.addProperty(isDeath ? Schema.deathDate : Schema.birthDate, timeSpan.getStart());

    model.add(timeSpan.getModel());
  }

  public void addPropertyResource(Property property, String uri) {
    if (property == null || uri == null) return;
    resource.addProperty(property, model.createResource(uri));
  }

  public void addProperty(Property property, Resource object) {
    if (property == null || object == null) return;
    resource.addProperty(property, object);
  }

  public void addProperty(Property property, String object, String lang) {
    if (property == null || object == null || object.isEmpty()) return;

    if (lang != null)
      resource.addProperty(property, model.createLiteral(object, lang));
    else
      resource.addProperty(property, object);
  }

  public void addProperty(Property property, String object) {
    addProperty(property, object, null);
  }

  private TimeSpan cleanDate(String d) {
    if (d == null || d.isEmpty() || d.startsWith(".") || d.equals("compositeur")) return null;
    if (d.equals("?")) return TimeSpan.emptyUncertain();
    TimeSpan ts;
    d = d.replaceFirst("(.{4}\\??) BC", "-$1");

    boolean uncertain = false;
    boolean after = false;
    String uncertainRegex = "(.{4}) ?\\?";
    if (d.matches(uncertainRegex)) {
      uncertain = true;
      d = d.replaceFirst(uncertainRegex, "$1");
    }
    if (d.startsWith("ca")) {
      uncertain = true;
      d = d.replaceFirst("^ca", "").trim();
    }
    if (d.startsWith("après")) {
      after = true;
      d = d.replaceFirst("^après", "").trim();
    }
    // "850?" is 850-uncertain, not 8500-precision at decade
    if (!d.startsWith("1") && d.length() > 3 && d.charAt(3) == '?') {
      uncertain = true;
      d = d.substring(0, 3);
    }
    if (d.replaceFirst("^-", "").length() > 4) {
      // I have the info on the end date!
      ts = new TimeSpan(d.substring(0, 4), d.substring(4));
    } else ts = new TimeSpan(d);

    if (uncertain) ts.setQuality(TimeSpan.Precision.UNCERTAINTY);
    if (after) ts.setQuality(TimeSpan.Precision.AFTER);

    return ts;
  }

  public static Person fromUnimarcField(DataField field) {
    // for fields 700 and 701
    String firstName = null, lastName = null, birthDate = null, deathDate = null;
    if (field.isCode('a'))  // surname
      lastName = field.getString('a').trim();

    if (field.isCode('b'))  // name
      firstName = field.getString('b').trim();

    if (field.isCode('f')) { // birth - death dates
      String[] dates = field.getString('f').split("-");
      birthDate = dates[0].trim();
      if (dates.length > 1) deathDate = dates[1].trim();
    }
    return new Person(firstName, lastName, birthDate, deathDate, null);
  }

  public void interlink() {
    // 1. search in doremus by name/date
    Resource match = getPersonFromDoremus();
    if (match != null) {
      this.setUri(match.getURI());
      return;
    }

    // 2. search in isni by name/date
    ISNIRecord isniMatch = null;
    try {
      isniMatch = ISNIWrapper.search(this.getFullName(), this.getBirthYear());
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (isniMatch == null) return;

    // 3. search in doremus by isni
    match = getPersonFromDoremus(isniMatch.uri);
    if (match != null) {
      this.setUri(match.getURI());
      return;
    }

    // 4. add isni info
    this.isniEnrich(isniMatch);
  }

  private Resource getPersonFromDoremus() {
    return getFromDoremus(this.getFullName(), this.getBirthYear());
  }

  public static Resource getFromDoremus(String name, String birthDate) {
    String sparql = "PREFIX ecrm: <" + CIDOC.getURI() + ">\n" +
      "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
      "PREFIX prov: <" + PROV.getURI() + ">\n" +
      "PREFIX schema: <" + Schema.getURI() + ">\n" +
      "SELECT DISTINCT ?s " +
      "FROM <http://data.doremus.org/bnf> " +
      "WHERE { " +
      "?s a ecrm:E21_Person; foaf:name \"" + name + "\"." +
      (birthDate != null ? "?s schema:birthDate ?date. FILTER regex(str(?date), \"" + birthDate +
        "\")\n" : "") +
      "}";

    return (Resource) Utils.queryDoremus(sparql, "s");
  }

  private static Resource getPersonFromDoremus(String isni) {
    String sparql =
      "PREFIX owl: <" + OWL.getURI() + ">\n" +
        "SELECT DISTINCT * WHERE {" +
        " ?s owl:sameAs <" + isni + "> }";

    return (Resource) Utils.queryDoremus(sparql, "s");
  }


  public void isniEnrich(ISNIRecord isni) {
    this.addPropertyResource(OWL.sameAs, isni.uri);
    this.addPropertyResource(OWL.sameAs, isni.getViafURI());
    this.addPropertyResource(OWL.sameAs, isni.getMusicBrainzUri());
    this.addPropertyResource(OWL.sameAs, isni.getMuziekwebURI());
    this.addPropertyResource(OWL.sameAs, isni.getWikidataURI());

    String wp = isni.getWikipediaUri();
    String dp = isni.getDBpediaUri();

    if (wp == null) {
      wp = isni.getWikipediaUri("fr");
      dp = isni.getDBpediaUri("fr");
    }
    this.addPropertyResource(OWL.sameAs, dp);
    this.addPropertyResource(FOAF.isPrimaryTopicOf, wp);
  }
}
