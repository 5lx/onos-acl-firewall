package org.cpsc8570.team4;

import java.util.List;

public interface AdvAclService {

    List<AdvAclRule> getAdvAclRules();

    AdvAclRule addAdvAclRule(AdvAclRule rule);

    void clearByRuleId(AdvAclRuleId ruleId);

    void clearAll();

}
