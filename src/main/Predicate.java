package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Predicate {
	
	String predicate = "";
	Boolean trouve = false;
	
	public String extract (String etiquette){
		
		/*************************** Liste des Etiquettes dans UNIMARC *********************************/
	    String filePath = "Data\\UNIMARC-B.txt"; //Fichier UNIMARC contenant toutes les �tiquettes et toutes les sous zones
		try{
		BufferedReader buff = new BufferedReader(new FileReader(filePath));
		String line;
		while (((line = buff.readLine()) != null) && (trouve == false)) { //Parcourir toutes les lignes du fichier
			LabelExtraction label = new LabelExtraction();
			predicate = label.extract(line); // Extraire l'�tiquette de la ligne courante (3 premiers caract�res)
			if (predicate.equals(etiquette)) { // Si cette �tiquette correspond � l'�tiquette de la ligne du fichier trait�
				predicate =""; // predicate = texte correspondant � l'�tiquette
				String[] caractere = line.split("");
			    for (int x=4; x<caractere.length; x++){ // Parcourir tous les caract�res de la ligne courante
			    	predicate = predicate + caractere[x];
			    }
				trouve = true; // On a trouv� l'�tiquette dans le fichier UNIMARC
			}
		}
		}
		catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
		
		/***********************************************************************************************/
		
		return predicate;
	}

}
