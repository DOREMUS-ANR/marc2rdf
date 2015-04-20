package main;

public class RDFConversion {
	
	static String etiquette;
	
	/************* Méthode de conversion d'UNIMARC en RDF ************/
	public void convert (String line){
		
		LabelExtraction label = new LabelExtraction();
		etiquette = label.extract(line); // Extraire l'étiquette de la ligne courante
		
		/********* Extraire le prédicat de la ligne courante *************/
		Predicate predicate = new Predicate ();
		ClassMain.predicate = predicate.extract(etiquette);
		
		/************ Extraire TOUS les objets de la ligne courante ***************/
		SousZonesExtraction sousZones = new SousZonesExtraction(); //Extraire les sous zones de la ligne courante
		sousZones.extract(line); // Objet = Sous-zone de la ligne courante
//		ClassMain.object = sousZones.extract(line); // Objet = Sous-zone de la ligne courante
	}
	
	/***************** Rechercher le numéro de notice dans le fichier MARC *******************************/
	public String searchNumNotice (String line){
		
		String numNotice="";
		LabelExtraction label = new LabelExtraction();
		String etiquette = label.extract(line); // Extraire l'étiquette de la ligne courante
		
		/********* Récupérer le numéro de notice dont l'étiquette est 001 ********/
		if (etiquette.equals("001")) // Le numero de la notice 
		{
			String[] caractere = line.split("");
		    for (int x=4; x<caractere.length; x++){ // Parcourir tous les caractères de la ligne courante
		    	numNotice = numNotice+ caractere [x];
		    }
			// SousZonesExtraction sousZones = new SousZonesExtraction(); //Extraire les sous zones de la ligne courante
			// ClassMain.subject = sousZones.extract(line); // Subject = TUM (Titre Uniforme Musical)
			}
		return numNotice;
	}
}
