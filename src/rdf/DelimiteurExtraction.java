package rdf;

public class DelimiteurExtraction {
	
	public String extract(String line){
		
		/**** R�cup�rer la valeur du d�limiteur de chaque ligne parcourue ****/
		String delimiteur = ""; //delimiteur de la ligne courante
		String[] caractere = line.split("");
	    for (int x=0; x<=2; x++){ // Parcourir les 2 premiers caract�res de chaque ligne = Etiquette
	    	delimiteur = delimiteur + caractere[x];
	    }
	    return delimiteur;
		
	}

}
