package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SousZonesExtraction {
	
	String valeurSousZone = "";
	Boolean sousZoneBool = false ; // indique si on est dans une sous-zone ou pas
	Boolean delimiteur = false ; // indique si le caract�re courant est un d�limiteur
	int index = 0 ; // L'index de la lettre qui vient juste apr�s le "$"
	String delimiteurString ; // Exemples : $a, $b, etc.
	
	public String extract (String line){
		
		String[] caractere = line.split("");
	    for (int x=0; x<caractere.length; x++){ // Parcourir tous les caract�res de la ligne courante
	    	if (!((index==x)&& (index != 0))) { //Si on ne se positionne pas � la lettre qui vient juste apr�s "$"
	    		
	    	/********** V�rifier si le caract�re courant est un d�limiteur ($a, $b, etc) ************/
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
	    		sousZoneBool = false ; // Remettre sousZoneBool � false avant de v�rifier si c'est bien un d�limiteur
	    		try {
				Integer.parseInt(caractere[x+1]); // Verifier si le caract�re apres le "$" est un entier
			} catch (NumberFormatException e){ // Si le caract�re apres le "$" est une lettre alors c'est un d�limiteur
				sousZoneBool = true ; // On est bien dans une sous zone
				delimiteur = true ; // Le caract�re courant est un d�limiteur
				index = x+1; // Index de la lettre qui vient juste apres "$" et qu'on va pas prendre en compte
				
				/***********************************************************************************/
				delimiteurString = "$"+caractere[x+1];
				TriplesStock triples = new TriplesStock();
				triples.stock(ClassMain.subject, ClassMain.predicate, ":_"+RDFConversion.etiquette+delimiteurString); // Exemple : (n� notice, titre et mention de responsabilit�, 200 $a)
				/************ rechercher dans le fichier UNIMARC � quoi correspond ce d�limiteur **************/
				
				Boolean found = false; 
				String predicate; 
				String sousZone = ""; // sousZone = texte correspondant � la sousZone
				String filePath = "Data\\UNIMARC-B.txt"; //Fichier UNIMARC contenant toutes les �tiquettes et toutes les sous zones
					try{
					BufferedReader buff = new BufferedReader(new FileReader(filePath));
					String ligne;
					while (((ligne = buff.readLine()) != null) && (found == false)) { //Parcourir toutes les lignes du fichier
						LabelExtraction label = new LabelExtraction();
						predicate = label.extract(ligne); // Extraire l'�tiquette de la ligne courante (3 premiers caract�res)
						if (predicate.equals(ClassMain.predicate)) { // Si cette �tiquette correspond � l'�tiquette de la ligne du fichier trait�
							
						    found = true; // On a trouv� la sous-zone appartenant � l'�tiquette courante dans le fichier UNIMARC
						}
					}
					}
					catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
				/**********************************************************************************************/
			}
	    	}
	    	else if (sousZoneBool==true) extractSousZone(caractere[x]); //Stocker les caract�res apres "$lettre"
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
	
	/*************** R�cup�rer le contenu de la sous zone courante ********************/
	public void extractSousZone(String character){
		valeurSousZone = valeurSousZone + character; //Affecter le caract�re courant � l'�l�ment "sousZone"
	}

}
