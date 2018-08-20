package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class M54_Label_Name extends DoremusResource {
  private final String label;

  public M54_Label_Name(DataField df) {
    // Datafield 723
    super(df.getString(3));

    this.setClass(MUS.M154_Label_Name);

    String name = df.getString('a');
    this.label = name;
    if (df.isCode('b')) name += ". " + df.getString('b');
    this.addProperty(RDFS.label, name);


    String note = String.join(", ",
      Stream.of(df.getString('d'), df.getString('q'))
        .filter(Objects::nonNull).collect(Collectors.toList()));
    if (!note.isEmpty()) {
      this.addNote(note);
      name += " (" + note + ")";
    }

    this.addProperty(CIDOC.P1_is_identified_by, name);
  }

  public String getLabel() {
    return label;
  }
}
