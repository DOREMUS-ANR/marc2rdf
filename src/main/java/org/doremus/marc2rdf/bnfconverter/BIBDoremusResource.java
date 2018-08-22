package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

abstract class BIBDoremusResource extends DoremusResource {
  private int countResp = 0;

  public BIBDoremusResource(String identifier) {
    super(identifier);
  }

  public BIBDoremusResource(Record record) {
    super(record);
  }


  protected void parseTitleAndStatements(boolean forPublishing) {
    titleFromField(245, false).forEach(this::addTitle);
    titleFromField(247, false).forEach(this::addParallelTitle);

    titleFromField(750, true).forEach(title -> this.addProperty(MUS.U68_has_variant_title, title));
    titleFromField(751, true).forEach(title -> this.addProperty(MUS.U68_has_variant_title, title));

    // title statements
    titleFromField(245, true).forEach(title -> addTitleStatement(title, false, forPublishing));
    titleFromField(247, true).forEach(title -> addTitleStatement(title, true, forPublishing));
  }

  protected void parseResponsibilities(boolean forPublishing) {
    // title auxiliary info
    for (DataField dataField : record.getDatafieldsByCode(245)) {
      List<Literal> literals = extractResponsibilities(dataField, forPublishing);
      for (Literal literal : literals) {
        this.addStatement(this.uri + "/responsibility/" + ++countResp, literal, MUS.M157_Statement_of_Responsibility,
          MUS.U172_has_statement_of_responsibility_relating_to_title);
      }
    }
    for (DataField dataField : record.getDatafieldsByCode(247)) {
      List<Literal> literals = extractResponsibilities(dataField);
      for (Literal literal : literals) {
        this.addStatement(this.uri + "/responsibility/" + ++countResp, literal, MUS.M157_Statement_of_Responsibility,
          MUS.U173_has_parallel_statement_of_responsibility_relating_to_title);
      }
    }
  }

  protected void parseCastStatement() {
    // cast statement
    String note = record.getDatafieldsByCode(245, 'j').stream().findFirst().orElse(null);
    Stream<String> annotation = record.getDatafieldsByCode(313).stream()
      .filter(df -> df.isCode('k'))
      .map(df -> df.getString('k') + " : " + String.join(" ; " + df.getStrings('a')));
    this.addStatement(this.uri + "/cast/1", note, MUS.M155_Cast_Statement, MUS.U174_has_cast_statement, annotation);

    note = record.getDatafieldsByCode(247, 'j').stream().findFirst().orElse(null);
    this.addStatement(this.uri + "/cast/2", note, MUS.M155_Cast_Statement, MUS.U175_has_parallel_cast_statement);
  }

  private void addTitleStatement(Literal title, boolean asParallel, boolean forPublishing) {
    Resource titleStatement = model.createResource(this.uri + "/title")
      .addProperty(RDF.type, MUS.M156_Title_Statement)
      .addProperty(RDFS.comment, title);

    if (!asParallel && forPublishing) {
      List<String> notes = record.getDatafieldsByCode(300, 'a');
      notes.addAll(record.getDatafieldsByCode(350, 'a'));
      notes.stream()
        .filter(s -> s.toLowerCase().startsWith("titre"))
        .filter(s -> s.toLowerCase().contains("titre restitué") ||
          (!s.contains("compositeur") && !s.contains("interprète")))
        .forEach(s -> titleStatement.addProperty(MUS.U50_has_annotation, s));

      record.getDatafieldsByCode(750).stream()
        .filter(x -> x.isCode('k'))
        .map(x -> parseTitleField(x, true, true))
        .flatMap(List::stream)
        .forEach(s -> titleStatement.addProperty(MUS.U50_has_annotation, s));
    }
    this.addProperty(
      asParallel ? MUS.U171_has_parallel_title_statement : MUS.U170_has_title_statement,
      titleStatement);
  }

