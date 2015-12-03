package main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class FileClass {
	
	static org.jdom2.Document document;
	static Element racine;

	public static void main(String[] args) throws IOException, JDOMException {
		
		    final Collection<File> all = new ArrayList<File>();
		    findFilesRecursively(new File("C:\\Users\\Manel\\Music\\files\\"), all, ".xml");
		    for (File file : all) {
		    	  /***************************************************************/
		    	  SAXBuilder sxb = new SAXBuilder();
			      document = sxb.build(file);
			      racine = document.getRootElement();
			      /*******************************************************************/
			      List listNodes = racine.getChildren("controlfield"); //On cree une Liste contenant tous les noeuds de l'Element racine
			      Iterator i = listNodes.iterator();
			      while(i.hasNext())
			      {
			         Element courant = (Element)i.next();
			         if (courant.getAttributeValue("tag").equals("001"))
			         System.out.println(courant.getTextNormalize());
			      }
			      /*******************************************************************/
		    }
	}
	/*************************************************************************************************/
	private static void findFilesRecursively(File file, Collection<File> all, final String extension) {
	    final File[] children = file.listFiles(new FileFilter() {
	    	public boolean accept(File f) {
	                return f.getName().endsWith(extension) ;
	            }}
	    );
	    if (children != null) {
	        for (File child : children) {
	            all.add(child);
	            findFilesRecursively(child, all, extension);
	        }
	    }
	}
	/*************************************************************************************************/
}
