package org.cpsc8570.team4.impl;


import com.google.common.collect.Collections2;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.cpsc8570.team4.AdvAclRule;
import org.cpsc8570.team4.AdvAclRuleId;
import org.cpsc8570.team4.AdvAclStore;
import org.cpsc8570.team4.IPRange;
import org.cpsc8570.team4.IPSet;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Versioned;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component(immediate = true)
@Service
public class AdvAclHolder extends AbstractStore implements AdvAclStore{

    private ConsistentMap<AdvAclRuleId, AdvAclRule> ruleSet;
    private ConsistentMap<AdvAclRuleId, Set<FlowRule>> ruleToFlow;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Activate
    public void activate() {
        ApplicationId appId = coreService.getAppId("org.foo.app");

        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(AdvAclRule.class)
                .register(AdvAclRuleId.class)
                .register(IPSet.class)
                .register(IPRange.class);

        ruleSet = storageService.<AdvAclRuleId, AdvAclRule>consistentMapBuilder()
                .withSerializer(Serializer.using(serializer.build()))
                .withName("advAcl-rule-set")
                .withApplicationId(appId)
                .withPurgeOnUninstall()
                .build();

        ruleToFlow = storageService.<AdvAclRuleId, Set<FlowRule>>consistentMapBuilder()
                .withSerializer(Serializer.using(serializer.build()))
                .withName("advRule-to-flow")
                .withApplicationId(appId)
                .withPurgeOnUninstall()
                .build();

    }

    @Override
    public List<AdvAclRule> getAdvAclRules() {
        List<AdvAclRule> rules = new ArrayList<>();
        rules.addAll(Collections2.transform(ruleSet.values(), Versioned::value));
        return rules;
    }

    @Override
    public AdvAclRule getAdvAclRule(AdvAclRuleId ruleId) {
        Versioned<AdvAclRule> rule = ruleSet.get(ruleId);
        if (rule != null) {
            return rule.value();
        } else {
            return null;
        }
    }

    @Override
    public void addAdvAclRule(AdvAclRule rule) {
        ruleSet.putIfAbsent(rule.getId(), rule);
    }

    @Override
    public void clearByRuleId(AdvAclRuleId ruleId) {
        ruleSet.remove(ruleId);
    }

    @Override
    public void clearAll() {
        ruleSet.clear();
        ruleToFlow.clear();
    }

    @Override
    public void addRuleToFlowMapping(AdvAclRuleId ruleId, FlowRule flowRule) {
        ruleToFlow.computeIf(ruleId,
                             flowRuleSet -> (flowRuleSet == null || !flowRuleSet.contains(flowRule)),
                             (id, flowRuleSet) -> {
                                 Set<FlowRule> newSet = new HashSet<>();
                                 if (flowRuleSet != null) {
                                     newSet.addAll(flowRuleSet);
                                 }
                                 newSet.add(flowRule);
                                 return newSet;
                             });
    }

    @Override
    public Set<FlowRule> getFlowByRule(AdvAclRuleId ruleId) {
        Versioned<Set<FlowRule>> flowRuleSet = ruleToFlow.get(ruleId);
        if (flowRuleSet != null) {
            return flowRuleSet.value();
        } else {
            return null;
        }
    }

    @Override
    public void removeRuleToFlowMapping(AdvAclRuleId ruleId) {
        ruleToFlow.remove(ruleId);
    }
}
