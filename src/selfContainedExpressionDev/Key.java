package selfContainedExpressionDev;

import complexWork.SubZoneContent;

public class Key {
	
	static String key = "";
	
	public static String getKey (String file){
		
		/******************************************************************************/
		SubZoneContent subZone = new SubZoneContent () ;
		key = subZone.getSubZoneContent(file, "144" , "t") ;		
		/************************************************************************************/
		
		return key ;
	}

}
