package selfContainedExpressionDevAutre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SubZoneContent { // Récupérer le contenu d'une sous-zone
	
	String content = "" ;
	
	public String getSubZoneContent (String file, String zone, String subZone){
		
		
		/*********************************************************************************************************/
		try{
		BufferedReader buff = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = buff.readLine()) != null) { //Parcourir toutes les lignes du fichier
			
			Boolean sousZoneBool = false ; // indique si on est dans une sous-zone ou pas
			Boolean delimiteur = false ; // indique si le caractère courant est un délimiteur
			int index = 0 ; // L'index de la lettre qui vient juste après le "$"
			
			LabelExtraction label = new LabelExtraction();
			String etiquette = label.extract(line); // Extraire l'étiquette de la ligne courante
		if (etiquette.equals(zone))
		{
			String[] caractere = line.split("");
		    for (int x=0; x<caractere.length; x++){ // Parcourir tous les caractères de la ligne courante
		    	if (!((index==x)&& (index != 0))) { //Si on ne se positionne pas à la lettre qui vient juste après "$"
		    	/********** Vérifier si le caractère courant est un délimiteur ($a, $b, etc) ************/
		    	if ((caractere[x].equals("$"))&&(caractere[x+1].equals(subZone)))
		    	{
					sousZoneBool = true ; // On est bien dans une sous zone
					delimiteur = true ; // Le caractère courant est un délimiteur
					index = x+1; // Index de la lettre qui vient juste apres "$" et qu'on va pas prendre en compte
		    	}
		    	else if ((caractere[x].equals("$"))&&(!(caractere[x+1].equals(subZone)))) sousZoneBool=false ;
		    	else if (sousZoneBool==true) extractSousZone(caractere[x]); //Stocker les caractères apres "$lettre"
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
		content = content + character; //Affecter le caractère courant à l'élément "subject"
	}
}
