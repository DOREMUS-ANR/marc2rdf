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
import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;
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
	        
	        String inputFolderPath = null ;//= "C:\\Users\\Manel\\Music\\A\\"; //default input folder
	       // String outputFolderPath = "C:\\Users\\Manel\\Music\\AA\\"; //default output folder
	        if(folder.getDir() != null)
	        {
	        	inputFolderPath = folder.getDir().replace("/", "\\");
	        	//outputFolderPath = folder.getDir().replace("/", "\\") + "\\out\\";
	        }
	        
			/*********** Copier/Coller tous les fichiers dans un seul **************/
		    listeRepertoire(new File(inputFolderPath));
		    folder.log.setText("Conversion Completed");
	}
	/*************************************************************************************************/
	public static void listeRepertoire ( File repertoire) throws URISyntaxException, IOException {
        if ( repertoire.isDirectory ( ) ) {
                File[] list = repertoire.listFiles();
                if (list != null){
                	for (File file : list) {
                		if (file.isFile()){
                			fich= file.getAbsolutePath();
            		    	if (file.getName().length()==12){ //Notice BNF
                				Model m = ModelFactory.createDefaultModel();
                		    	BNF2RDF rdf = new BNF2RDF ();
                		    	m = rdf.convert(file.getAbsolutePath());
                		    	File dir = new File(file.getParentFile()+"\\"+"RDF"+"\\");
                		    	dir.mkdirs(); //Créer un nouveau dossier
                		    	//  Mettre dans des fichiers séparés
                		          File fileName = new File(dir.getAbsolutePath()+"\\"+file.getName()+".ttl");
                		          fileName.getParentFile().mkdirs();
                		          FileWriter out = new FileWriter( fileName );
                		          m.write(out, "TURTLE");
            			          out.close();
                			}
                			else 
                				if (file.getName().length()==11){ //Notice PP
                				Model m = ModelFactory.createDefaultModel();
                		    	PP2RDF rdf = new PP2RDF ();
                		    	
                		    	/****** Verifier si c'est une notice d'oeuvre ou un TUM **********/
                		    	StringBuilder buffer = new StringBuilder();
                		        InputStream fileStream = new FileInputStream(file); //Charger le fichier MARCXML a parser
                		        MarcXmlReader reader = new MarcXmlReader(fileStream);
                		        String typeNotice = null;
                		        String idTUM = null;
                		        
                		        
                		        while (reader.hasNext()) { // Parcourir le fichier MARCXML
                		        	 Record s = reader.next();
                		        	 typeNotice = s.getType();
                		        	 
                		        	 if (typeNotice.equals("UNI:100")) { //Si c'est une notice d'oeuvre
                		        		 for (int i=0; i<s.dataFields.size(); i++) {
                		        			 if (s.dataFields.get(i).getEtiq().equals("500")) {
                		        				 if (s.dataFields.get(i).isCode('3'))
                		        					 idTUM = s.dataFields.get(i).getSubfield('3').getData();
                		        			 }
                		        		 }
                		        	 }
                		        }
                		        if (typeNotice.equals("AIC:14")) { //Si c'est un TUM
                		        	m = rdf.convert(file.getAbsolutePath());
                		        }
                		        else if (typeNotice.equals("UNI:100")){ //Si c'est une notice d'oeuvre
                		        	
                		        	m = rdf.convert(file.getAbsolutePath()); //Convertir la notice d'oeuvre
                		        	final File folderTUMs = new File("C:\\Users\\Manel\\Music\\Datasets\\PP\\TUMs\\");
                		        	File tum = getTUM(folderTUMs, idTUM);
                		        	fich= tum.getAbsolutePath();
                		        	m.add(rdf.convert(tum.getAbsolutePath())); //Convertir le TUM correspondant
                		        }
                		    	/****************************************************************/
                		    	
                		    	File dir = new File(file.getParentFile()+"\\"+"RDF"+"\\");
                		    	dir.mkdirs(); //Créer un nouveau dossier
                		    	//  Mettre dans des fichiers séparés
                		          File fileName = new File(dir.getAbsolutePath()+"\\"+file.getName()+".ttl");
                		          fileName.getParentFile().mkdirs();
                		          FileWriter out = new FileWriter( fileName );
                		          m.write(out, "TURTLE");
            			          out.close();
                			}
                		}
        		    }
	                for ( int i = 0; i < list.length; i++) {
	                        listeRepertoire( list[i]);
	                } 
                } 
        } 
} 
	
	
	public static File getTUM(final File folder, String idTUM) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	getTUM(fileEntry, idTUM);
	        } else {
	            if (fileEntry.getName().equals(idTUM+".xml")) {
	            	return fileEntry;
	            }
	        }
	    }
		return folder;
	}
}