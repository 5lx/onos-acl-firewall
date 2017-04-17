package org.cpsc8570.team4;

import org.onosproject.core.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public final class AdvAclRule {

    private AdvAclRuleId id;
    private  List<IPSet> ipSets = new ArrayList<>();
    private String ipProto;
    private int dstPort;
    private String action;

    private static IdGenerator idGenerator;

    public static void bindIdGenerator(IdGenerator idGenerator) {
        AdvAclRule.idGenerator = idGenerator;
    }

    public List<IPSet> getIpSets() {
        return ipSets;
    }

    public String getAction() {
        return action;
    }

    public boolean isLeagal(){
        if(this.ipSets.size() > 0)
            return true;
        return false;
    }

    public AdvAclRule(String srcIpStart, String srcIpEnd, String dstIpStart,
                      String dstIpEnd, int dstPort, String ipProto,
                      String action){
        this.id = AdvAclRuleId.valueOf(idGenerator.getNewId());
        this.ipSets.add(new IPSet(new IPRange(srcIpStart, srcIpEnd), new IPRange(dstIpStart, dstIpEnd)));
        this.ipProto = ipProto;
        this.dstPort = dstPort;
        this.action = action;
    }

    public AdvAclRuleId getId(){
        return this.id;
    }

    /**
     * cutoff overlap ip from this object
     * @param rule
     */
    public void cutOff(AdvAclRule rule){

        if(!this.action.equals(rule.getAction())){

            for(IPSet cutSet : rule.ipSets){

                List<IPSet> temp = new ArrayList<>();

                for(IPSet oSet: this.ipSets)
                    temp.addAll(IPSet.cutOff(oSet, cutSet));
                this.ipSets = temp;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id:").append(id.toString()).append(",");
        sb.append("ipProto:").append(ipProto).append(",");
        sb.append("dstPort:").append(dstPort).append(",");
        sb.append("action:").append(action).append(",");
        sb.append("ipSets:{");
        for (IPSet ipSet : this.ipSets)
            sb.append(ipSet.toString());
        sb.append("},");
        return sb.toString();
    }
}
