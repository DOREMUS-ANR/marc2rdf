package firstExecution;


public class DescripLibre {
	
	static String descripLibre  = "";
	
	public static String getDescripLibre (String file){
		
		/************************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("ex�cution"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("�x�cution"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("repr�sentation"))) {
		descripLibre = subZone.getSubZoneContent(file, "600" , "a") ;
		}
		/************************************************************************************/
		
		return descripLibre ;
	}

}
