package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ArtistConverter {
  private Model model;
  private Record record;


  public ArtistConverter(Record record, Model model) throws URISyntaxException {
    this.record = record;
    this.model = model;

    List<Person> artists = getArtistsInfo(record);

    if (artists.size() == 0) {
      System.out.println("Empty artist for record " + record.getIdentifier());
      return;
    }

    Person base = artists.get(0);

    if (artists.size() > 1) {
//      System.out.println("****** Multiple name of artist for record " + record.getIdentifier());

      for (Person artist : artists) {
        String lang = artist.getLang();
        if (lang == null || lang.equals("fre")) {
          base = artist;
          break;
        }
      }
    }

    Resource resource = model.createResource(base.getUri().toString());
    resource.addProperty(RDF.type, CIDOC.E21_Person);

    for (Person artist : artists) {

      String fullName = artist.getLastName();
      if (artist.getFirstName() != null) fullName = artist.getFirstName() + " " + artist.getLastName();
      if (artist.getLang() != null)
        resource.addProperty(CIDOC.P131_is_identified_by, model.createLiteral(fullName, artist.getLang()));
      else
        resource.addProperty(CIDOC.P131_is_identified_by, fullName);

      if (artist.getBirthDate() != null) {
        resource.addProperty(CIDOC.P98i_was_born, artist.getBirthDate());
      }
    }

    // sameAs links

    for (String isni : record.getDatafieldsByCode("031", 'a')) {
      resource.addProperty(OWL.sameAs, model.createResource("http://isni.org/isni/" + isni));
    }

    String ark = record.getAttrByName("IDPerenne").getData();
    resource.addProperty(OWL.sameAs, model.createResource("http://catalogue.bnf.fr/" + ark));

  }


  static List<Person> getArtistsInfo(Record record) throws URISyntaxException {
    List<Person> artists = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("100")) {
      String firstName = null, lastName = null, birthDate = null, lang = null;

      if (field.isCode('m')) { // name
        firstName = field.getSubfield('m').getData().trim();
      }
      if (field.isCode('a')) { // surname
        lastName = field.getSubfield('a').getData().trim();
      }
      if (field.isCode('d')) { // birth - death dates
        String d = field.getSubfield('d').getData();
        int split = d.indexOf("-");
        birthDate = (split > 0 ? d.substring(0, split) : d).trim();
      }
      if (field.isCode('w')) { // lang
        String w = field.getSubfield('w').getData();
        lang = w.substring(6, 9).replaceAll("\\.", "").trim();
        if (lang.length() == 0) lang = null;
      }

      artists.add(new Person(firstName, lastName, birthDate, lang));
    }
    return artists;
  }

}

