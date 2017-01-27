package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.CIDOC;

import java.net.URI;
import java.net.URISyntaxException;

public class Person {
  private final Model model;
  private Resource resource;

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
    addProperty(CIDOC.P131_is_identified_by, this.getIdentification(), lang);
    addProperty(CIDOC.P98i_was_born, this.getBirthDate());
    addProperty(CIDOC.P100i_died_in, this.getDeathDate());
    return resource;
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

}
