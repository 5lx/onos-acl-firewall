package org.cpsc8570.team4.impl;


import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.cpsc8570.team4.AdvAclRule;
import org.cpsc8570.team4.AdvAclService;
import org.cpsc8570.team4.AdvAclRuleId;
import org.cpsc8570.team4.AdvAclStore;
import org.cpsc8570.team4.IPRange;
import org.cpsc8570.team4.IPSet;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;

import org.onosproject.core.IdGenerator;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowEntry;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.host.HostService;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of AdvAclService
  */
@Component(immediate = true)
@Service
public class AdvAclManager implements AdvAclService{

    private ApplicationId appId;
    private final Logger log = getLogger(getClass());

    private static IdGenerator idGenerator;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected AdvAclStore advAclStore;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.foo.app");
        idGenerator = coreService.getIdGenerator("AdvAclRuleIds");
        AdvAclRule.bindIdGenerator(idGenerator);
//        hostService.addListener(hostListener);
//        idGenerator = coreService.getIdGenerator("acl-ids");
//        AclRule.bindIdGenerator(idGenerator);
//        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
//        hostService.removeListener(hostListener);
        // TODO can be replaced with this.clearAcl()
//        flowRuleService.removeFlowRulesById(appId);
//        aclStore.clearAcl();
//        log.info("Stopped");
    }

    @Override
    public List<AdvAclRule> getAdvAclRules() {
        return advAclStore.getAdvAclRules();
    }

    @Override
    public AdvAclRule addAdvAclRule(AdvAclRule rule) {
        List<AdvAclRule> rules = this.getAdvAclRules();
        for(AdvAclRule r : rules)
            rule.cutOff(r);

        if(rule.isLeagal())
            advAclStore.addAdvAclRule(rule);

        // flow enforce

        // get device id
        Set<DeviceId> selectedDeviceIds = new HashSet<>();
        final Iterable<Host> hosts = hostService.getHosts();

        for(Host h : hosts)
            selectedDeviceIds.add(h.location().deviceId());

        for(IPSet ipSet : rule.getIpSets())
            for(String srcCidr : IPRange.iprange2cidr(ipSet.getSrcIPRange()))
                for(String dstCidr : IPRange.iprange2cidr(ipSet.getDstIPRange())){

                    for(DeviceId deviceId : selectedDeviceIds){
                        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
                        TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder();
                        FlowEntry.Builder flowEntry = DefaultFlowEntry.builder();

                        selectorBuilder.matchEthType(Ethernet.TYPE_IPV4);
                        selectorBuilder.matchIPSrc(Ip4Prefix.valueOf(srcCidr));
                        selectorBuilder.matchIPDst(Ip4Prefix.valueOf(dstCidr));

                        if (!rule.getAction().equals("DENY"))
                            treatment.add(Instructions.createOutput(PortNumber.CONTROLLER));

                        flowEntry.forDevice(deviceId);
                        flowEntry.withSelector(selectorBuilder.build());
                        flowEntry.withTreatment(treatment.build());
                        flowEntry.withPriority(30000);
                        flowEntry.fromApp(appId);

                        flowEntry.makePermanent();
                        flowRuleService.applyFlowRules(flowEntry.build());
                        advAclStore.addRuleToFlowMapping(rule.getId(), flowEntry.build());
                    }
                }

        return rule;
    }

    @Override
    public void clearByRuleId(AdvAclRuleId ruleId) {
        advAclStore.clearByRuleId(ruleId);

        Set<FlowRule> flowSet = advAclStore.getFlowByRule(ruleId);
        if (flowSet != null) {
            for (FlowRule flowRule : flowSet) {
                flowRuleService.removeFlowRules(flowRule);
            }
        }
        advAclStore.removeRuleToFlowMapping(ruleId);
    }

    @Override
    public void clearAll() {
        advAclStore.clearAll();
    }
}
