package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SousZones {

 public String extract (String predicat, String delimitor, String valeurSousZone){

	 /********** Parcourir le document UNIMARC contenant la liste des étiquettes et des sous zones*********/
	 String etiquette;
	 String delimiteur;
	 String dollarValeur =""; // dollarValeur = texte correspondant à la sous zone courante
	 boolean trouve = false; // indique si l'étiquette a été trouvée dans le document UNIMARC
	 boolean termine = false ; // indique si on a parcourru toutes les sous zones d'une étiquette donnée
	 String filePath = "Data\\UNIMARC.txt"; //Fichier UNIMARC contenant la liste des étiquettes et des sous zones
	 try{
	 BufferedReader buff = new BufferedReader(new FileReader(filePath));
	 String line;
	 while (((line = buff.readLine()) != null) && (trouve == false)) { //Parcourir toutes les lignes du fichier UNIMARC
		 LabelExtraction label = new LabelExtraction();
		 etiquette = label.extract(line); // Extraire l'étiquette de la ligne courante
		 if (etiquette.equals(predicat)){ // L'étiquette qu'on recherche
			 trouve = true; // on a trouvé l'étiquette
			 /****************** Extraire les sous zones **********************/
			 while (((line = buff.readLine()) != null) && (termine == false)) { // Tant qu'on a pas parcourru toutes les sous zones d'une étiquette donnée
				 DelimiteurExtraction dollar = new DelimiteurExtraction();
				 delimiteur = dollar.extract(line); // Extraire le délimiteur de la ligne courante
				 if (delimiteur.contains("$")){ // Vérifier si c'est bien un délimiteur
					 /********************************************************/
					 if (delimiteur.equals(delimitor)){
						String[] caractere = line.split("");
					    for (int x=3; x<caractere.length; x++){ // Parcourir tous les caractères de la ligne courante
					    	dollarValeur = dollarValeur + caractere[x]; //dollarValeur est le texte qui correspond au délimiteur
					    }
					    
					    TriplesStock triples = new TriplesStock();
						triples.stock("_:"+predicat+delimitor, dollarValeur, valeurSousZone);
						
					    termine = true;
					 }
					 /********************************************************/
				 }
				 else termine = true;
			 }
			 /*****************************************************************/
		 }
	 }
	 } catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); } 
	 /************************************************************************/
	 
	 return dollarValeur;
     }
}