package main;

import identifierAssignment.IdentifierAssignment;
import identifierAssignmentOther.IdentifierAssignmentOther;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import selfContainedExpression.SelfContainedExpression;
import work.Work;
import expressionCreation.ExpressionCreation;

public class Main {
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		/****************************************************************************************/
		Work work = new Work();
		System.out.println("WORK");
		System.out.println("   "+"was assigned by : "+work.getBiblioAgency("Data\\XMLFile.xml"));
		System.out.println("   "+"identifier assignment (identifier) : "+work.getIdentifier("Data\\XMLFile.xml"));
		/****************************************************************************************/
		ExpressionCreation expression = new ExpressionCreation();
		System.out.println("Expression Creation");
		System.out.println("   "+"Date of the work (Machine format): "+expression.getDateMachine("Data\\XMLFile.xml"));
		System.out.println("   "+"Date of the work (Text format): "+expression.getDateText("Data\\XMLFile.xml"));
		System.out.println("   "+"is created by : "+expression.getComposer("Data\\XMLFile.xml"));
		/****************************************************************************************/
		SelfContainedExpression sce = new SelfContainedExpression();
		System.out.println("SELF-CONTAINED EXPRESSION");
		System.out.println("   "+"Context for the expression (Dédicace) : "+sce.getDedicace("Data\\XMLFile.xml"));
		System.out.println("   "+"Catalog : "+sce.getCatalog("Data\\XMLFile.xml"));
		System.out.println("   "+"Catalog Name : "+sce.getCatalogName("Data\\XMLFile.xml"));
		System.out.println("   "+"Catalog Number : "+sce.getCatalogNumber("Data\\XMLFile.xml"));
		System.out.println("   "+"Opus : "+sce.getOpus("Data\\XMLFile.xml"));
		System.out.println("   "+"Opus Number : "+sce.getOpusNumber("Data\\XMLFile.xml"));
		System.out.println("   "+"Opus SubNumber : "+sce.getOpusSubNumber("Data\\XMLFile.xml"));
		System.out.println("   "+"Note : "+sce.getNote("Data\\XMLFile.xml"));
		System.out.println("   "+"Key : "+sce.getKey("Data\\XMLFile.xml"));
		System.out.println("   "+"Order Number : "+sce.getOrderNumber("Data\\XMLFile.xml"));
		System.out.println("   "+"Genre : "+sce.getGenre("Data\\XMLFile.xml"));
		System.out.println("   "+"Bibliographic Agency : "+sce.getBiblioAgency("Data\\XMLFile.xml"));
		/****************************************************************************************/
		IdentifierAssignment ia = new IdentifierAssignment ();
		System.out.println("IDENTIFIER ASSIGNMENT");
		System.out.println("   "+"Bibliographic Agency : "+ia.getBiblioAgency("Data\\XMLFile.xml"));
		System.out.println("   "+"Identifiant Creator : "+ia.getCreator("Data\\XMLFile.xml"));
		System.out.println("   "+"Catalog : "+ia.getCatalog("Data\\XMLFile.xml"));
		System.out.println("   "+"Opus : "+ia.getOpus("Data\\XMLFile.xml"));
		System.out.println("   "+"Distribution : "+ia.getDistribution("Data\\XMLFile.xml"));
		System.out.println("   "+"Order Number : "+ia.getOrderNumber("Data\\XMLFile.xml"));
		System.out.println("   "+"Key : "+ia.getKey("Data\\XMLFile.xml"));
		System.out.println("   "+"Langue : "+ia.getLangue("Data\\XMLFile.xml"));
		System.out.println("   "+"Genre : "+ia.getGenre("Data\\XMLFile.xml"));
		System.out.println("   "+"Date (Year) : "+ia.getDateYear("Data\\XMLFile.xml"));
		System.out.println("   "+"Version : "+ia.getVersion("Data\\XMLFile.xml"));
		System.out.println("   "+"Documentation : "+ia.getDocumentation("Data\\XMLFile.xml"));
		/****************************************************************************************/
		IdentifierAssignmentOther iao = new IdentifierAssignmentOther ();
		System.out.println("IDENTIFIER ASSIGNMENT FOR ANOTHER EXPRESSION");
		System.out.println("   "+"Bibliographic Agency : "+iao.getBiblioAgency("Data\\XMLFile.xml"));
		System.out.println("   "+"Catalog : "+iao.getCatalog("Data\\XMLFile.xml"));
		System.out.println("   "+"Opus : "+iao.getOpus("Data\\XMLFile.xml"));
		System.out.println("   "+"Genre : "+iao.getGenre("Data\\XMLFile.xml"));
		System.out.println("   "+"Distribution : "+iao.getDistribution("Data\\XMLFile.xml"));
		System.out.println("   "+"Order Number : "+iao.getOrderNumber("Data\\XMLFile.xml"));
		System.out.println("   "+"Key : "+iao.getKey("Data\\XMLFile.xml"));
		System.out.println("   "+"Langue : "+iao.getLangue("Data\\XMLFile.xml"));
		System.out.println("   "+"Date (Year) : "+iao.getDateYear("Data\\XMLFile.xml"));
		System.out.println("   "+"Version : "+iao.getVersion("Data\\XMLFile.xml"));
		System.out.println("   "+"Derivation Type : "+iao.getDerivationType("Data\\XMLFile.xml"));
		/****************************************************************************************/
	}
}
