package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class PM43_PerformedExpression extends DoremusResource {
  public PM43_PerformedExpression(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, MUS.M43_Performed_Expression);
  }

  public PM43_PerformedExpression(Record record) {
    super(record);
    this.resource.addProperty(RDF.type, MUS.M43_Performed_Expression);

    String rdaType = PP2RDF.guessType(record);
    if (rdaType != null)
      this.resource.addProperty(MUS.U227_has_content_type, rdaType, "en");


    for (String title : record.getDatafieldsByCode(200, 'a'))
      this.resource.addProperty(RDFS.label, title.trim())
        .addProperty(CIDOC.P102_has_title, title.trim());

    for (DataField df : record.getDatafieldsByCode(610)) {
      if ("03".equals(df.getString('b')))
        this.resource.addProperty(MUS.U65_has_geographical_context, df.getString('a'));
    }

  }
}
