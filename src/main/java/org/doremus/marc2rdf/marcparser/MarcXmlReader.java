package org.doremus.marc2rdf.marcparser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MarcXmlReader {

  private List<Record> list;

  /********
   * Constructor bridge
   *********/
  public MarcXmlReader(InputStream input, MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder) {
    //Charger le fichier XML pour le parser
    this(new InputSource(input), handlerBuilder);
  }

  public MarcXmlReader(String inputFile, MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder) throws FileNotFoundException {
    this(new FileInputStream(inputFile), handlerBuilder);
  }

  public MarcXmlReader(File inputFile, MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder) throws FileNotFoundException {
    this(new FileInputStream(inputFile), handlerBuilder);
  }

  /**********************
   * Constructor main
   ******************************/

  public MarcXmlReader(InputSource input, MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder) {
    try {
      // Mettre toutes les zones du MARC dans une liste
      MarcXmlHandler handler = handlerBuilder.build();
      this.list = handler.getRecordList();

      parse(handler, input); //Parser le fichier XML
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**********************
   * Parser le fichier XML
   *****************************/
  private void parse(ContentHandler handler, InputSource input) {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    try {
      XMLReader reader = spf.newSAXParser().getXMLReader();
      reader.setContentHandler(handler);
      reader.parse(input);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<Record> getRecords() {
    return list;
  }

  public DataField getDatafieldByCode(String code) {
    for (Record s : list) {
      DataField res = s.getDatafieldByCode(code);
      if (res != null) return res;

    }
    return null;
  }

  public List<DataField> getDatafieldsByCode(String code) {
    List<DataField> results = new ArrayList<>();
    for (Record s : list) {
      results.addAll(s.getDatafieldsByCode(code));
    }
    return results;
  }

  public List<ControlField> getControlfieldsByCode(String code) {
    List<ControlField> results = new ArrayList<>();
    for (Record s : list) {
      results.addAll(s.getControlfieldsByCode(code));
    }
    return results;
  }

}
