package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

public class ConstructURI {
	
	static String identifiant;
	static String classe;

	public static URI get() throws URISyntaxException, UnsupportedEncodingException {
		
		/****************** CLASSE = R�cup�rer le type du document (200 ---> $b) *******************/
		SpecificSousZone typeDocument = new SpecificSousZone();
		classe = typeDocument.extract("200", "$b"); // Exemple : "musique imprim�e"
		
			/************ Mettre la 1ere lettre de chaque mot en MAJ et supprimer l'espace ********/
			String classe2 = ""; // La chaine de caract�res (APRES)
			Pattern patern = Pattern.compile(" "); // Le s�parateur est un espace
			String[] sousChaines = patern.split(classe); //Tokeniser la chaine
			for(int i = 0; i < sousChaines.length; i++) // Parcourir tous les mots de la chaine
			{
				{
					if(!sousChaines[i].equals("")) // Si le caract�re courant n'est pas un espace
					{ 
						String ch2 = sousChaines[i].substring(0, 1); // Affecter le mot � la nouvelle sous chaine
						ch2 = ch2.toUpperCase(); //Mettre la 1�re lettre du mot en MAJ
						ch2+= sousChaines[i].substring(1); 
						classe2+= ch2;
					}  
				}
			}
			classe = classe2; // Exemple : "MusiqueImprim�e"
		/******************************************************************************************/
		
		/*********** IDENTIFIANT = Rechercher le num�ro de notice dans le fichier MARC ************/
		String filePath = "Data\\1.Document.txt"; //Fichier UNIMARC d�crivant une oeuvre
		try{
		BufferedReader buff = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = buff.readLine()) != null) { //Parcourir toutes les lignes du fichier
			RDFConversion rdfConversion = new RDFConversion();
			String identif = rdfConversion.searchNumNotice(line); // Rechercher le num�ro de notice dans le fichier MARC courant
			if (!(identif.equals(""))) identifiant = identif;
		}
		}
		catch (IOException ioe) { System.out.println("Erreur --" + ioe.toString()); }
		/****************************************************************************************/
            
		 URIBuilder builder = new URIBuilder()
         .setScheme("http")
         .setHost("id.example.com")
         .setPath("/musique/"+ classe +"/"+ identifiant);
 
 URI uri = builder.build();
 return uri;
			
		}
		}
