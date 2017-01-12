package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.ConstructURI;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Person {
  private URI uri;
  private String firstName, lastName, birthDate, lang;
  private List<String> sameAs;

  public Person(String firstName, String lastName, String birthDate) throws RuntimeException, URISyntaxException {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.sameAs = new ArrayList<>();

    if (lastName == null) {
      throw new RuntimeException("Missing artist value: null | " + lastName + " | " + birthDate);
    }

    this.uri = ConstructURI.build("E21_Person", firstName, lastName, birthDate);
  }

  public Person(String firstName, String lastName, String birthDate, String lang) throws URISyntaxException {
    this(firstName, lastName, birthDate);
    this.lang = lang;
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

  public String getLang() {
    return lang;
  }

  public void sameAs(String s) {
    sameAs.add(s);
  }

  public List<String> getSameAs() {
    return sameAs;
  }
}
