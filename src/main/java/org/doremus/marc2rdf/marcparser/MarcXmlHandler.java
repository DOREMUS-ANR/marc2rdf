package org.doremus.marc2rdf.marcparser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.HashMap;

/*************
 * Met chaque objet en haut de la pile
 ********************/

public class MarcXmlHandler implements ContentHandler {

  private RecordList list;
  private StringBuffer buffer;
  private Subfield subfield;
  private DataField dataField;
  private ControlField controlField;
  private Record record;

  /******
   * Constantes representant chaque type de balise
   ******/
  private static final int RECORD = 1;
  private static final int CONTROLFIELD = 2;
  private static final int DATAFIELD = 3;
  private static final int SUBFIELD = 4;

  private String ETIQ;
  private String CODE;
  private String TYPE;
  private String ID;
  private String IND1;
  private String IND2;

  private boolean useControlField;

  private final HashMap<String, Integer> elementMap;
  // Hashset for mapping of element labels to types

  private boolean bDATA;

  /*****************************
   * Constructeur
   **************************************/
  public MarcXmlHandler(String recordLabel, String datafieldLabel, String subfieldLabel,
                        String tagLabel, String codeLabel, String typeLabel, String idlabel) {
    this.list = new RecordList();

    elementMap = new HashMap<>();
    elementMap.put(recordLabel, RECORD);
    elementMap.put(datafieldLabel, DATAFIELD);
    elementMap.put(subfieldLabel, SUBFIELD);

    this.ETIQ = tagLabel;
    this.CODE = codeLabel;
    this.TYPE = typeLabel;
    this.ID = idlabel;

    this.useControlField = false;
  }

  public MarcXmlHandler(String recordLabel, String datafieldLabel, String subfieldLabel, String controlfieldLabel,
                        String tagLabel, String codeLabel, String typeLabel, String idlabel) {
    this(recordLabel, datafieldLabel, subfieldLabel, tagLabel, codeLabel, typeLabel, idlabel);

    elementMap.put(controlfieldLabel, CONTROLFIELD);
    this.IND1 = "ind1";
    this.IND2 = "ind2";
    this.useControlField = true;
  }

  public RecordList getList() {
    return this.list;
  }

  /*********************************************************************************/
  public void startElement(String uri, String name, String qName, Attributes attrs) throws SAXException {

    String realName = (name.length() == 0) ? qName : name;
    Integer elementType = elementMap.get(realName);

    if (elementType == null) return;
    bDATA = CONTROLFIELD == elementType || SUBFIELD == elementType;

    switch (elementType) {
      case RECORD:
        String typeNotice = attrs.getValue(TYPE);
        String idNotice = attrs.getValue(ID);
        this.record = new Record(typeNotice, idNotice);
        break;
      case CONTROLFIELD:
        buffer = new StringBuffer();
        String etiqCode = attrs.getValue(ETIQ);
        controlField = new ControlField(etiqCode);
        break;
      case DATAFIELD:
        etiqCode = attrs.getValue(ETIQ);

        if (useControlField) {
          String ind1 = attrs.getValue(IND1);
          String ind2 = attrs.getValue(IND2);

          if (ind1.isEmpty()) ind1 = " ";
          if (ind2.isEmpty()) ind2 = " ";
          dataField = newDataField(etiqCode, ind1.charAt(0), ind2.charAt(0));
        } else {
          dataField = newDataField(etiqCode);
        }

        break;
      case SUBFIELD:
        buffer = new StringBuffer();
        String code = attrs.getValue(CODE);

        if (!useControlField) {
          //On récupère que ce qui est après le $
          // Example : code = "017$a", on doit récupérer que ce qui est après le $
          code = code.substring(code.indexOf("$") + 1);
        }

        if (code == null || code.isEmpty())
          subfield = null;
        else
          subfield = new Subfield(code.charAt(0));
    }
  }

