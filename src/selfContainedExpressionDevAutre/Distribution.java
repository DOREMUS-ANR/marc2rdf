package selfContainedExpressionDevAutre;

public class Distribution {

static String hasNote ;
	
	public static String getDistribution (String file) { // file = Fichier UNIMARC d�crivant une oeuvre
		
		SubZoneContent subZone = new SubZoneContent () ;
		
		hasNote = subZone.getSubZoneContent(file, "144" , "b") ;
		
		return hasNote ;
		
	}
	
}
