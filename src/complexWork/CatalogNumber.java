package complexWork;

public class CatalogNumber {

	static String catalogNumber = "";
	
	public static String getCatalogNumber (String file) {// file = Fichier UNIMARC décrivant une oeuvre
		
		SubZoneContent subZ = new SubZoneContent () ;
		String subZone = subZ.getSubZoneContent(file, "144" , "k") ; // Récupérer le contenu de la sous-zone
		
        /*************************************************/
		String[] caracter = subZone.split("");
		
		boolean t = false ;
		for (int x=0; x<caracter.length; x++){
			if ((caracter[x].equals(" "))) t = true ;
			else if (t==true) catalogNumber = catalogNumber + caracter[x];
		}
		/************************************************/
		
		return catalogNumber;
	}
}
