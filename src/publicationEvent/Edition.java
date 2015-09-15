package publicationEvent;

import firstExecution.SubZoneContent;

public class Edition {
	
	static String note = "";
	
	public static String getEdition (String file){
		
		/************************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("éd"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("édition"))) {
		note = subZone.getSubZoneContent(file, "600" , "a") ;
		}
		/************************************************************************************/
		
		return note;
	}

}
