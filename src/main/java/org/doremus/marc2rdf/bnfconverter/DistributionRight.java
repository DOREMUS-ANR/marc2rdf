package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.Artist;
import org.doremus.ontology.CRO;

public class DistributionRight extends AbstractRight {
  public DistributionRight(String uri, Artist distributor, String year) {
    super(uri, distributor, year);
    this.setClass(CRO.DistributionRight);
  }
}
