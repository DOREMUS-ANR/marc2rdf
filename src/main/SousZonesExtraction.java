package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SousZonesExtraction {
	
	String valeurSousZone = "";
	Boolean sousZoneBool = false ; // indique si on est dans une sous-zone ou pas
	Boolean delimiteur = false ; // indique si le caractère courant est un délimiteur
	int index = 0 ; // L'index de la lettre qui vient juste après le "$"
	String delimiteurString ; // Exemples : $a, $b, etc.
	
	public String extract (String line){
		
		String[] caractere = line.split("");
	    for (int x=0; x<caractere.length; x++){ // Parcourir tous les caractères de la ligne courante
	    	if (!((index==x)&& (index != 0))) { //Si on ne se positionne pas à la lettre qui vient juste après "$"
	    		
	    	/********** Vérifier si le caractère courant est un délimiteur ($a, $b, etc) ************/
	    	if (caractere[x].equals("$"))
	    	{
	    		/************************************************************************************/
	    		if (valeurSousZone != "") 				
				{
				SousZones sousZ = new SousZones ();
				sousZ.extract(RDFConversion.etiquette, delimiteurString, valeurSousZone);
				}
	    		/************************************************************************************/
	    		valeurSousZone = ""; // On se retrouve dans une nouvelle sous-zone
	    		sousZoneBool = false ; // Remettre sousZoneBool à false avant de vérifier si c'est bien un délimiteur
	    		try {
				Integer.parseInt(caractere[x+1]); // Verifier si le caractère apres le "$" est un entier
			} catch (NumberFormatException e){ // Si le caractère apres le "$" est une lettre alors c'est un délimiteur
				sousZoneBool = true ; // On est bien dans une sous zone
				delimiteur = true ; // Le caractère courant est un délimiteur
				index = x+1; // Index de la lettre qui vient juste apres "$" et qu'on va pas prendre en compte
				
				/***********************************************************************************/
				delimiteurString = "$"+caractere[x+1];
				TriplesStock triples = new TriplesStock();
				triples.stock(ClassMain.subject, ClassMain.predicate, ":_"+RDFConversion.etiquette+delimiteurString); // Exemple : (n° notice, titre et mention de responsabilité, 200 $a)
				/************ rechercher dans le fichier UNIMARC à quoi correspond ce délimiteur **************/
				
				Boolean found = false; 
				String predicate; 
				String sousZone = ""; // sousZone = texte correspondant à la sousZone
				String filePath = "Data\\UNIMARC-B.txt"; //Fichier UNIMARC contenant toutes les étiquettes et toutes les sous zones
					try{
					BufferedReader buff = new BufferedReader(new FileReader(filePath));
					String ligne;
					while (((ligne = buff.readLine()) != null) && (found == false)) { //Parcourir toutes les lignes du fichier
						LabelExtraction label = new LabelExtraction();
						predicate = label.extract(ligne); // Extraire l'étiquette de la ligne courante (3 premiers caractères)
						if (predicate.equals(ClassMain.predicate)) { // Si cette étiquette correspond à l'étiquette de la ligne du fichier traité
							
						    found = true; // On a trouvé la sous-zone appartenant à l'étiquette courante dans le fichier UNIMARC
						}
					}
					}
					catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
				/**********************************************************************************************/
			}
	    	}
	    	else if (sousZoneBool==true) extractSousZone(caractere[x]); //Stocker les caractères apres "$lettre"
	}
	}
	    /************************************************************************************/
		if (valeurSousZone != "") 				
		{
		SousZones sousZ = new SousZones ();
		sousZ.extract(RDFConversion.etiquette, delimiteurString, valeurSousZone);
		}
		/************************************************************************************/
		
		return valeurSousZone;
	}
	
	/*************** Récupérer le contenu de la sous zone courante ********************/
	public void extractSousZone(String character){
		valeurSousZone = valeurSousZone + character; //Affecter le caractère courant à l'élément "sousZone"
	}

}
