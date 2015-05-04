package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Predicate {
	
	String predicate = "";
	Boolean trouve = false;
	
	public String extract (String etiquette){
		
		/*************************** Liste des Etiquettes dans UNIMARC *********************************/
	    String filePath = "Data\\UNIMARC-B.txt"; //Fichier UNIMARC contenant toutes les étiquettes et toutes les sous zones
		try{
		BufferedReader buff = new BufferedReader(new FileReader(filePath));
		String line;
		while (((line = buff.readLine()) != null) && (trouve == false)) { //Parcourir toutes les lignes du fichier
			LabelExtraction label = new LabelExtraction();
			predicate = label.extract(line); // Extraire l'étiquette de la ligne courante (3 premiers caractères)
			if (predicate.equals(etiquette)) { // Si cette étiquette correspond à l'étiquette de la ligne du fichier traité
				predicate =""; // predicate = texte correspondant à l'étiquette
				String[] caractere = line.split("");
			    for (int x=4; x<caractere.length; x++){ // Parcourir tous les caractères de la ligne courante
			    	predicate = predicate + caractere[x];
			    }
				trouve = true; // On a trouvé l'étiquette dans le fichier UNIMARC
			}
		}
		}
		catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
		
		/***********************************************************************************************/
		
		return predicate;
	}

}
