package expressionCreationRepr;

import complexWork.SubZoneContent;

public class DateCreation {
	
	static String date  = "";
	
	public static String getDateCreation (String file) { // file = Fichier UNIMARC décrivant une oeuvre
		
		PositionZone p1 = new PositionZone();
		String content1 = p1.getPositionZone(file, "008", "27", "36");
		PositionZone p2 = new PositionZone();
		String content2 = p2.getPositionZone(file, "008", "37", "46");
		
		if (content2.equals("")) date = content1;
		else date = content1+"-"+content2;
		
		/***********************  Si date indiquée en texte libre ***************************/
		if (content1.equals("")&& content2.equals("")){
		SubZoneContent subZone = new SubZoneContent () ;
		if ((subZone.getSubZoneContent(file, "600" , "a").contains("comp."))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("Date de composition"))||
					(subZone.getSubZoneContent(file, "600" , "a").contains("Dates de composition"))) {
		date = subZone.getSubZoneContent(file, "600" , "a") ;
		}
		}
		/************************************************************************************/
		return date ;
	}

}
