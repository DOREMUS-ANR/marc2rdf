package selfContainedExpressionDev;

public class Catalog {
	
	static String hasNote ;
	
	CatalogName catalogName ;
	
	CatalogNumber catalogNumber ;
	
public static String getCatalog (String file) { // file = Fichier UNIMARC d�crivant une oeuvre
		
	SubZoneContent subZone = new SubZoneContent () ;
	
	hasNote = subZone.getSubZoneContent(file, "144" , "k") ;
	
	return hasNote;
	
}

}
