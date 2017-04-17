package org.cpsc8570.team4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IPSet {

    private IPRange srcIPRange;
    private IPRange dstIPRange;

    public IPSet(IPRange src, IPRange dst){
        this.srcIPRange = src;
        this.dstIPRange = dst;
    }

    public IPRange getSrcIPRange() {
        return srcIPRange;
    }

    public IPRange getDstIPRange() {
        return dstIPRange;
    }

    public static List<IPSet> cutOff(IPSet set0, IPSet set1){
        List<IPSet> ipSets = new ArrayList<>();

        Map<String, List<IPRange>> srcMap = IPRange.cutOff(set0.getSrcIPRange(), set1.getSrcIPRange());
        List<IPRange> srcCutoffIPRanges = srcMap.get("cutoff");
        List<IPRange> srcRemainIPRanges = srcMap.get("remain");

        Map<String, List<IPRange>> dstMap = IPRange.cutOff(set0.getDstIPRange(), set1.getDstIPRange());
//        List<IPRange> dstCutoffIPRanges = dstMap.get("cutoff");
        List<IPRange> dstRemainIPRanges = dstMap.get("remain");

        for(IPRange range0 : srcRemainIPRanges)
            ipSets.add(new IPSet(range0, set0.getDstIPRange()));

        for(IPRange range0 : srcCutoffIPRanges)
            for(IPRange range1 : dstRemainIPRanges)
                ipSets.add(new IPSet(range0, range1));

        return ipSets;
    }

    @Override
    public String toString() {
        return "srcIPRange:" + this.srcIPRange.toString() + ",dstIPRange:" + this.dstIPRange.toString() + ",";
    }
}
