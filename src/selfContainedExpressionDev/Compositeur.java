package selfContainedExpressionDev;

public class Compositeur {
	
	static String compositeur = "";
	
	public static String getCompositeur (String file){
		
		/*******************************************************************/
		SubZoneContent subZone1 = new SubZoneContent () ;	
		compositeur = compositeur + subZone1.getSubZoneContent(file, "100" , "3") ;
		SubZoneContent subZone2 = new SubZoneContent () ;
		compositeur = compositeur + " "+ subZone2.getSubZoneContent(file, "100" , "w") ;
		SubZoneContent subZone3 = new SubZoneContent () ;
		compositeur = compositeur + " "+subZone3.getSubZoneContent(file, "100" , "a") ;
		SubZoneContent subZone4 = new SubZoneContent () ;
		compositeur = compositeur + " "+subZone4.getSubZoneContent(file, "100" , "m") ;
		SubZoneContent subZone5 = new SubZoneContent () ;
		compositeur = compositeur + " "+subZone5.getSubZoneContent(file, "100" , "d") ;
		SubZoneContent subZone6 = new SubZoneContent () ;
		compositeur = compositeur + " "+subZone6.getSubZoneContent(file, "100" , "e") ;
		SubZoneContent subZone7 = new SubZoneContent () ;
		compositeur = compositeur + " "+subZone7.getSubZoneContent(file, "100" , "h") ;
		SubZoneContent subZone8 = new SubZoneContent () ;
		compositeur = compositeur + " "+subZone8.getSubZoneContent(file, "100" , "u") ;	
		/*******************************************************************/
		
		return compositeur;
	}

}
