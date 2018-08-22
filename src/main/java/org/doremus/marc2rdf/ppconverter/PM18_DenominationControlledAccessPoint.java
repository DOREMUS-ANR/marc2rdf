package org.doremus.marc2rdf.ppconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.MUS;

public class PM18_DenominationControlledAccessPoint extends DoremusResource {
  public PM18_DenominationControlledAccessPoint(String identifier) {
    super(identifier);

    this.setClass(MUS.M18_Controlled_Access_Point_Denomination);
  }
}
