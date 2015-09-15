package expressionCreationAutre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import expressionCreationRepr.PositionZone;

public class DateCreationM {
	
static String date  = "";
static String content = "";
	
	public static String getDateCreationM (String file) { // file = Fichier UNIMARC décrivant une oeuvre
		
		/*************************** Extraire tout le contenu de la zone *****************************************/
		try{
		BufferedReader buff = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = buff.readLine()) != null) { //Parcourir toutes les lignes du fichier	
			LabelExtraction label = new LabelExtraction();
			String etiquette = label.extract(line); // Extraire l'étiquette de la ligne courante
		if (etiquette.equals("008"))
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
		
		String[] contentPosition = content.split("");
		if ((!(contentPosition[17].equals("f")))&& (!(contentPosition[17].equals("r")))) {
			PositionZone p = new PositionZone();
			date = p.getPositionZone(file, "008", "7", "16");
		}
		else if ((contentPosition[17].equals("f"))|| (contentPosition[17].equals("r"))) {
			PositionZone p = new PositionZone();
			date = p.getPositionZone(file, "008", "19", "28");
		}
		
		return date ;
	}

}
