package org.doremus.marc2rdf.bnfMarcXML;

import java.util.ArrayList;
import java.util.List;

public class RecordList {

  private List<Record> list;
  private boolean eof = false; //Boolean qui indique "la fin de fichier"

  /******************* Constructeur **********************************/
  public RecordList() {
    list = new ArrayList<Record>();
  }

  /************* Mettre l'objet dans la pile *************************/
  public void push(Record record) {
    list.add(record);
  }

  /************** R�cup�rer l'objet de la pile ***********************/
  public synchronized Record pop() {
    Record record = null;
    if (list.size() > 0)
      record = (Record) list.remove(0);
    return record;
  }

  /*********** Retourne "true" s'il existe encore des objets dans la pile ******/
  public boolean hasNext() {
    if (!isEmpty() || !eof)
      return true;
    return false;
  }

  /************** Si la fin du document est atteinte ***************************/
  public void end() {
    eof = true;
  }

  /************* retourne "true" si la pile est vide **************************/
  private boolean isEmpty() {
    return (list.size() == 0);
  }

}