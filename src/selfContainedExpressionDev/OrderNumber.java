package selfContainedExpressionDev;

public class OrderNumber {
	
	static String orderNumber ;
	
	public static String getOrderNumber (String file) { // file = Fichier UNIMARC décrivant une oeuvre
		
		SubZoneContent subZone = new SubZoneContent () ;
		
		orderNumber = subZone.getSubZoneContent(file, "144" , "n") ;
		
		return orderNumber ;
		
	}

}
