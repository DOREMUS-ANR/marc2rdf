package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;

import java.net.URI;
import java.net.URISyntaxException;

public class Person {
  private Resource resource;
  private final Model model;

  private URI uri;
  private String firstName, lastName, birthDate, deathDate, lang;

  public Person(String firstName, String lastName, String birthDate, String deathDate, String lang) throws RuntimeException, URISyntaxException {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.deathDate = deathDate;
    this.lang = lang;
    this.model = ModelFactory.createDefaultModel();

    if (lastName == null) {
      throw new RuntimeException("Missing artist value: null | " + lastName + " | " + birthDate);
    }

    this.uri = ConstructURI.build("E21_Person", firstName, lastName, birthDate);
    initResource();
  }

  public Person(String firstName, String lastName, String birthDate) throws RuntimeException, URISyntaxException {
    this(firstName, lastName, birthDate, null, null);
  }

  public Person(String firstName, String lastName, String birthDate, String lang) throws URISyntaxException {
    this(firstName, lastName, birthDate, null, lang);
  }

  public URI getUri() {
    return uri;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public String getDeathDate() {
    if (deathDate == null || deathDate.replaceAll("\\.", "").isEmpty())
      return null;
    return deathDate;
  }

  public String getLang() {
    return lang;
  }

  public Model getModel() {
    return model;
  }

  public Resource asResource() {
    return this.resource;
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
    this.resource = model.createResource(this.getUri().toString());
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
    TimeSpan ts = cleanDate(date);
    if (ts == null) return;

    Property schemaProp = model.createProperty(Converter.SCHEMA + (isDeath ? "deathDate" : "birthDate"));

    String url = this.uri + (isDeath ? "/death" : "/birth");
    ts.setUri(url + "/interval");
    addProperty(isDeath ? CIDOC.P100i_died_in : CIDOC.P98i_was_born,
      model.createResource(url)
        .addProperty(RDF.type, isDeath ? CIDOC.E69_Death : CIDOC.E67_Birth)
        .addProperty(CIDOC.P4_has_time_span, ts.asResource())
    );

    if (ts.getStart() != null)
      this.resource.addProperty(schemaProp, ts.getStart());

    model.add(ts.getModel());
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
      d.replaceFirst("^ca", "").trim();
    }
    if (d.startsWith("après")) {
      after = true;
      d.replaceFirst("^après", "").trim();
    }
    // "850?" is 850-uncertain, not 8500-precision at decade
    if (!d.startsWith("1") && d.length() > 3 && d.charAt(3) == '?') {
      uncertain = true;
      d = d.substring(0, 3);
    }
    if (d.replaceFirst("^-", "").length() > 4)
      // I have the info on the end date!
      ts = new TimeSpan(d.substring(0, 4), d.substring(4));
    else ts = new TimeSpan(d);

    if (uncertain) ts.setQuality(TimeSpan.Precision.UNCERTAINTY);
    if (after) ts.setQuality(TimeSpan.Precision.AFTER);

    return ts;
  }
}
