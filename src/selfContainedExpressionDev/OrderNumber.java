package selfContainedExpressionDev;

public class OrderNumber {
	
	static String orderNumber ;
	
	public static String getOrderNumber (String file) { // file = Fichier UNIMARC d�crivant une oeuvre
		
		SubZoneContent subZone = new SubZoneContent () ;
		
		orderNumber = subZone.getSubZoneContent(file, "144" , "n") ;
		
		return orderNumber ;
		
	}

}
