package org.doremus.marc2rdf.marcparser;

import java.util.ArrayList;
import java.util.List;

public class RecordList {

  private List<Record> list;
  private boolean eof = false; //Boolean qui indique "la fin de fichier"

  /*******************
   * Constructeur
   **********************************/
  public RecordList() {
    list = new ArrayList<>();
  }

  /*************
   * Mettre l'objet dans la pile
   *************************/
  public void push(Record record) {
    list.add(record);
  }

  /**************
   * Recuperer l'objet de la pile
   ***********************/
  public synchronized Record pop() {
    if (list.size() > 0)
      return list.remove(0);
    return null;
  }

  /***********
   * Retourne "true" s'il existe encore des objets dans la pile
   ******/
  public boolean hasNext() {
    return !isEmpty() || !eof;
  }

  /**************
   * Si la fin du document est atteinte
   ***************************/
  public void end() {
    eof = true;
  }

  /*************
   * retourne "true" si la pile est vide
   **************************/
  private boolean isEmpty() {
    return (list.size() == 0);
  }

}
