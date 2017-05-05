package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ArtistConverter {

  public ArtistConverter(Record record, Model model) throws URISyntaxException {
    List<Person> artists = getArtistsInfo(record, true);

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


    base.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, model.createResource("http://data.doremus.org/organization/DOREMUS"))
      .addProperty(PROV.wasDerivedFrom, intermarcRes)
      .addProperty(PROV.wasGeneratedBy, provActivity);

    // END PROV-O tracing

    model.add(base.getModel());
  }


  static List<Person> getArtistsInfo(Record record) throws URISyntaxException {
    return getArtistsInfo(record, false);
  }

  static List<Person> getArtistsInfo(Record record, boolean full) throws URISyntaxException {
    List<Person> artists = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("100")) {
      if (!full && !field.isCode('3')) continue;

      String firstName = null, lastName = null, birthDate = null, deathDate = null, lang = null;

      if (field.isCode('m')) { // name
        firstName = field.getSubfield('m').getData().trim();
      }
      if (field.isCode('a')) { // surname
        lastName = field.getSubfield('a').getData().trim();
      }
      if (field.isCode('d')) { // birth - death dates
        String d = field.getSubfield('d').getData();

        // av. J.-C.
        d = d.replaceAll("av\\. J\\.-C\\.", "BC");
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

