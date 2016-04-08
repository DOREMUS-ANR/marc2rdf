package org.doremus.marc2rdf.bnfMarcXML;

import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource; //Utiliser "SAX" pour parser un fichier XML
import org.xml.sax.XMLReader;

public class MarcXmlReader {

	private RecordList list;

	/******** Le constructeur : lui passer le fichier MARCXML a parser *********/
  	public MarcXmlReader(InputStream input) {
  		this(new InputSource(input)); //Charger le fichier XML pour le parser
  	}
  	
  	/********************** Parser le fichier XML ******************************/
    public MarcXmlReader(InputSource input) {
    	this.list = new RecordList();
    	try {
    		MarcXmlHandler handler = new MarcXmlHandler(list); //Mettre toutes les zones du MARC dans une liste
            parse(handler,input); //Parser le fichier XML
    	} 
    	catch (Exception e) { }
    }
    
    /********************** Parser le fichier XML*****************************/
    private void parse(ContentHandler handler, InputSource input) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        XMLReader reader = null;
        try {
            reader = spf.newSAXParser().getXMLReader();
            reader.setContentHandler(handler);
            reader.parse(input);
        } catch (Exception e) {
        }
    }
    /**************************************************************************/
    
  public boolean hasNext() {
    return list.hasNext();
  }


  public Record next() {
    return list.pop();
  }

   /**************************************************************************/  

}