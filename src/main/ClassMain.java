package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ClassMain {
	
	static String subject;
	static String predicate = "";
	static String object= "";
	static ArrayList<String> tab = new ArrayList<String>(); // Tableau dynamique pour ajouter les valeurs du tableau
	
	/**************************** Methode main ()
	 * @throws URISyntaxException ***********************************/
	public static void main(String[] args) throws URISyntaxException {
		
		 /***************** Parcourir le document UNIMARC ************************/
		    String filePath = "Data\\1.Document.txt"; //Fichier UNIMARC décrivant une oeuvre
			try{
			BufferedReader buff1 = new BufferedReader(new FileReader(filePath));
			String line;
			
			/**************** Rechercher le numéro de notice dans le fichier MARC *******************/
			ConstructURI uri = new ConstructURI();
			subject = uri.get().toString();
			
			/******** Rechercher les prédicats et les objets dans le fichier MARC ********/
			BufferedReader buff2 = new BufferedReader(new FileReader(filePath));
			while ((line = buff2.readLine()) != null) { //Parcourir toutes les lignes du fichier
				RDFConversion rdfConversion = new RDFConversion();
				rdfConversion.convert(line); // Rechercher le prédicat et LES objets de la ligne courante
				
				/***************** Stocker les triplets  *******************************/
			/*	if ((!(subject.equals("")))&&(!(predicate.equals("")))&&(!(object.equals("")))){
					TriplesStock triples = new TriplesStock();
					triples.stock(subject, predicate, object);
				}
				/**********************************************************************/
			}
			} catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
			
			/************ Afficher les triplets dans la console *********************/
			DisplayConsole console = new DisplayConsole();
			console.display(tab); //Afficher <Sujet,Prédicat,Objet> de la ligne courante	
	}
}
