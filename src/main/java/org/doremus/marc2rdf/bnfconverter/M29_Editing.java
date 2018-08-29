package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M29_Editing extends DoremusResource {
  private static final Pattern SPARS_PATTERN = Pattern.compile("[AD]{3}");

  public M29_Editing(Record record) {
    super(record);
    this.setClass(MUS.M29_Editing);

    parse280c();
  }

  private void parse280c() {
    List<String> field280 = record.getDatafieldsByCode(280, 'c');
    if (field280.size() == 0) return;

    String field280c = field280.get(0);
    Matcher m = SPARS_PATTERN.matcher(field280c);
    if (m.find()) {
      String SPARS = m.group(0);
      this.addProperty(MUS.U219_used_object_of_type_for_mixing, String.valueOf(SPARS.charAt(1)));
      this.addProperty(MUS.U218_used_object_of_type_for_mastering, String.valueOf(SPARS.charAt(2)));
    }
    field280c = field280c.toLowerCase();
    String spatialization = "stereo";
    if (field280c.contains("mono")) spatialization = "mono";
    else if (field280c.contains("quadriphonie")) spatialization = "quadriphonie";
    this.addProperty(MUS.U225_used_sound_spatialization_technique, spatialization);

    if (field280c.contains("dolby") || field280c.contains("dbx"))
      this.addProperty(MUS.U192_used_noise_reduction_technique, "Dual-ended systems", "en");
  }

  public M29_Editing add(M46_SetOfTracks tracks) {
    this.addProperty(FRBROO.R17_created, tracks);
    return this;
  }

//  public M29_Editing add(F29_RecordingEvent recordingEvt) {
//    this.resource.addProperty(MUS.U29_edited, recordingEvt.getRecording().asResource());
//    return this;
//  }
//
//  public M29_Editing add(F4_ManifestationSingleton support) {
//    this.resource.addProperty(FRBROO.R18_created, support.asResource());
//    return this;
//  }
}
