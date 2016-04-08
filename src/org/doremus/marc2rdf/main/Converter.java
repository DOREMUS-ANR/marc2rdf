package org.doremus.marc2rdf.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import org.doremus.marc2rdf.bnfConverter.BNF2RDF;
import org.doremus.marc2rdf.bnfMarcXML.MarcXmlReader;
import org.doremus.marc2rdf.bnfMarcXML.Record;
import org.doremus.marc2rdf.ppConverter.PP2RDF;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Converter {

	static String fich="";
	
	public static String getFile(){
		return fich;
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		
			/*********** Copier/Coller tous les fichiers dans un seul **************/
		    final Collection<File> all = new ArrayList<File>();
		    findFilesRecursively(new File("C:\\Users\\Eva\\Desktop\\Eva\\EURECOM\\Internship\\dataAux\\"), all, ".xml");
	       // FileWriter out = new FileWriter("C:\\Users\\Manel\\Videos\\A\\notice.nt");
	        Model model = ModelFactory.createDefaultModel();
	        Model m = ModelFactory.createDefaultModel();
	        FileWriter out = null;
		    for (File file : all) {
		    	fich= file.getAbsolutePath();
		    	/***************************************************************/
		    	//BNF2RDF rdf = new BNF2RDF ();
		    	PP2RDF rdf = new PP2RDF ();
		    	m = rdf.convert(file.getAbsolutePath());
		    	model.add(m);
		    }
		    	//  Mettre dans des fichiers séparés
		 /*         String fileName = "C:\\Users\\Manel\\Music\\AA\\"+file.getName();
		          out = new FileWriter( fileName );
		    	/***************************************************************/
		/*    }
		     try {
	             model.write(out);
	         }
	         finally {
	            try {
	                out.close();
	            }
	            catch (IOException closeException) {
	            }
	         }*/
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