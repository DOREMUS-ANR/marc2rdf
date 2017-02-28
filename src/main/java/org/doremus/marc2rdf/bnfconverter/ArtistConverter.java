package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
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

  public ArtistConverter(Record record, Model model) throws URISyntaxException {
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


    for (Person artist : artists) {
      if (artist == base) continue;

      base.addProperty(FOAF.firstName, artist.getFirstName(), artist.getLang());
      base.addProperty(FOAF.surname, artist.getLastName(), artist.getLang());
      base.addProperty(FOAF.name, artist.getFullName(), artist.getLang());
      base.addProperty(CIDOC.P131_is_identified_by, artist.getIdentification(), artist.getLang());
    }

    // sameAs links

    for (String isni : record.getDatafieldsByCode("031", 'a'))
      base.asResource().addProperty(OWL.sameAs, model.createResource("http://isni.org/isni/" + isni));

    String ark = record.getAttrByName("IDPerenne").getData();
    base.asResource().addProperty(OWL.sameAs, model.createResource("http://catalogue.bnf.fr/" + ark));

    model.add(base.getModel());
  }


  static List<Person> getArtistsInfo(Record record) throws URISyntaxException {
    List<Person> artists = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("100")) {
      String firstName = null, lastName = null, birthDate = null, deathDate = null, lang = null;

      if (field.isCode('m')) { // name
        firstName = field.getSubfield('m').getData().trim();
      }
      if (field.isCode('a')) { // surname
        lastName = field.getSubfield('a').getData().trim();
      }
      if (field.isCode('d')) { // birth - death dates
        String d = field.getSubfield('d').getData();
        String[] dates = d.split("-");
        birthDate = dates[0].trim();
        if (dates.length > 1) deathDate = dates[1].trim();
      }
      if (field.isCode('w')) { // lang
        String w = field.getSubfield('w').getData();
        if (w.length() > 9)
          lang = w.substring(6, 9).replaceAll("\\.", "").trim();
        if (lang != null && lang.length() == 0) lang = null;
      }

      artists.add(new Person(firstName, lastName, birthDate, deathDate, lang));
    }
    return artists;
  }

}

