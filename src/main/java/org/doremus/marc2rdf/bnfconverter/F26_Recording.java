package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class F26_Recording extends DoremusResource {
  private final List<Artist> producers;

  public F26_Recording(Record record, String identifier) {
    super(record, identifier);
    this.setClass(FRBROO.F26_Recording);

    // history
    record.getDatafieldsByCodePropagate(317, 'a').stream()
      .filter(x -> !x.equalsIgnoreCase("Enregistrement public"))
      .filter(x -> {
        x = x.toLowerCase();
        return x.contains("repiquage") || x.contains("enregistrement") || x.contains("enregistré");
      })
      .forEach(x -> this.addProperty(MUS.U15_has_history, x, "fr"));

    // producer rights
    String year = getRightYear();
    producers = record.getDatafieldsByCodePropagate(722).stream()
      .filter(df -> "3160".equals(df.getString(4)))
      .map(ArtistConverter::parseArtistField)
      .collect(Collectors.toList());
    producers.addAll(record.getDatafieldsByCodePropagate(732).stream()
      .filter(df -> "3160".equals(df.getString(4)))
      .map(CorporateBody::fromUnimarcField)
      .collect(Collectors.toList()));

    int i = 0;
    for (Artist p : producers) {
      String prodUri = this.uri + "/producer_right/" + ++i;
      ProducersRights pr = new ProducersRights(prodUri, p, year);
      this.addProperty(CIDOC.P104_is_subject_to, pr);
    }
    if (i == 0) {
      for (String x : record.getDatafieldsByCodePropagate(352, 'a')) {
        if (!x.toLowerCase().contains("prod.")) continue;
        String prodUri = this.uri + "/producer_right/" + ++i;
        ProducersRights pr = new ProducersRights(prodUri, x, year);
        producers.add(pr.getHolder());
        this.addProperty(CIDOC.P104_is_subject_to, pr);
      }
    }

    // id matrice
    for (DataField df : record.getDatafieldsByCodePropagate(270)) {
      String _id = df.getString('m');
      List<String> eb = Stream.of(df.getString('e'), df.getString('b'))
        .filter(Objects::nonNull).collect(Collectors.toList());
      if (eb.size() > 0) _id += " (" + String.join(" ", eb) + ")";
      this.addComplexIdentifier(_id, "Identifiant de matrice");
    }
  }

  private String getRightYear() {
    String txt = record.getDatafieldsByCodePropagate("044", 'f')
      .stream().findFirst().orElse(" ");
    if (txt.charAt(0) != 'c') return null;
    return txt.substring(1, 5);
  }

  public List<Artist> getProducers() {
    return producers;
  }
}
