package expressionCreationAutre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PositionZone {
	
	String content = "";
	
	public String getPositionZone (String file, String zone, String position1, String position2){
		
		/*************************** Extraire tout le contenu de la zone *****************************************/
		try{
		BufferedReader buff = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = buff.readLine()) != null) { //Parcourir toutes les lignes du fichier	
			LabelExtraction label = new LabelExtraction();
			String etiquette = label.extract(line); // Extraire l'étiquette de la ligne courante
		if (etiquette.equals(zone))
		{
			String[] caractere = line.split("");
			for (int x=8; x<caractere.length; x++){ // Parcourir tous les caractères de la ligne courante
				content = content + caractere[x]; //Affecter le caractère courant à l'élément "content"
			}
		}
		}
		}
		catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
		/*********************************************************************************************************/
		
		/*************************** Extraire le contenu des positions *******************************************/
		String[] contentPosition = content.split("");
		content = "";
		for (int x=Integer.parseInt(position1); x<=Integer.parseInt(position2); x++){ // Parcourir tous les caractères de la ligne courante
			content = content + contentPosition[x]; //Affecter le caractère courant à l'élément "content"
		}
		
		/*********************************************************************************************************/
		
		return content;
		
		
	}

}
