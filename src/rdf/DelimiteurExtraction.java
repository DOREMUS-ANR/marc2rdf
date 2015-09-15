package rdf;

public class DelimiteurExtraction {
	
	public String extract(String line){
		
		/**** Récupérer la valeur du délimiteur de chaque ligne parcourue ****/
		String delimiteur = ""; //delimiteur de la ligne courante
		String[] caractere = line.split("");
	    for (int x=0; x<=2; x++){ // Parcourir les 2 premiers caractères de chaque ligne = Etiquette
	    	delimiteur = delimiteur + caractere[x];
	    }
	    return delimiteur;
		
	}

}
