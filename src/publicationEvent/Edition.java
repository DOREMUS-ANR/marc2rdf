package publicationEvent;

import firstExecution.SubZoneContent;

public class Edition {
	
	static String note = "";
	
	public static String getEdition (String file){
		
		/************************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("�d"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("�dition"))) {
		note = subZone.getSubZoneContent(file, "600" , "a") ;
		}
		/************************************************************************************/
		
		return note;
	}

}
