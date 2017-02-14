package org.doremus.marc2rdf.main;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.CIDOC;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeSpan {
  private static final RDFDatatype W3CDTF = TypeMapper.getInstance().getSafeTypeByName(DCTerms.getURI() + "W3CDTF");

  private final Model model;
  private Resource resource;
  private String uri;

  private String startYear, startMonth, startDay;
  private String endYear, endMonth, endDay;
  private boolean approximative;

  public TimeSpan(String startYear) {
    this(startYear, null, null, null, null, null);
  }

  public TimeSpan(String startYear, String startMonth, String startDay, String endYear, String endMonth, String endDay) {
    this.startYear = safeString(startYear);
    this.startMonth = safeString(startMonth);
    this.startDay = safeString(startDay);

    this.endYear = safeString(endYear);
    this.endMonth = safeString(endMonth);
    this.endDay = safeString(endDay);

    this.approximative = false;

    this.model = ModelFactory.createDefaultModel();
    this.resource = null;
    this.uri = null;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  private void initResource() {
    if (startYear.isEmpty() && endYear.isEmpty()) return;

    // there is at least one of the two year
    if (startYear.isEmpty()) startYear = endYear.substring(0, 2) + "00"; //beginning of the century
    if (endYear.isEmpty()) {
      endYear = startYear;
      endMonth = startMonth;
      endDay = startDay;
    }

    if (startMonth.isEmpty()) startMonth = "01";
    if (startDay.isEmpty()) startDay = "01";
    if (endMonth.isEmpty()) endMonth = "12";
    if (endDay.isEmpty()) {
      endDay = getLastDay(endMonth, endYear);
      if (endDay == null) {
        System.out.println("Cannot get last day of: " + endMonth + "/" + endYear);
        return;
      }
    }

    String date = startYear + startMonth + startDay + "/" + endYear + endMonth + endDay;

    Property timeSpanProp = this.approximative ? CIDOC.P81_ongoing_throughout : CIDOC.P82_at_some_time_within;

    this.resource = this.model.createResource(this.uri)
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(timeSpanProp, ResourceFactory.createTypedLiteral(date, W3CDTF));
  }


  public Resource asResource() {
    if (this.resource == null) initResource();
    return this.resource;
  }

  private String safeString(String input) {
    if (input == null) return "";
    return input.trim();
  }

  public static String getLastDay(String month, String year) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
      Date convertedDate = dateFormat.parse(year + month + "01");
      Calendar c = Calendar.getInstance();
      c.setTime(convertedDate);
      c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
      return c.get(Calendar.DAY_OF_MONTH) + "";
    } catch (ParseException pe) {
      pe.printStackTrace();
      return null;
    }
  }

  public Model getModel() {
    return model;
  }
}
