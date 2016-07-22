package org.doremus.marc2rdf.marcparser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class MarcXmlReader {

  private RecordList list;
  private MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder;

  /********
   * Le constructeur : lui passer le fichier MARCXML a parser
   *********/
  public MarcXmlReader(InputStream input, MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder) {
    //Charger le fichier XML pour le parser
    this(new InputSource(input), handlerBuilder);
  }

  /**********************
   * Parser le fichier XML
   ******************************/

  public MarcXmlReader(InputSource input, MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder) {
    try {
      // Mettre toutes les zones du MARC dans une liste
      MarcXmlHandler handler = handlerBuilder.build();
      this.list = handler.getList();
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

  /**************************************************************************/

  public boolean hasNext() {
    return list.hasNext();
  }


  public Record next() {
    return list.pop();
  }

}
