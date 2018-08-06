package org.doremus.marc2rdf.main;

/**
 * @author EvaFernandez
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileChooser extends JPanel implements ActionListener {
  /**
   * serialVersionUID added automatically
   */
  private static final long serialVersionUID = 1L;
  JButton chooseButton;
  JTextArea log;
  JFileChooser fc;
  String dir;
  Boolean choosed;

  public FileChooser() {
    super(new BorderLayout());
    choosed = false;
    //Create the log first, because the action listeners
    //need to refer to it.
    log = new JTextArea(1, 20);
    log.setMargin(new Insets(5, 5, 5, 5));
    log.setEditable(false);
    //Create a file chooser
    fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    chooseButton = new JButton("Choose Directory...");
    chooseButton.addActionListener(this);


    //For layout purposes, put the buttons in a separate panel
    JPanel buttonPanel = new JPanel(); //use FlowLayout

    log.setBackground(buttonPanel.getBackground());
    buttonPanel.add(chooseButton);
    buttonPanel.add(log);
    add(buttonPanel, BorderLayout.PAGE_START);
  }

  public void actionPerformed(ActionEvent e) {

    //Handle choose button action.
    System.out.println("Handling chooseButton...");
    if (e.getSource() == chooseButton) {
      int returnVal = fc.showOpenDialog(FileChooser.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        //This is where a real application would open the file.
        log.append("Converting " + file.getName() + " file.");
        dir = file.getAbsolutePath().toString();
        getInsets();
        choosed = true;
        System.out.println("Chosen Directory: " + dir);
        synchronized (this) {
          this.notify();
        }
      } else {
        log.append("Open command cancelled by user.");
      }
      log.setCaretPosition(log.getDocument().getLength());
    }
  }

  public String getDir() {
    return this.dir;
  }

  public Boolean getChoosed() {
    return this.choosed;
  }
}
