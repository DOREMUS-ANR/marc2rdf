package selfContainedExpressionDevAutre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SubZoneContent { // R�cup�rer le contenu d'une sous-zone
	
	String content = "" ;
	
	public String getSubZoneContent (String file, String zone, String subZone){
		
		
		/*********************************************************************************************************/
		try{
		BufferedReader buff = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = buff.readLine()) != null) { //Parcourir toutes les lignes du fichier
			
			Boolean sousZoneBool = false ; // indique si on est dans une sous-zone ou pas
			Boolean delimiteur = false ; // indique si le caract�re courant est un d�limiteur
			int index = 0 ; // L'index de la lettre qui vient juste apr�s le "$"
			
			LabelExtraction label = new LabelExtraction();
			String etiquette = label.extract(line); // Extraire l'�tiquette de la ligne courante
		if (etiquette.equals(zone))
		{
			String[] caractere = line.split("");
		    for (int x=0; x<caractere.length; x++){ // Parcourir tous les caract�res de la ligne courante
		    	if (!((index==x)&& (index != 0))) { //Si on ne se positionne pas � la lettre qui vient juste apr�s "$"
		    	/********** V�rifier si le caract�re courant est un d�limiteur ($a, $b, etc) ************/
		    	if ((caractere[x].equals("$"))&&(caractere[x+1].equals(subZone)))
		    	{
					sousZoneBool = true ; // On est bien dans une sous zone
					delimiteur = true ; // Le caract�re courant est un d�limiteur
					index = x+1; // Index de la lettre qui vient juste apres "$" et qu'on va pas prendre en compte
		    	}
		    	else if ((caractere[x].equals("$"))&&(!(caractere[x+1].equals(subZone)))) sousZoneBool=false ;
		    	else if (sousZoneBool==true) extractSousZone(caractere[x]); //Stocker les caract�res apres "$lettre"
		}
		}
		    }
		}
		}
		catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
		/*********************************************************************************************************/
		
		return content ;
		
	}
	
	public void extractSousZone(String character){
		content = content + character; //Affecter le caract�re courant � l'�l�ment "subject"
	}
}
