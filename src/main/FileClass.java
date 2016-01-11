package main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class FileClass {

	public static void main(String[] args) throws IOException {
		    final Collection<File> all = new ArrayList<File>();
		    findFilesRecursively(new File("C:\\Users\\Manel\\Music\\fichiers"), all, ".xml");
		    for (File file : all) {
		    	/***************************************************************/
		    	Record record = null; // Record = notice
		        InputStream fileInput = new FileInputStream(file); //Charger le fichier MARCXML a parser
		        MarcXmlReader reader = new MarcXmlReader(fileInput);
		        String codeGenre = "";
		        while (reader.hasNext()) { // Parcourir le fichier MARCXML
		        	 Record s = reader.next();
		        	 for (int i=0; i<s.controlFields.size(); i++) {
		        		 if (s.controlFields.get(i).getEtiq().equals("008")) {
		        			 	for (int j=18; j<=20; j++){
		        			 		codeGenre = codeGenre + s.controlFields.get(i).getData().charAt(j);
		        			 		//buffer.append(s.controlFields.get(i).getData().charAt(j));
		        			 	}   
		        			 	if (codeGenre.startsWith("sn")){
		        			 	File cible = new File("C:\\Users\\Manel\\Music\\F\\sn\\"+file.getName()+".xml");
		        		    	 copyFile(file,cible);
		        		    	 System.out.println("1");
		        			 	}
		        			 	else if (codeGenre.startsWith("co")){
			        			 	File cible = new File("C:\\Users\\Manel\\Music\\F\\co\\"+file.getName()+".xml");
			        		    	 copyFile(file,cible);
			        		    	 System.out.println("2");
			        			 	}
		        			 	else if (codeGenre.startsWith("sy")){
			        			 	File cible = new File("C:\\Users\\Manel\\Music\\F\\sy\\"+file.getName()+".xml");
			        		    	 copyFile(file,cible);
			        		    	 System.out.println("3");
			        			 	}
		        		 }
		        	 }
		        }
		    	/***************************************************************/
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
	public static boolean copyFile(File source, File dest){
		try{
			java.io.FileInputStream sourceFile = new java.io.FileInputStream(source);
				java.io.FileOutputStream destinationFile = null;
					destinationFile = new FileOutputStream(dest);
					byte buffer[] = new byte[512 * 1024];
					int nbLecture;
					while ((nbLecture = sourceFile.read(buffer)) != -1){
						destinationFile.write(buffer, 0, nbLecture);
					}
			} 
		 catch (IOException e){
		}
	 
		return true; 
	}
}