package selfContainedExpressionDev;

public class OpusSubNumber {

	static String opusSubNumber = "";
	
	public static String getOpusSubNumber (String file) {// file = Fichier UNIMARC décrivant une oeuvre
		
		SubZoneContent subZ = new SubZoneContent () ;
		String subZone = subZ.getSubZoneContent(file, "144" , "p") ; // Récupérer le contenu de la sous-zone
		
		/***************************************************************/
		String[] caracter = subZone.split("");
		
		boolean t = false ;
		for (int x=0; x<caracter.length; x++){
			if ((caracter[x].equals("o"))) t = true ;
			else if (t==true) opusSubNumber = opusSubNumber + caracter[x];
		}
		/**************************************************************/

		return opusSubNumber;
	}
}

