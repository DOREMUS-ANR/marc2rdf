package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.isnimatcher.ISNIRecord;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.ISNIWrapper;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArtistConverter {
  public ArtistConverter(Record record, Model model) throws URISyntaxException {
    List<Person> artists = getArtistsInfo(record, true);

    if (artists.size() == 0) {
      System.out.println("Empty artist for record " + record.getIdentifier());
      return;
    }


    Person base = artists.get(0);

    if (artists.size() > 1) {
      for (Person artist : artists) {
        String lang = artist.getLang();
        if (lang == null || lang.equals("fr")) {
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
      base.addProperty(RDFS.label, artist.getFullName(), artist.getLang());
      base.addProperty(CIDOC.P131_is_identified_by, artist.getIdentification(), artist.getLang());
    }

    for (String[] names : getAlternateNames(record)) {
      String firstName = names[0], surname = names[1], lang = names[2];

      String fullName = surname;
      if (firstName != null) {
        String separator = firstName.endsWith("-") ? "" : " ";
        fullName = firstName + separator + surname;
      }

      base.addProperty(FOAF.firstName, firstName, lang);
      base.addProperty(FOAF.surname, surname, lang);
      base.addProperty(FOAF.name, fullName, lang);
      // NOTE: I intentionally did not add P131 and RDFS.label for distinguish alternate names
    }

    // birth - death places
    base.setBirthPlace(getPlace(record, true));
    base.setDeathPlace(getPlace(record, false));

    // sameAs links
    String isni = record.getDatafieldsByCode("031", 'a').stream().findFirst().orElse(null);
    try {
      ISNIRecord rec;
      if (isni != null) {
        base.asResource().addProperty(OWL.sameAs, model.createResource("http://isni.org/isni/" + isni));
        rec = ISNIWrapper.get(isni);
      } else {
        rec = ISNIWrapper.search(base.getFullName(), base.getBirthDate());
      }
      if (rec != null) base.isniEnrich(rec);
    } catch (IOException e) {
      e.printStackTrace();
    }


    String ark = record.getAttrByName("IDPerenne").getData();
    base.asResource().addProperty(OWL.sameAs, model.createResource("http://catalogue.bnf.fr/" + ark));
    // END sameAs links

    // identifier
    base.asResource().addProperty(DCTerms.identifier, record.getIdentifier());


    // PROV-O tracing
    Resource intermarcRes = model.createResource("http://catalogue.bnf.fr/" + ark + ".intermarc")
      .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, model.createResource(BNF2RDF.organizationURI));

    Resource provActivity = model.createResource(ConstructURI.build("bnf", "prov", record.getIdentifier()).toString())
      .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
      .addProperty(PROV.used, intermarcRes)
      .addProperty(RDFS.comment, "Reprise et conversion de la notice MARC de la BnF", "fr")
      .addProperty(RDFS.comment, "Resumption and conversion of the MARC record of the BnF", "en")
      .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);

    base.addProvenance(intermarcRes, provActivity);

    // END PROV-O tracing
    model.add(base.getModel());
  }


  private String getPlace(Record record, boolean birth) {
    DataField df = record.getDatafieldByCode(603);
    if (df == null) return null;

    char code = birth ? 'a' : 'b';
    return df.getString(code);
  }


  static List<Person> getArtistsInfo(Record record) {
    return getArtistsInfo(record, false);
  }

  static List<Person> getArtistsInfo(Record record, boolean full) {
    return record.getDatafieldsByCode("100").stream()
      .filter(field -> full || field.isCode('3'))
      .map(ArtistConverter::parseArtistField)
      .collect(Collectors.toList());
  }

  public static Person parseArtistField(DataField field) {
    String firstName = field.getString('m');
    String lastName = field.getString('a');
    if (firstName == null && lastName == null)
      return null;

    String birthDate = null;
    String deathDate = null;
    String lang = null;

    if (field.isCode('d')) { // birth - death dates
      String d = field.getString('d');

      // av. J.-C.
      d = d.replaceAll("av\\. J\\.?-C\\.?", "BC");
      String[] dates = d.split("[-–]");
      birthDate = dates[0].trim();
      if (dates.length > 1) deathDate = dates[1].trim();
    }

    if (field.isCode('w')) { // lang
      String w = field.getString('w');
      lang = Utils.intermarcExtractLang(w);
    }

    try {
      return new Person(firstName, lastName, birthDate, deathDate, lang);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }

  static List<String[]> getAlternateNames(Record record) {
    List<String[]> names = new ArrayList<>();
    for (DataField field : record.getDatafieldsByCode("400")) {

      String firstName = field.getString('m');
      String lastName = field.getString('a');

      String lang = null;
      if (field.isCode('w')) { // lang
        String w = field.getString('w');
        lang = Utils.intermarcExtractLang(w);
      }

      names.add(new String[]{firstName, lastName, lang});
    }
    return names;
  }
}