  /*********************************************************************************/
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (bDATA)
      buffer.append(new String(ch, start, length)); // Récupérer le contenu de la balise "data"
  }

  /*********************************************************************************/
  public void endElement(String uri, String name, String qName) throws SAXException {
    String realName = (name.length() == 0) ? qName : name;
    Integer elementType = elementMap.get(realName);
    if (elementType == null) return;

    switch (elementType) {
      case RECORD:
        list.push(this.record);
        break;
      case CONTROLFIELD:
        controlField.setData(buffer.toString());
        record.addControlField(controlField);
        break;
      case DATAFIELD:
        if(this.dataField == null) return;
        record.addDataField(dataField);
        break;
      case SUBFIELD:
        if (subfield == null) return;
        subfield.setData(buffer.toString());
        dataField.addSubfield(subfield);
    }
  }

  /*********************************************************************************/
  public void endDocument() throws SAXException {
    list.end();
  }

  /*********************************************************************************/
  private DataField newDataField(String tag, char ind1, char ind2, String... subfieldCodesAndData) {
    DataField df = new DataField(tag, ind1, ind2);

    for (int i = 0; i < subfieldCodesAndData.length; i += 2) {
      Subfield sf = new Subfield(subfieldCodesAndData[i].charAt(0), subfieldCodesAndData[i + 1]);
      df.addSubfield(sf);
    }
    return (df);
  }

  /*********************************************************************************/
  private DataField newDataField(String tag, String... subfieldCodesAndData) {
    DataField df = new DataField(tag);

    for (int i = 0; i < subfieldCodesAndData.length; i += 2) {
      Subfield sf = new Subfield(subfieldCodesAndData[i].charAt(0), subfieldCodesAndData[i + 1]);
      df.addSubfield(sf);
    }
    return (df);
  }


  /*********************************************************************************/
  public void startDocument() throws SAXException {
  }

  public void ignorableWhitespace(char[] data, int offset, int length) throws SAXException {
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
  }

  public static class MarcXmlHandlerBuilder {
    private String recordLabel;
    private String datafieldLabel;
    private String subfieldLabel;
    private String controlfieldLabel;
    private String tagLabel;
    private String codeLabel;
    private String typeLabel;
    private String idlabel;

    public MarcXmlHandlerBuilder() {
      this.recordLabel = "record";
      this.datafieldLabel = "datafield";
      this.subfieldLabel = "subfield";
      this.controlfieldLabel = "controlfield";
      this.tagLabel = "tag";
      this.codeLabel = "code";
      this.typeLabel = "type";
      this.idlabel = "id";
    }

    public MarcXmlHandler build() {
      if (controlfieldLabel != null)
        return new MarcXmlHandler(recordLabel, datafieldLabel, subfieldLabel, controlfieldLabel,
          tagLabel, codeLabel, typeLabel, idlabel);

      return new MarcXmlHandler(recordLabel, datafieldLabel, subfieldLabel, tagLabel, codeLabel, typeLabel, idlabel);
    }

    public MarcXmlHandlerBuilder recordLabel(String recordLabel) {
      this.recordLabel = recordLabel;
      return this;
    }

    public MarcXmlHandlerBuilder datafieldLabel(String datafieldLabel) {
      this.datafieldLabel = datafieldLabel;
      return this;

    }

    public MarcXmlHandlerBuilder subfieldLabel(String subfieldLabel) {
      this.subfieldLabel = subfieldLabel;
      return this;

    }

    public MarcXmlHandlerBuilder controlfieldLabel(String controlfieldLabel) {
      this.controlfieldLabel = controlfieldLabel;
      return this;

    }

    public MarcXmlHandlerBuilder controlfieldLabel(boolean on) {
      // switch off controlfield
      if (!on) this.controlfieldLabel = null;
      return this;

    }

    public MarcXmlHandlerBuilder tagLabel(String tagLabel) {
      this.tagLabel = tagLabel;
      return this;

    }

    public MarcXmlHandlerBuilder codeLabel(String codeLabel) {
      this.codeLabel = codeLabel;
      return this;

    }

    public MarcXmlHandlerBuilder typeLabel(String typeLabel) {
      this.typeLabel = typeLabel;
      return this;

    }

    public MarcXmlHandlerBuilder idlabel(String idlabel) {
      this.idlabel = idlabel;
      return this;

    }
  }
}