  protected List<Literal> parseTitleField(DataField df, boolean parseE, boolean parseK) {
    List<Literal> list = new ArrayList<>();

    char lastCode = 0;
    String currentTitle = null, language = null, currentK = null;

    for (Subfield sf : df.getSubfields()) {
      String value = sf.getData();
      switch (sf.getCode()) {
        case 'k':
          if (parseK) currentK = value;
          break;
        case 'a':
        case 'b':
        case 'c':
          if (currentTitle != null) {
            if (currentK != null) currentTitle = currentK + " \"" + currentTitle + "\"";
            currentK = null;
            list.add(model.createLiteral(currentTitle, language));
          }
          currentTitle = value
            .replaceAll("\\|", "")
            .replaceAll("\\.{3} ?\\[etc\\.] ?$", "")
            .replaceAll("^(?i)(suivi d[e']|pr[eèé]c[eèé]d[eèé][eèé]? d[e']|avec)", "")
            .replaceAll("\\s+", " ");
          break;
        case 'e':
          if (parseE)
            currentTitle += " : " + value;
          break;
        case 'h':
          currentTitle += ". " + value;
          break;
        case 'i':
          currentTitle += (lastCode == 'h' ? ", " : ". ") + value;
          break;
        case 'w':
          language = Utils.intermarcExtractLang(value);
      }
      lastCode = sf.getCode();
      if (currentTitle != null) {
        if (currentK != null) currentTitle = currentK + " \"" + currentTitle + "\"";
        list.add(model.createLiteral(currentTitle, language));
      }
    }
    return list;
  }

  protected Stream<Literal> titleFromField(int field, boolean parseE) {
    return titleFromField(record.getDatafieldsByCode(field), parseE);
  }

  private Stream<Literal> titleFromField(List<DataField> dfs, boolean parseE) {
    return dfs.stream()
      .map(df -> parseTitleField(df, parseE, false))
      .flatMap(List::stream);
  }


  private List<Literal> extractResponsibilities(DataField df) {
    return extractResponsibilities(df, false);
  }

  @SuppressWarnings("StringConcatenationInLoop")
  private List<Literal> extractResponsibilities(DataField df, boolean secondaryParsing) {
    List<Literal> list = new ArrayList<>();

    String language = null;
    String current = null;
    boolean secondaryDone = false;
    boolean aOk = false;
    for (Subfield sf : df.getSubfields()) {
      String value = sf.getData();
      switch (sf.getCode()) {
        case 'a':
          aOk = true;
          break;
        case 'd':
          break;
        case 'f':
          if (current != null) list.add(model.createLiteral(current, language));
          current = value;
          break;
        case 'g':
          current += ", " + value;
          if (secondaryParsing && aOk)
            if (!secondaryDone) {
              secondaryDone = true;
              this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
                MUS.U178_has_statement_of_responsibility_relating_to_edition);
            } else
              this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
                MUS.U179_has_parallel_statement_of_responsibility_relating_to_edition);

          break;
        case 'w':
          language = Utils.intermarcExtractLang(value);
        default:
          aOk = false;
      }
    }
    if (current != null) {
      list.add(model.createLiteral(current, language));
      if (secondaryParsing && aOk)
        if (!secondaryDone)
          this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
            MUS.U178_has_statement_of_responsibility_relating_to_edition);
        else
          this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
            MUS.U179_has_parallel_statement_of_responsibility_relating_to_edition);
    }
    return list;
  }

  protected void addStatement(String uri, Object text, OntClass _class, Property property) {
    addStatement(uri, text, _class, property, (Stream<String>) null);
  }

  protected void addStatement(String uri, Object text, OntClass _class, Property property, List<String> annotation) {
    addStatement(uri, text, _class, property, annotation.stream());
  }

  private void addStatement(String uri, Object text, OntClass _class, Property property, Stream<String> annotation) {
    if (text == null) return;
    Literal l = null;
    if (text instanceof String) l = model.createLiteral((String) text);
    else if (text instanceof Literal) l = (Literal) text;


    Resource resource = model.createResource(uri)
      .addProperty(RDF.type, _class)
      .addProperty(RDFS.comment, l);

    if (annotation != null)
      annotation.filter(Objects::nonNull).forEach(a -> resource.addProperty(MUS.U50_has_annotation, a));
    this.addProperty(property, resource);
  }

  public void addTitle(Literal title) {
    this.addProperty(MUS.U167_has_title_proper, title);
    this.addProperty(RDFS.label, title);
  }

  public void addParallelTitle(Literal title) {
    this.addProperty(MUS.U168_has_parallel_title, title);
  }


  protected void addComplexIdentifier(String identifier, String type, DoremusResource issuer) {
    Resource idRes = model.createResource(this.uri + "/id/" + identifier)
      .addProperty(RDF.type, CIDOC.E42_Identifier)
      .addProperty(CIDOC.P2_has_type, type)
      .addProperty(FRBROO.R8_consists_of, identifier)
      .addProperty(RDFS.label, identifier);

    if(issuer!=null) idRes.addProperty(FRBROO.R8_consists_of, issuer.asResource());

    this.addProperty(CIDOC.P1_is_identified_by, idRes);
  }
}
