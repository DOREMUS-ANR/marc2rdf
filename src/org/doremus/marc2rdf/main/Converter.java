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

import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.bnfparser.MarcXmlReader;
import org.doremus.marc2rdf.bnfparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;

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
		    findFilesRecursively(new File("C:\\Users\\Manel\\Music\\A\\"), all, ".xml");

	     //   Model model = ModelFactory.createDefaultModel();
	      //  Model m = ModelFactory.createDefaultModel();
	        FileWriter out = null;
		    for (File file : all) {
		    	fich= file.getAbsolutePath();
		    	Model m = ModelFactory.createDefaultModel();
		    	/***************************************************************/
		    	BNF2RDF rdf = new BNF2RDF ();
		    	//PP2RDF rdf = new PP2RDF ();
		    	m = rdf.convert(file.getAbsolutePath());
		   // 	model.add(m);
		    
		    	//  Mettre dans des fichiers séparés
		          String fileName = "C:\\Users\\Manel\\Music\\AA\\"+file.getName()+".ttl";
		          out = new FileWriter( fileName );
		          
		          try {
			             m.write(out, "TURTLE");
			         }
			         finally {
			            try {
			                out.close();
			            }
			            catch (IOException closeException) {
			            }
			         }
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