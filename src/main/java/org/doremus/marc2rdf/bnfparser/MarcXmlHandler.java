package org.doremus.marc2rdf.bnfparser;

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
  private String etiqCode;
  private String typeNotice;
  private String idNotice;

  /******
   * Constantes representant chaque type de balise
   ******/

  private static final int RECORD_ID = 2;
  private static final int CONTROLFIELD_ID = 4;
  private static final int DATAFIELD_ID = 5;
  private static final int SUBFIELD_ID = 6;

  private static final String etiqAttr = "tag";
  private static final String codeAttr = "code";
  private static final String IND1Attr = "ind1";
  private static final String IND2Attr = "ind2";
  private static final String typeAttr = "type";
  private static final String idAttr = "id";

  private static final HashMap<String, Integer> elementMap; // Hashset for mapping of element strings to constants (Integer)

  static {
    elementMap = new HashMap<>();
    elementMap.put("record", RECORD_ID);
    elementMap.put("controlfield", CONTROLFIELD_ID);
    elementMap.put("datafield", DATAFIELD_ID);
    elementMap.put("subfield", SUBFIELD_ID);
  }

  boolean bDATA = false;

  /*****************************
   * Constructeur
   **************************************/
  public MarcXmlHandler(RecordList queue) {
    this.list = queue;
  }

  /*********************************************************************************/
  public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException {

    String realName = (name.length() == 0) ? qName : name;
    Integer elementType = elementMap.get(realName);

    bDATA = qName.equalsIgnoreCase("controlfield") || qName.equalsIgnoreCase("subfield");


    if (elementType == null)
      return;

    switch (elementType) {
      case RECORD_ID:
        typeNotice = atts.getValue(typeAttr);
        idNotice = atts.getValue(idAttr);
        record = new Record(typeNotice, idNotice);
        break;
      case CONTROLFIELD_ID:
        buffer = new StringBuffer();
        etiqCode = atts.getValue(etiqAttr);
        controlField = newControlField(etiqCode);
        break;
      case DATAFIELD_ID:
        etiqCode = atts.getValue(etiqAttr);
        String ind1 = atts.getValue(IND1Attr);
        String ind2 = atts.getValue(IND2Attr);

        if (ind1.length() == 0) ind1 = " ";
        if (ind2.length() == 0) ind2 = " ";
        dataField = newDataField(etiqCode, ind1.charAt(0), ind2.charAt(0));
        break;
      case SUBFIELD_ID:
        buffer = new StringBuffer();
        String code = atts.getValue(codeAttr);
        if (code == null || code.length() == 0) {
          code = " ";
        }
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
      case RECORD_ID:
        list.push(record);
        break;
      case CONTROLFIELD_ID:
        controlField.setData(buffer.toString().trim());
        record.addControlField(controlField);
        break;
      case DATAFIELD_ID:
        record.addDataField(dataField);
        break;
      case SUBFIELD_ID:
        subfield.setData(buffer.toString().trim());
        dataField.addSubfield(subfield);
    }
  }

  /*********************************************************************************/
  public void endDocument() throws SAXException {
    list.end();
  }

  /*********************************************************************************/
  public DataField newDataField(String tag, char ind1, char ind2, String... subfieldCodesAndData) {
    DataField df = new DataField(tag, ind1, ind2);

    for (int i = 0; i < subfieldCodesAndData.length; i += 2) {
      Subfield sf = new Subfield(subfieldCodesAndData[i].charAt(0), subfieldCodesAndData[i + 1]);
      df.addSubfield(sf);
    }
    return (df);
  }

  /*********************************************************************************/
  public ControlField newControlField(String tag) {
    return new ControlField(tag);
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
  /*******************************************************************************************/

}
