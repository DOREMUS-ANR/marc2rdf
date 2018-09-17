package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.util.List;
import java.util.regex.Matcher;

public class F32_Carrier_Production_Event extends DoremusResource {
  private boolean hasInformation;
  private String place;
  private String agent;

  public F32_Carrier_Production_Event(Record record) {
    super(record);
    this.setClass(FRBROO.F32_Carrier_Production_Event);
    this.hasInformation = false;

    parse270();

    List<DataField> printers = record.getDatafieldsByCode(727);
    printers.addAll(record.getDatafieldsByCode(737));
    printers.stream()
      .filter(df -> "3050".equals(df.getString(4)))
      .forEach(df -> {
        Artist printer = (df.getEtiq().equals("727")) ?
          Person.fromIntermarcField(df) :
          CorporateBody.fromIntermarcField(df);

        if (printer == null) return;

        if (this.place != null) printer.addResidence(this.place);

        String function = "imprimeur";
        if ("3310".equals(df.getString(4))) function = "graveur de musique";

        this.hasInformation = true;
        this.addActivity(printer, function);
      });

    if (this.activityCount == 0) {
      Artist printer = Artist.fromString(this.agent, true);
      if (this.place != null) printer.addResidence(this.place);
      this.addActivity(printer, "imprimeur");
    }
  }

  private void parse270() {
    DataField df = record.getDatafieldByCode(270);
    if (df == null) return;

    String a = df.getString('a');
    String c = df.getString('c');
    String d = df.getString('d');

    if (c != null) {
      this.hasInformation = true;
      if (a != null && !a.isEmpty() && !a.equalsIgnoreCase("s.l."))
        this.place = a;
      this.agent = c;
    }

    if (d != null) {
      Matcher m = TimeSpan.YEAR_PATTERN.matcher(d);
      if (m.find()) {
        this.hasInformation = true;
        this.addTimeSpan(new TimeSpan(m.group(0)));
      }
    }
  }

  public boolean hasInformation() {
    return hasInformation;
  }

  public F32_Carrier_Production_Event add(F3_ManifestationProductType manif) {
    this.addProperty(FRBROO.R26_produced_things_of_type, manif);
    return this;
  }
}
