package org.doremus.marc2rdf.main;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.Time;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

public class TimeSpan {
  public static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
    "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";
  public static final String frenchDayRegex = "(1er|[\\d]{1,2})";
  public static final String frenchMonthRegex = "(janvier|février|mars|avril|mai|juin|juillet|ao[ûu]t|septembre|octobre|novembre|décembre)";
  public static final String frenchDateRegex = "(?:le )?(?:" + frenchDayRegex + "? ?" + frenchMonthRegex + "? ?)?(\\d{4})";
  public static final String frenchDateRangeRegex = "(?:" + frenchDateRegex + "?-)" + frenchDateRegex;

  private static final String[] UNCERTAINITY_CHARS = ".-?".split("");
  private static final int lastYear = LocalDate.now().getYear() - 1;

  private final Model model;
  private Resource resource;
  private String uri;

  private String startYear, startMonth, startDay;
  private String endYear, endMonth, endDay;
  private String startDate, endDate;
  private String startTime;
  private XSDDatatype startType, endType;
  private Precision startQuality, endQuality;
  private String label;
  private boolean endRequired = true;

  public static TimeSpan emptyUncertain() {
    TimeSpan ts = new TimeSpan(null);
    ts.setQuality(Precision.UNCERTAINTY);
    ts.resource = ts.model.createResource(ts.uri)
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDFS.label, "?")
      .addProperty(CIDOC.P79_beginning_is_qualified_by, ts.startQuality.toString())
      .addProperty(CIDOC.P80_end_is_qualified_by, ts.endQuality.toString());

