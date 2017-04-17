package org.cpsc8570.team4;

import org.onosproject.net.flow.FlowRule;
import org.onosproject.store.Store;

import java.util.List;
import java.util.Set;

public interface AdvAclStore extends Store {

    List<AdvAclRule> getAdvAclRules();

    AdvAclRule getAdvAclRule(AdvAclRuleId ruleId);

    void addAdvAclRule(AdvAclRule rule);

    void clearByRuleId(AdvAclRuleId ruleId);

    void clearAll();

    void addRuleToFlowMapping(AdvAclRuleId ruleId, FlowRule flowRule);

    Set<FlowRule> getFlowByRule(AdvAclRuleId ruleId);

    void removeRuleToFlowMapping(AdvAclRuleId ruleId);

}
