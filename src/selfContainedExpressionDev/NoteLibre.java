package selfContainedExpressionDev;

import complexWork.SubZoneContent;

public class NoteLibre {
	
	static String note = "";
	
	public static String getNoteLibre (String file){
		
		/******************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("comp"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("1re �d."))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("�d."))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("�dition"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("1re ex�cution"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("1re repr�sentation"))
				) {
		note = subZone.getSubZoneContent(file, "600" , "a") ;
		}		
		/************************************************************************************/
		
		return note ;
		
	}

}
