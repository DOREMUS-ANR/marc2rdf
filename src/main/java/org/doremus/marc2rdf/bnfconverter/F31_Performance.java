package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.E53_Place;
import org.doremus.marc2rdf.main.GeoNames;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class F31_Performance extends DoremusResource {
  private TimeSpan timeSpan;
  private E53_Place place;
  private E53_Place country;
  private E53_Place geoContext;

  public F31_Performance(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F31_Performance);
  }

  public F31_Performance(Record record) {
    this(record, record.getIdentifier());
  }

  public F31_Performance(Record record, String identifier) {
    super(record, identifier);
    this.setClass(FRBROO.F31_Performance);

    // in public
    List<String> notes = record.getDatafieldsByCode(317, 'a');
    notes.addAll(record.getDatafieldsByCode(317, 'a'));
    notes.stream()
      .filter(txt -> txt.toLowerCase().contains("enregistrement public"))
      .findAny()
      .ifPresent(x -> this.addProperty(MUS.U89_occured_in_performance_conditions, "en public", "fr"));

    this.timeSpan = record.getDatafieldsByCode(314).stream()
      .map(this::parseDate)
      .filter(Objects::nonNull)
      .findFirst().orElse(null);
    this.addTimeSpan(timeSpan);

    this.place = record.getDatafieldsByCode(314).stream()
      .map(this::parsePlace)
      .filter(Objects::nonNull)
      .findFirst().orElse(null);
    this.setPlace(this.place);

    // geo context
    record.getDatafieldsByCode(143, 'a').stream()
      .filter("Traditions"::equalsIgnoreCase)
      .findAny().ifPresent(any -> setGeoContext(this.country));
  }

  private E53_Place parsePlace(DataField df) {
    String country = df.getString('p');
    String city = df.getString('a');
    String building = df.getString('c');

    if ("s.l.".equalsIgnoreCase(city)) city = null;
    if ("s.l.".equalsIgnoreCase(building)) building = null;
    if ("s.l.".equalsIgnoreCase(country)) country = null;

    E53_Place pCountry = null, pCity = null, pBuilding;
    if (country != null) {
      pCountry = E53_Place.withUri(GeoNames.getCountryUri(country));
      if (pCountry == null) country = null;
    }
    if (city != null) {
      pCity = new E53_Place(city, country, null);
      pCity.addSurroundingPlace(pCountry);
    }
    if (building != null) {
      pBuilding = new E53_Place(building + (city != null ? " (" + city + ")" : ""), country, null);
      pBuilding.addSurroundingPlace(pCity);
      return pBuilding;
    }
    this.country = pCountry;
    return pCity != null ? pCity : pCountry;
  }

  private TimeSpan parseDate(DataField df) {
    if (!df.isCode('d')) return null;
    List<String> dates = df.getStrings('d').stream()
      .flatMap(txt -> {
        String year = txt.substring(0, 4);
        String month = "00", day = "00";
        if (txt.length() > 6) month = txt.substring(4, 6);
        if (txt.length() == 8) day = txt.substring(6);
        if (month.equals("00")) month = null;
        if (day.equals("00")) day = null;
        return Stream.of(year, month, day);
      }).collect(Collectors.toList());

    if (dates.size() == 0) return null;
    if (dates.size() == 6)
      return new TimeSpan(dates.get(0), dates.get(1), dates.get(2), dates.get(3), dates.get(4), dates.get(5));
    return new TimeSpan(dates.get(0), dates.get(1), dates.get(2));
  }

  public TimeSpan getTime() {
    return this.timeSpan;
  }

  public E53_Place getPlace() {
    return this.place;
  }

  public void setPlace(E53_Place place) {
    this.addProperty(CIDOC.P7_took_place_at, place);
  }

  private void setGeoContext(E53_Place place) {
    this.addProperty(MUS.U65_has_geographical_context, place);
    this.geoContext = this.country;
  }

  public E53_Place getGeoContext() {
    return this.geoContext;
  }

  public F31_Performance add(M42_PerformedExpressionCreation m42_performedExpressionCreation) {
    this.addProperty(CIDOC.P9_consists_of, m42_performedExpressionCreation);
    return this;
  }

  public F31_Performance add(F25_PerformancePlan plan) {
    this.addProperty(FRBROO.R25_performed, plan);
    return this;
  }

  public F31_Performance add(F22_SelfContainedExpression f22_selfContainedExpression) {
    this.addProperty(FRBROO.R66_included_performed_version_of, f22_selfContainedExpression);
    return this;
  }

  public boolean hasTimeSpace() {
    return this.timeSpan != null || this.place != null;
  }

  public boolean hasInformation() {
    boolean result = hasTimeSpace();
    for (DataField ignored : record.getDatafieldsByCodePropagate(111)) result = true;
    for (DataField ignored : record.getDatafieldsByCodePropagate(711)) result = true;
    for (DataField ignored : record.getDatafieldsByCodePropagate(101)) result = true;
    for (DataField ignored : record.getDatafieldsByCodePropagate(701)) result = true;
    return result;
  }
}
