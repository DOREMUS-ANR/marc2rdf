package firstExecution;


public class DescripLibre {
	
	static String descripLibre  = "";
	
	public static String getDescripLibre (String file){
		
		/************************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("exécution"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("éxécution"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("représentation"))) {
		descripLibre = subZone.getSubZoneContent(file, "600" , "a") ;
		}
		/************************************************************************************/
		
		return descripLibre ;
	}

}
