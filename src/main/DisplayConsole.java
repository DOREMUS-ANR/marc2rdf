package main;

import java.util.ArrayList;

import dnl.utils.text.table.TextTable;

public class DisplayConsole {

	public void display (ArrayList<String> tab) {
		
		String[] columnNames = { "Subject", "Predicate", "Object"};                                          
		String[][] data = new String [tab.size()/3][3]; // Tableau � 2 dimensions pour l'affichage final
		int k = 0 ;
		for (int i = 0; i < tab.size()/3; i++){ // Parcourir toutes les lignes du tableau 
			for(int j = 0; j <3; j++){ // Parcourir toutes les colonnes du tableau
				data[i][j] = tab.get(k); // La case courante aura comme valeur celle de la case courante du tableau dynamique "tab"
				k++;
			} 
			} 

		TextTable tt = new TextTable(columnNames, data); 
		tt.setAddRowNumbering(true); // Num�roter les lignes du tableau � gauche 
		tt.printTable(); // Afficher le tableau 
	}
}
