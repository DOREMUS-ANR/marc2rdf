package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.E53_Place;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PF31_Performance extends DoremusResource {
  private PF25_PerformancePlan performancePlan;
  private List<PM43_PerformedExpression> playedWorks;

  public PF31_Performance(Record record) {
    super(record);

    this.manageRelatedVideoConcert();

    this.resource.addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(CIDOC.P2_has_type, "concert")
      .addProperty(DC.identifier, getIdentifier());

    boolean withPublic = true;

    performancePlan = new PF25_PerformancePlan(record);
    this.add(performancePlan);

    this.playedWorks = new ArrayList<>();
    List<String> playedExpr = record.getDatafieldsByCode(449, '3');

    for (String track : getTracks()) {
      PM42_PerformedExpressionCreation m42 = new PM42_PerformedExpressionCreation(track);
      this.add(m42);
      m42.setConcert(this);
      performancePlan.add(m42);
      playedWorks.add(m42.getExpression());
      if (record.isType("UNI:2")) {
        for (String pe : playedExpr) m42.linkWorkById(pe);
        if (!m42.hasWorkLinked && this.includeAComposer())
          m42.linkNewWork(record);
        this.model.add(m42.getModel());
      }
      // I intentionally have not added the models: they will be in the UNI44 record
    }
    model.add(performancePlan.getModel());

    for (String title : record.getDatafieldsByCode(200, 'a')) {
      if (title.contains("sans public")) withPublic = false;

      title = title.trim();
      this.resource.addProperty(CIDOC.P102_has_title, title)
        .addProperty(RDFS.label, title);
    }

    record.getDatafieldsByCode(312, 'a').stream()
      .map(String::trim)
      .forEach(title -> this.resource.addProperty(MUS.U68_has_variant_title, title));


    for (String note : record.getDatafieldsByCode(200, 'e')) {
      if (note.contains("sans public")) withPublic = false;
    }

    for (PM28_Individual_Performance ip : PM42_PerformedExpressionCreation.parseArtist(record, this.uri)) {
      this.resource.addProperty(CIDOC.P9_consists_of, ip.asResource());
      this.model.add(ip.getModel());
    }

    this.resource.addProperty(MUS.U205_has_cast_detail, PM42_PerformedExpressionCreation.getCastDetail(record));

    PM42_PerformedExpressionCreation.getMuseeMusique(record).forEach(note ->
      this.resource.addProperty(MUS.U193_used_historical_instruments, note));


    parseCategorization(this);

    // generic notes
    getGenericNotes(record).forEach(this::addNote);

    // conditions
    if (record.isType("UNI:2"))
      this.resource.addProperty(MUS.U89_occured_in_performance_conditions, "en intérieur", "fr")
        .addProperty(MUS.U89_occured_in_performance_conditions, "dans les conditions du direct", "fr")
        .addProperty(MUS.U89_occured_in_performance_conditions, "en public", "fr");
    else if (record.isType("UNI:4"))
      this.resource.addProperty(MUS.U89_occured_in_performance_conditions, "en direct", "fr")
        .addProperty(MUS.U89_occured_in_performance_conditions, withPublic ? "en public" : "sans public", "fr");
  }

  static void parseCategorization(DoremusResource _this) {
    for (DataField df : _this.getRecord().getDatafieldsByCode(610)) {
      String value = df.getString('a').trim();
      if (df.hasSubfieldValue('b', "03")) { //descripteur géographique
        PM40_Context context = new PM40_Context(value);
        _this.getModel().add(context.getModel());
        _this.asResource().addProperty(MUS.U65_has_geographical_context, context.asResource());
      }

      _this.asResource().addProperty(MUS.U19_is_categorized_as, _this.getModel().createResource()
        .addProperty(RDF.type, MUS.M19_Categorization)
        .addProperty(RDFS.label, value));
    }
  }

  static Stream<String> getGenericNotes(Record record) {
    return record.getDatafieldsByCode(300, 'a').stream()
      .filter(n -> !n.contains("prise de son"))
      .filter(n -> !n.contains("enregistré par"))
      .filter(n -> !n.contains("musée de la musique"))
      .filter(n -> !n.contains("collection"));
  }

  private boolean includeAComposer() {
    return record.getDatafieldsByCode(701, 4).stream()
      .anyMatch(x -> x.equals("230"));
  }


  public PF31_Performance(String identifier) {
    super(identifier);
    this.performancePlan = null;
  }

  public PF31_Performance(String uri, String note, Model model) {
    super();
    this.performancePlan = null;
    this.model = model;
    this.regenerateResource(uri);
    this.resource.addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note);
  }

  public PF31_Performance add(PM42_PerformedExpressionCreation m42) {
    this.resource.addProperty(CIDOC.P9_consists_of, m42.asResource());
    return this;
  }

  public void add(PF22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R66_included_performed_version_of, f22.asResource());
  }

  public void add(PF25_PerformancePlan f25) {
    this.resource.addProperty(FRBROO.R25_performed, f25.asResource());
  }

  public void setPlace(E53_Place place) {
    this.resource.addProperty(CIDOC.P7_took_place_at, place.asResource());
  }

  public void setPlace(List<E53_Place> places) {
    for (E53_Place p : places) this.setPlace(p);
  }


  public void setTime(TimeSpan timeSpan) {
    if (timeSpan == null) {
      log("Missing time");
      return;
    }
    this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
  }

  public String getUri() {
    return uri;
  }

  private List<String> getTracks() {
    return record.getDatafieldsByCode(462, 3);
  }

  public List<PM43_PerformedExpression> getPlayedWorks() {
    return playedWorks;
  }

  public PF25_PerformancePlan getRelatedF25() {
    if (this.performancePlan == null)
      this.performancePlan = new PF25_PerformancePlan(getIdentifier(), true);
    return this.performancePlan;
  }

  private void manageRelatedVideoConcert() {
    List<String> rvc = record.getDatafieldsByCode(494, 3);
    if (rvc.isEmpty()) return;

    Property prop = rvc.size() > 1 ? CIDOC.P9_consists_of : OWL.sameAs;
    rvc.forEach(c ->
      this.resource.addProperty(prop, new PF31_Performance(c).asResource()));
  }

}
