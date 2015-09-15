package selfContainedExpressionDev;

public class LabelExtraction {
	
	public String extract(String line){
		
		/**** Récupérer la valeur de l'étiquette de chaque ligne parcourue ****/
		String etiquette = ""; //etiquette de la ligne courante
		String[] caractere = line.split("");
	    for (int x=0; x<=3; x++){ // Parcourir les 3 premiers caractères de chaque ligne = Etiquette
	    	etiquette = etiquette + caractere[x];
	    }
	    return etiquette;
		
	}

}
