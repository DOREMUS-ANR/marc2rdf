package expressionCreationAutre;

public class DateCreationH {
	
	static String date  = "";
	
	public static String getDateCreation (String file) { // file = Fichier UNIMARC décrivant une oeuvre
		
		/***********************  Si date indiquée en texte libre ***************************/
		SubZoneContent subZone = new SubZoneContent () ;
		date = subZone.getSubZoneContent(file, "260" , "d") ;
		/************************************************************************************/
		return date ;
	}

}
