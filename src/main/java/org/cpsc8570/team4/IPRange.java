package org.cpsc8570.team4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IPRange {

    private long ipStart, ipEnd;

    public IPRange(String ipStart, String ipEnd){
        this.ipStart = IPRange.ip2long(ipStart);
        this.ipEnd = IPRange.ip2long(ipEnd);
    }

    public IPRange(long ipStart, long ipEnd){
        this.ipStart = ipStart;
        this.ipEnd = ipEnd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(IPRange.long2ip(ipStart)).append("-");
        sb.append(IPRange.long2ip(ipEnd)).append("}");
        return sb.toString();
    }

    /**
     *
     * @param range0
     * @param range1
     * @return hashmap{'cutoff':List(IPRange), 'remain': List(IPRanges)}
     */
    public static Map<String, List<IPRange>> cutOff(IPRange range0, IPRange range1){
        List<IPRange> cutoff = new ArrayList<>();
        List<IPRange> remain = new ArrayList<>();
        Map<String, List<IPRange>> map = new HashMap<>();
        map.put("cutoff", cutoff);
        map.put("remain", remain);
        if(range0.ipEnd < range1.ipStart || range0.ipStart > range1.ipEnd){
            remain.add(range0);
            return map;
        }
        if(range1.ipStart <= range0.ipStart && range1.ipEnd >= range0.ipEnd){
            cutoff.add(range0);
            return map;
        }
        if(range0.ipStart < range1.ipStart && range0.ipEnd > range1.ipEnd){
            remain.add(new IPRange(range0.ipStart, range1.ipStart - 1));
            remain.add(new IPRange(range1.ipEnd + 1, range0.ipEnd));
            cutoff.add(range1);
            return map;
        }
        if(range1.ipStart <= range0.ipStart){
            remain.add(new IPRange(range1.ipEnd + 1, range0.ipEnd));
            cutoff.add(new IPRange(range0.ipStart, range1.ipEnd));
            return map;
        }
        if(range0.ipStart <= range1.ipStart){
            remain.add(new IPRange(range0.ipStart, range1.ipStart - 1));
            cutoff.add(new IPRange(range1.ipStart, range0.ipEnd));
            return map;
        }
        return map;
    }

    public static List<String> iprange2cidr(long ipStart, long ipEnd){
        long start = ipStart;
        long end = ipEnd;

        ArrayList<String> result = new ArrayList<String>();
        while ( end >= start ) {
            byte maxSize = 32;
            while ( maxSize > 0) {
                long mask = iMask( maxSize - 1 );
                long maskBase = start & mask;

                if ( maskBase != start ) {
                    break;
                }

                maxSize--;
            }
            double x = Math.log( end - start + 1) / Math.log( 2 );
            byte maxDiff = (byte)( 32 - Math.floor( x ) );
            if ( maxSize < maxDiff) {
                maxSize = maxDiff;
            }
            String ip = long2ip(start);
            result.add( ip + "/" + maxSize);
            start += Math.pow( 2, (32 - maxSize) );
        }

        return result;
    }

    public static List<String> iprange2cidr(IPRange ipRange) {
        return IPRange.iprange2cidr(ipRange.ipStart, ipRange.ipEnd);
    }

    public static List<String> iprange2cidr( String ipStart, String ipEnd ) {
        long start = ip2long(ipStart);
        long end = ip2long(ipEnd);

        return IPRange.iprange2cidr(start, end);
    }

    public static List<String> iprange2cidr( int ipStart, int ipEnd ) {
        long start = ipStart;
        long end = ipEnd;

        return IPRange.iprange2cidr(start, end);
    }

    private static long iMask(int s) {
        return Math.round(Math.pow(2, 32) - Math.pow(2, (32 - s)));
    }

    private static long ip2long(String ipstring) {
        String[] ipAddressInArray = ipstring.split("\\.");
        long num = 0;
        long ip = 0;
        for (int x = 3; x >= 0; x--) {
            ip = Long.parseLong(ipAddressInArray[3 - x]);
            num |= ip << (x << 3);
        }
        return num;
    }

    private static String long2ip(long longIP) {
        StringBuffer sbIP = new StringBuffer("");
        sbIP.append(String.valueOf(longIP >>> 24));
        sbIP.append(".");
        sbIP.append(String.valueOf((longIP & 0x00FFFFFF) >>> 16));
        sbIP.append(".");
        sbIP.append(String.valueOf((longIP & 0x0000FFFF) >>> 8));
        sbIP.append(".");
        sbIP.append(String.valueOf(longIP & 0x000000FF));

        return sbIP.toString();
    }
}
