package selfContainedExpressionDev;

public class Opus {
	
	static String hasNote = "" ;
	
	OpusNumber opusNumber;
	
	OpusSubNumber opusSubNumber;
	
	
	public static String getOpus (String file) { // file = Fichier UNIMARC d�crivant une oeuvre
		
		SubZoneContent subZone = new SubZoneContent () ;
		
		hasNote = subZone.getSubZoneContent(file, "144" , "p") ;
		
		return hasNote;
	}
}
