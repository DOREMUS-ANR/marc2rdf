package complexWork;

public class CatalogName {
	
	static String catalogName = "";
	
	public static String getCatalogName (String file) {// file = Fichier UNIMARC décrivant une oeuvre
		
		SubZoneContent subZ = new SubZoneContent () ;
		String subZone = subZ.getSubZoneContent(file, "144" , "k") ; // Récupérer le contenu de la sous-zone
	
		/****************************************/
		String[] caracter = subZone.split("");
		int x = 0 ;
		if (!(caracter.length==1)){
		while (!(caracter[x].equals(" "))) {
			catalogName = catalogName + caracter[x];
			x++;
		}
		}
		/****************************************/
	
		return catalogName;	
	}
}
