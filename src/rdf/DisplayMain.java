package rdf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import publicationEvent.PublicationEvent;

import selfContainedExpressionDev.SelfContainedExpressionDev;
import selfContainedExpressionDevAutre.SelfContainedExpressiondevAutre;
import selfContainedExpressionId.SelfContainedExpressionId;
import selfContainedExpressionIdAutre.SelfContainedExpressionIdAutre;

import complexWork.ComplexWork;
import expressionCreationAutre.ExpressionCreationAutre;
import expressionCreationRepr.ExpressionCreationRepr;
import firstExecution.FirstExecution;

public class DisplayMain {
	
	public static String subject="";
	public static String predicate="";
	public static String object="";
	public static ArrayList<String> tab = new ArrayList<String>(); // Tableau dynamique pour ajouter les valeurs du tableau

	public static void main(String[] args) throws URISyntaxException, IOException {
		
		/**************** Rechercher le num�ro de notice dans le fichier MARC *******************/
		ConstructURI uri = new ConstructURI();
		subject = uri.get().toString();
		/****************************************************************************************/
		
		/******************** Extraire les triplets de la notice ********************************/
		// F15 : Complex Work - Description minimale non ambigue
		ComplexWork.execute();
		
		// F28 : Expression Creation - Creation de l'expression repr�sentative de l'oeuvre musicale
		ExpressionCreationRepr.execute();
		
		// F22 : Self Contained Expression - Description d�velopp�e de l'expression repr�sentative
		SelfContainedExpressionDev.execute();
		
		// Premi�re ex�cution de l'oeuvre
		FirstExecution.execute();
		
		// F30 : Publication Event - 1ere publication de l'oeuvre
		PublicationEvent.execute();
		
		// F22 : Self Contained Expression - Assignation d'identifiant pour l'expression repr�sentative
		SelfContainedExpressionId.execute();
		
		// F28 : Expression Creation - Creation d'une autre expression de l'oeuvre musicale
		ExpressionCreationAutre.execute();
		
		// F22 : Self Contained Expression - Description d�velopp�e d'une autre expression de l'oeuvre musicale
		SelfContainedExpressiondevAutre.execute();
		
		// F22 : Self Contained Expression - Assignation d'identifiant pour une autre expression de l'oeuvre musicale
		SelfContainedExpressionIdAutre.execute();
		
		/****************************************************************************************/
		
		/************ Afficher les triplets dans la console *********************/
		DisplayConsole console = new DisplayConsole();
		console.display(tab); //Afficher <Sujet,Pr�dicat,Objet> de la ligne courante
		/************************************************************************/
	}

}
