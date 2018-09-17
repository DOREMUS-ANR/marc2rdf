package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.MUS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class M6_Casting extends DoremusResource {
  private boolean containsInfo;
  private int detailNum;

  public M6_Casting(Record record, String uri) {
    super(record);
    this.setUri(uri);
    this.setClass(MUS.M6_Casting);

    containsInfo = false;
    detailNum = 0;

    if (record.isTUM()) // note
      record.getDatafieldsByCode("144", 'b').stream()
        .filter(Objects::nonNull)
        .filter(x -> !x.trim().isEmpty())
        .peek(this::addNote)
        .forEach(x -> containsInfo = true);

    // casting detail
    parseCastingDetail().stream()
      .filter(Objects::nonNull)
      .forEach(this::addCastingDetail);

    if (record.isDAV() && !doesContainsInfo()) {
      List<String> fields = record.getDatafieldsByCode(701, 4);
      fields.addAll(record.getDatafieldsByCode(711, 4));
      fields.stream()
        .map(FunctionMap::get).filter(Objects::nonNull)
        .map(FunctionMap::getMop).filter(Objects::nonNull)
        .map(mop-> new M23_Casting_Detail(mop, "1", false))
        .forEach(this::addCastingDetail);
    }
  }

  public boolean doesContainsInfo() {
    return containsInfo;
  }

  private void addCastingDetail(M23_Casting_Detail detail) {
    detail.setUri(this.uri + "/detail/" + ++detailNum);
    this.addProperty(MUS.U23_has_casting_detail, detail);
    this.containsInfo = true;
  }

  private List<M23_Casting_Detail> parseCastingDetail() {
    return record.getDatafieldsByCode("048").stream()
      .flatMap(df -> df.getSubfields().stream())
      .filter(x -> x.getCode() == 'a' || x.getCode() == 'b')
      .map(x -> {
        String castingDetail = x.getData();
        String mopCode = castingDetail.substring(0, 2);
        String mopNum = castingDetail.substring(2, 4);
        String iamlCode = Utils.itermarc2mop(mopCode);
        if (iamlCode == null) {
          log("Iaml code not found for mop: " + mopCode);
          return null;
        }
        return new M23_Casting_Detail(iamlCode, mopNum, x.getCode() == 'b');
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

}
