package org.doremus.marc2rdf.ppparser;

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
  private Record record;
  private String etiqCode;
  private String typeNotice;
  private String idNotice;

  /******
   * Constantes representant chaque type de balise
   ******/

  private static final int RECORD_ID = 2;
  private static final int DATAFIELD_ID = 3;
  private static final int SUBFIELD_ID = 4;

  private static final String etiqAttr = "UnimarcTag";
  private static final String codeAttr = "UnimarcSubfield";
  private static final String typeAttr = "type";
  private static final String idAttr = "id";

  private static final HashMap<String, Integer> elementMap; // Hashset for mapping of element strings to constants (Integer)

  static {
    elementMap = new HashMap<>();
    elementMap.put("NOTICE", RECORD_ID);
    elementMap.put("champs", DATAFIELD_ID);
    elementMap.put("SOUSCHAMP", SUBFIELD_ID);
  }

  private boolean bDATA = false;

  /*****************************
   * Constructeur
   **************************************/
  public MarcXmlHandler(RecordList queue) {
    this.list = queue;
  }

  /*********************************************************************************/
  public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException {
    String realName = (name.length() == 0) ? qName : name; //Récupérer le nom de la balise
    Integer elementType = elementMap.get(realName); //A quel "ID" correspond l'élément récupéré

    bDATA = (qName.equalsIgnoreCase("data"));

    if (elementType == null)
      return;

    switch (elementType) {
      case RECORD_ID:
        typeNotice = atts.getValue(typeAttr);
        idNotice = atts.getValue(idAttr);
        record = new Record(typeNotice, idNotice);
        break;
      case DATAFIELD_ID:
        etiqCode = atts.getValue(etiqAttr);
        dataField = newDataField(etiqCode);
        break;
      case SUBFIELD_ID:
        buffer = new StringBuffer();
        String code = atts.getValue(codeAttr); // Exemple : code = "017$a", on doit récupérer que ce qui est après le $
        int index = code.indexOf("$");
        code = code.substring(index + 1, code.length()); //On récupère que ce qui est après le $
        if (code.length() == 0) {
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
  public DataField newDataField(String tag, String... subfieldCodesAndData) {
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
  /*******************************************************************************************/

}
