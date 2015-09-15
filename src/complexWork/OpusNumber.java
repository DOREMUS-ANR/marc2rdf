package complexWork;

public class OpusNumber {
	
	static String opusNumber = "" ;
	
	public static String getOpusNumber (String file) {// file = Fichier UNIMARC décrivant une oeuvre
		
		SubZoneContent subZ = new SubZoneContent () ;
		String subZone = subZ.getSubZoneContent(file, "144" , "p") ; // Récupérer le contenu de la sous-zone
		
		/****************************************/
		String[] caracter = subZone.split("");
		int x = 5 ;
		while (!(caracter[x].equals(","))) {
			opusNumber = opusNumber + caracter[x];
			x++;
		}
		/****************************************/
		
		return opusNumber;
	}

}