    return ts;
  }

  public boolean hasUri() {
    return this.uri != null;
  }


  public enum Precision {
    CERTAINTY("certain"),
    UNCERTAINTY("uncertain"),
    AFTER("after"),
    DECADE("precision at decade"),
    CENTURY("precision at century"),
    MILLENNIUM("precision at millennium");
    private final String text;

    Precision(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }


  public TimeSpan(String startYear) {
    this(startYear, null, null, null, null, null);
  }

  public TimeSpan(String startYear, String endYear) {
    this(startYear, null, null, endYear, null, null);
  }

  public TimeSpan(String year, String month, String day) {
    this(year, month, day, null, null, null);
  }

  public TimeSpan(String year, String month, String day, String time) {
    this(year, month, day, null, null, null);
    this.endRequired = false;
    if (time == null) return;

    time = time.trim();
    if (time.length() == 5) time += ":00";
    this.startTime = time;
  }

  public TimeSpan(String startYear, String startMonth, String startDay, String endYear, String endMonth, String endDay) {
    this.model = ModelFactory.createDefaultModel();
    this.resource = null;
    this.uri = null;
    this.label = null;

    this.startYear = safeString(startYear);
    this.startMonth = safeString(frenchMonthToNumber(startMonth));
    this.startDay = safeString(startDay);
    if (this.startDay.equals("1er")) this.startDay = "01";

    this.endYear = safeString(endYear);
    this.endMonth = safeString(frenchMonthToNumber(endMonth));
    this.endDay = safeString(endDay);

    // range: "de 1848 à 1854"
    if ((this.endYear == null || this.endYear.equals(this.startYear)) &&
      this.startYear.matches("de \\d{4} à \\d{4}")) {
      this.endYear = this.startYear.substring(3, 7);
      this.startYear = this.startYear.substring(10, 14);
    }

    // quality of the interval extremes
    this.startQuality = computePrecision(this.startYear);
    this.startYear = this.startYear.replaceAll("[-.?]", "0");

    this.endQuality = computePrecision(this.endYear);
    if (endQuality != null) {
      try {
        int x = Integer.parseInt(this.endYear.replaceAll("[-.?]", "9"));
        this.endYear = Math.min(x, lastYear) + "";
      } catch (NumberFormatException e) {

      }
    }
  }

  private Precision computePrecision(String date) {
    int sum = 0;
    for (String c : UNCERTAINITY_CHARS) {
      sum += StringUtils.countMatches(date, c);
    }
    switch (sum) {
      case 1:
        return Precision.DECADE;
      case 2:
        return Precision.CENTURY;
      case 3:
        return Precision.MILLENNIUM;
    }
    return null;
  }


  public void setQuality(Precision quality) {
    this.startQuality = quality;
    this.endQuality = quality;
  }

  public void setStartQuality(Precision quality) {
    this.startQuality = quality;
  }

  public void setEndQuality(Precision quality) {
    this.endQuality = quality;
  }

  public void setUri(String uri) {
    this.uri = uri;
    if (this.resource != null) this.resource = ResourceUtils.renameResource(this.resource, uri);
  }

  public void setStartMonth(String month) {
    if (month == null) month = "";
    month = month.trim();
    if (month.length() > 2) month = frenchMonthToNumber(month);
    else if (month.length() == 1) month = "0" + month;

    this.startMonth = month;
  }

  public void setStartDay(String day) {
    if (day == null) day = "";
    day = day.trim();

    if (day.equals("1er")) day = "01";
    else if (day.length() == 1) day = "0" + day;

    this.startDay = day;
  }

  public Literal getStart() {
    if (startDate == null) return null;
    return this.model.createTypedLiteral(startDate, startType);
  }

  private String computeLabel() {
    if (endDate.isEmpty() || Objects.equals(startDate, endDate)) return startDate;
    return startDate + "/" + endDate;
  }

  public boolean hasEndYear() {
    return !this.endYear.isEmpty();
  }

  private void initResource() {
    if (startYear.isEmpty() && endYear.isEmpty()) return;

    // there is at least one of the two year
    if (startYear.isEmpty()) {
      startYear = endYear.substring(0, 2) + "00"; //beginning of the century
      startQuality = Precision.UNCERTAINTY;
    }
    if (endRequired && endYear.isEmpty()) {
      endYear = startYear;
      endMonth = startMonth;
      endDay = startDay;
    }

    startType = XSDDateType.XSDgYear;
    startDate = startYear;
    if (!startMonth.isEmpty()) {
      startType = XSDDateType.XSDgYearMonth;
      startDate += "-" + startMonth;
    }
    if (!startDay.isEmpty()) {
      startType = XSDDateType.XSDdate;
      startDate += "-" + startDay;
    }

    endType = XSDDateType.XSDgYear;
    endDate = endYear;
    if (!endMonth.isEmpty()) {
      endType = XSDDatatype.XSDgYearMonth;
      endDate += "-" + endMonth;
    }
    if (!endDay.isEmpty()) {
      endType = XSDDateType.XSDdate;
      endDate += "-" + endDay;
    }

    this.label = computeLabel();

    if (this.startTime != null) {
      startType = XSDDatatype.XSDdateTime;
      startDate += "T" + this.startTime;
    }

    this.resource = this.model.createResource(this.uri)
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDF.type, Time.Interval)
      .addProperty(RDFS.label, this.label);

    Resource startInstant = makeInstant(startDate, startType);
    Resource endInstant = makeInstant(endDate, endType);

    if (startInstant != null) this.resource.addProperty(Time.hasBeginning, startInstant);
    if (endInstant != null) this.resource.addProperty(Time.hasEnd, endInstant);

    if (this.startQuality != null)
      this.resource.addProperty(CIDOC.P79_beginning_is_qualified_by, startQuality.toString());
    if (this.endQuality != null)
      this.resource.addProperty(CIDOC.P80_end_is_qualified_by, endQuality.toString());
  }

  private Resource makeInstant(String date, XSDDatatype type) {
    if (!date.matches(UCT_DATE_REGEX)) return null;

    return this.model.createResource()
      .addProperty(RDF.type, Time.Instant)
      .addProperty(Time.inXSDDate, this.model.createTypedLiteral(date, type));
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

  public String getLabel() {
    return label;
  }

  public static TimeSpan fromUnimarcField(DataField df) {
    return fromUnimarcField(df, null);
  }

  public static TimeSpan fromUnimarcField(DataField df, String time) {
    if (df == null || !df.isCode('a')) return null;

    // 981$a20110607
    String date = df.getString('a');
    if (!date.matches("\\d{8}")) return null;

    String y = date.substring(0, 4),
      m = date.substring(4, 6),
      d = date.substring(6);

    // 981$b20:30
    String t = df.getString('b');
    if (t != null) time = t;
    return new TimeSpan(y, m, d, time);
  }
}
