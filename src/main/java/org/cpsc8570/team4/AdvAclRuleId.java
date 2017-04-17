package org.cpsc8570.team4;


import org.onlab.util.Identifier;

public final class AdvAclRuleId extends Identifier<Long> {

    public static AdvAclRuleId valueOf(long value) {
        return new AdvAclRuleId(value);
    }

    AdvAclRuleId() {
        super(0L);
    }

    AdvAclRuleId(long value) {
        super(value);
    }

    public long fingerprint() {
        return identifier;
    }

    @Override
    public String toString() {
//        return "0x" + Long.toHexString(identifier);
        return Long.toString(identifier);
    }

}
