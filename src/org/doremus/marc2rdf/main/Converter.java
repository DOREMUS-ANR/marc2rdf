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

import javax.swing.JFrame;

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
	
	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
			/****** Selectionnez le dossier contenant les fichiers a convertir *****/
			FileChooser folder = new FileChooser();
			JFrame frame = new JFrame("MARC to RDF");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setLocationRelativeTo(null);
	        //Add content to the window.
	        frame.add(folder);
	        //Display the window
	        frame.pack();
	        frame.setVisible(true);
	
	        synchronized (folder) {
	            while (!folder.getChoosed())
	            	folder.wait();
	        }
	        
	        String inputFolderPath = "C:\\Users\\Manel\\Music\\A\\"; //default input folder
	        String outputFolderPath = "C:\\Users\\Manel\\Music\\AA\\"; //default output folder
	        if(folder.getDir() != null)
	        {
	        	inputFolderPath = folder.getDir().replace("/", "\\");
	        	outputFolderPath = folder.getDir().replace("/", "\\") + "\\out\\";
	        }
	        
			/*********** Copier/Coller tous les fichiers dans un seul **************/
		    final Collection<File> all = new ArrayList<File>();
		    findFilesRecursively(new File(inputFolderPath), all, ".xml");

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
		          File fileName = new File(outputFolderPath+file.getName()+".ttl");
		          fileName.getParentFile().mkdirs();
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
		    folder.log.setText("Conversion Completed");
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