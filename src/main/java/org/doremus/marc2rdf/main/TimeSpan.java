package org.doremus.marc2rdf.main;

import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeSpan {
  public static final String frenchDayRegex = "(1er|[\\d]{1,2})";
  public static final String frenchMonthRegex = "(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)";
  public static final String frenchDateRegex = "(?:le )?" + frenchDayRegex + "? ?"+frenchMonthRegex+"? ?(\\d{4})";
  public static final String frenchDateRangeRegex = "(?:" + frenchDateRegex + "?-)" + frenchDateRegex;

  private final Model model;
  private Resource resource;
  private String uri;

  private String startYear, startMonth, startDay;
  private String endYear, endMonth, endDay;
  private String label;

  public TimeSpan(String startYear) {
    this(startYear, null, null, null, null, null);
  }

  public TimeSpan(String startYear, String endYear) {
    this(startYear, null, null, endYear, null, null);
  }

  public TimeSpan(String year, String month, String day) {
    this(year, month, day, null, null, null);
  }

  public TimeSpan(String startYear, String startMonth, String startDay, String endYear, String endMonth, String endDay) {
    this.startYear = safeString(startYear);
    this.startMonth = safeString(frenchMonthToNumber(startMonth));
    this.startDay = safeString(startDay);

    this.endYear = safeString(endYear);
    this.endMonth = safeString(frenchMonthToNumber(endMonth));
    this.endDay = safeString(endDay);

    // range: "de 1848 à 1854"
    if ((this.endYear == null || this.endYear.equals(this.startYear)) &&
      this.startYear.matches("de \\d{4} à \\d{4}")) {
      this.endYear = this.startYear.substring(3, 7);
      this.startYear = this.startYear.substring(10, 14);
    }

    this.model = ModelFactory.createDefaultModel();
    this.resource = null;
    this.uri = null;
    this.label = null;
  }


  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setStartMonth(String month) {
    if (month == null) return;
    month = month.trim();
    if (month.length() > 2) month = frenchMonthToNumber(month);
    else if (month.length() < 2) month = "0" + month;

    this.startMonth = month;
  }

  public void setStartDay(String day) {
    if (day == null) return;
    day = day.trim();

    if (day.equals("1er")) day = "01";
    else if (day.length() < 2) day = "0" + day;

    this.startDay = day;
  }

  private void computeLabel() {
    if (startYear.isEmpty()) label = endYear.substring(0, 2) + "00"; //beginning of the century
    else label = startYear;

    // avoid "1915/1915" ==> "1915"
    if (startMonth.isEmpty() && endMonth.isEmpty() && startYear.equals(endYear)) return;

    if (!startMonth.isEmpty()) label += "-" + startMonth;
    if (!startDay.isEmpty()) label += "-" + startDay;

    if (!endYear.isEmpty() || !endMonth.isEmpty() || !endDay.isEmpty())
      label += "/";
    if (!endYear.isEmpty()) label += endYear;
    if (!endMonth.isEmpty()) label += "-" + endMonth;
    if (!endDay.isEmpty()) label += "-" + endDay;
  }

  private void initResource() {
    if (startYear.isEmpty() && endYear.isEmpty()) return;
    if (this.label == null) computeLabel();

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

    String startDate = startYear + "-" + startMonth + "-" + startDay;
    String endDate = endYear + "-" + endMonth + "-" + endDay;

    this.resource = this.model.createResource(this.uri)
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDFS.label, this.label)
      .addProperty(CIDOC.P79_beginning_is_qualified_by, this.model.createTypedLiteral(startDate, XSDDateType.XSDdate))
      .addProperty(CIDOC.P80_end_is_qualified_by, this.model.createTypedLiteral(endDate, XSDDateType.XSDdate));
  }


  public Resource asResource() {
    if (this.resource == null) initResource();
    return this.resource;
  }

  private String safeString(String input) {
    if (input == null) return "";
    input = input.trim();
    if (input.length() == 1) input = "0" + input;
    return input;
  }

  private static String getLastDay(String month, String year) {
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

  @SuppressWarnings("SpellCheckingInspection")
  private static String frenchMonthToNumber(String month) {
    if (month == null || month.isEmpty()) return null;
    if (month.length() == 2) return month;

    String mm = month.toLowerCase();
    switch (mm) {
      case "janvier":
        return "01";
      case "février":
        return "02";
      case "mars":
        return "03";
      case "avril":
        return "04";
      case "mai":
        return "05";
      case "juin":
        return "06";
      case "juillet":
        return "07";
      case "août":
        return "08";
      case "septembre":
        return "09";
      case "octobre":
        return "10";
      case "novembre":
        return "11";
      case "décembre":
        return "12";
      default:
        return null;
    }
  }
}
