package complexWork;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Title {
	
	static String title ;
	
	public static String getTitle (String file) throws IOException{
		
		SubZoneContent subZone = new SubZoneContent ();
		String dollA = subZone.getSubZoneContent(file, "144", "a");
		String codeGenre = "sn#";
		
		/*******************************************************************/
		Boolean trouve = false ; // "trouve=true" si "codeGenre" a été trouvé dans le fichier
		 File fichier = new File("Data\\GenresBNF.xlsx");
        FileInputStream fis = new FileInputStream(fichier);

        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis); // Trouver l'instance workbook du fichier XLSX
        XSSFSheet mySheet = myWorkBook.getSheetAt(0); // Retourne la 1ere feuille du workbook XLSX
        Iterator<Row> iter = mySheet.iterator(); //Itérateur de toutes les lignes de la feuille courante
       
        while ((iter.hasNext())&&(trouve==false)) { // Traverser chaque ligne du fichier XLSX
        	Boolean correspondance = false ; // correspondance entre 144 $a et le genre courant parcouru
       	    Row row = iter.next();
            Iterator<Cell> cellIterator = row.cellIterator(); 
            int numColonne = 0; 
            while (cellIterator.hasNext()) { // Pour chaque ligne, itérer chaque colonne
                Cell cell = cellIterator.next();
                if (codeGenre.equals(cell.getStringCellValue()) ) trouve = true ; //On a trouvé le code du genre dans le fichier
                if ((trouve == true)&&(numColonne > 0)) {
               	 	if ((dollA.equals( cell.getStringCellValue() ))) { // 144 $a correspond au genre
               	 		correspondance = true ;
               	 }
                } 
                numColonne ++;
            }
            if ((correspondance == false)&&(trouve == true)) {
            	
            	SubZoneContent subZ1 = new SubZoneContent ();
            	SubZoneContent subZ2 = new SubZoneContent ();
            	SubZoneContent subZ3 = new SubZoneContent ();
            	
            	title = subZ1.getSubZoneContent(file, "144", "a")+".";
       	 		title = title + subZ2.getSubZoneContent(file, "144", "h")+".";
       	 		title = title + subZ3.getSubZoneContent(file, "144", "i");
       	 		
       	 		if (title.contains("..")) {
       	 			SubZoneContent subZ = new SubZoneContent ();
       	 			title = subZ.getSubZoneContent(file, "144", "a");
       	 		}
            }
        }		
		/*******************************************************************/
		
		return title ;
	}

}
