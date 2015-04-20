package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SpecificSousZone {

	String valeurSousZone = "";
	Boolean sousZoneBool = false ; // indique si on est dans une sous-zone ou pas
	Boolean delimiteur = false ; // indique si le caract�re courant est un d�limiteur
	int index = 0 ; // L'index de la lettre qui vient juste apr�s le "$"
	String delimiteurString ; // Exemples : $a, $b, etc.
	String etiquette;
	
	public String extract (String etiq, String delim){
		
		String filePath = "Data\\1.Document.txt"; //Fichier UNIMARC d�crivant une oeuvre
		try{
		BufferedReader buff = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = buff.readLine()) != null) { //Parcourir toutes les lignes du fichier
			
		/********************** Extraire l'�tiquette de la ligne courante ***************************/	
			LabelExtraction label = new LabelExtraction();
			etiquette = label.extract(line);
		/********************************************************************************************/
		if (etiquette.equals(etiq)){	
		String[] caractere = line.split("");
	    for (int x=0; x<caractere.length; x++){ // Parcourir tous les caract�res de la ligne courante
	    	if (!((index==x)&& (index != 0))) { //Si on ne se positionne pas � la lettre qui vient juste apr�s "$"
	    		
	    	/********** V�rifier si le caract�re courant est un d�limiteur ($a, $b, etc) ************/
	    	if (caractere[x].equals("$"))
	    	{
	    		valeurSousZone = ""; // On se retrouve dans une nouvelle sous-zone
	    		sousZoneBool = false ; // Remettre sousZoneBool � false avant de v�rifier si c'est bien un d�limiteur
	    		try {
				Integer.parseInt(caractere[x+1]); // Verifier si le caract�re apres le "$" est un entier
			} catch (NumberFormatException e){ // Si le caract�re apres le "$" est une lettre alors c'est un d�limiteur
				sousZoneBool = true ; // On est bien dans une sous zone
				delimiteur = true ; // Le caract�re courant est un d�limiteur
				index = x+1; // Index de la lettre qui vient juste apres "$" et qu'on va pas prendre en compte
				delimiteurString = "$"+caractere[x+1];
			}
	    	}
	    	else if ((sousZoneBool==true)&&(delimiteurString.equals(delim))) extractSousZone(caractere[x]); //Stocker les caract�res apres "$lettre"
	}
	    }}}
		}
	    catch (IOException e){}
		return valeurSousZone;
		}
	/*************** R�cup�rer le contenu de la sous zone courante ********************/
	public void extractSousZone(String character){
		valeurSousZone = valeurSousZone + character; //Affecter le caract�re courant � l'�l�ment "sousZone"
	}

}
