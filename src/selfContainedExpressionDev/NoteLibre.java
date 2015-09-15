package selfContainedExpressionDev;

import complexWork.SubZoneContent;

public class NoteLibre {
	
	static String note = "";
	
	public static String getNoteLibre (String file){
		
		/******************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("comp"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("1re éd."))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("éd."))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("édition"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("1re exécution"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("1re représentation"))
				) {
		note = subZone.getSubZoneContent(file, "600" , "a") ;
		}		
		/************************************************************************************/
		
		return note ;
		
	}

}
