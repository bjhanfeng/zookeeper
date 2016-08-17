package org.apache.zookeeper.client;

import org.apache.zookeeper.common.PathUtils;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author hanfeng
 * @date 16/8/16
 */

public class IntelligenceConnectStringParser implements ConnectStringParser {
    private static final int DEFAULT_PORT = 2181;
    private static final String DEFAULT_LOCALIP = "127.0.0.1";
    public static final String NAME = "intelligence";
    public final static String IP_REGEX = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";

    private final String chrootPath;

    private final ArrayList<InetSocketAddress> serverAddresses = new ArrayList<InetSocketAddress>();

    /**
     * @throws IllegalArgumentException for an invalid chroot path.
     */
    public IntelligenceConnectStringParser(String connectString) {
        // parse out chroot, if any

        String ip = getLocalIp();
        int off = connectString.indexOf('/');
        if (off >= 0) {
            String chrootPath = connectString.substring(off);
            // ignore "/" chroot spec, same as null
            if (chrootPath.length() == 1) {
                this.chrootPath = null;
            } else {
                PathUtils.validatePath(chrootPath);
                this.chrootPath = chrootPath;
            }
            connectString = connectString.substring(0, off);
        } else {
            this.chrootPath = null;
        }

        String hostsList[] = connectString.split(",");
        List<Host> hosts = new ArrayList<Host>(hostsList.length);
        for (String host : hostsList) {
            int port = DEFAULT_PORT;
            int pidx = host.lastIndexOf(':');
            if (pidx >= 0) {
                // otherwise : is at the end of the string, ignore
                if (pidx < host.length() - 1) {
                    port = Integer.parseInt(host.substring(pidx + 1));
                }
                host = host.substring(0, pidx);
                hosts.add(new Host(host, port));
            }
        }

        priorsSort(hosts, ip);
        for (Host host : hosts) {
            serverAddresses.add(InetSocketAddress.createUnresolved(host.host, host.port));
        }
    }

    private void priorsSort(List<Host> hosts, String ip) {
        if (hosts.size() < 2) return;
        for (int i = hosts.size() - 1, j = 0; j <= i; i--) {

            Host host = hosts.get(i);
            if (isSameSegment(host.host, ip)) {
                for (; j < hosts.size(); j++) {  //查找第一个可以替换的位置
                    Host host2 = hosts.get(j);
                    if (!isSameSegment(host2.host, ip)) {
                        break;
                    }
                }
                if (j >= i) break;  //找到的位置已经到达
                Host hosta = hosts.get(j);
                hosts.set(j, host);
                hosts.set(i, hosta);
                j++;
            }
        }

    }

    private boolean isSameSegment(String host, String ip) {
        String hostIp = null;
        if (Pattern.matches(IP_REGEX, host)) {
            hostIp = host;
        } else {
            try {
                InetAddress inetAddress = InetAddress.getByName(host);
                hostIp = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                return false;
            }
        }
        String[] hosta = hostIp.split("\\.");
        String[] ipa = ip.split("\\.");
        return hosta[0].equals(ipa[0]) && hosta[1].equals(ipa[1]);
    }

    class Host {
        public String host;
        public int port;

        Host(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private String getLocalIp() {

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

        } catch (SocketException e) {
            return DEFAULT_LOCALIP;
        }
        if (interfaces == null) {
            return DEFAULT_LOCALIP;
        }
        List<String> ipList = new ArrayList<String>();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address) {
                    String ip = address.getHostAddress();
                    if (!DEFAULT_LOCALIP.equals(ip)) {
                        ipList.add(ip);
                    }
                }
            }
        }
        if (ipList.size() == 0)
            return DEFAULT_LOCALIP;

        String[] ipPriors = {"10.", "192"};
        for (String ipPrior : ipPriors) {
            for (String ip : ipList) {
                if (ip.startsWith(ipPrior)) {
                    return ip;
                }
            }
        }
        return DEFAULT_LOCALIP;


    }

    public String getChrootPath() {
        return chrootPath;
    }

    public ArrayList<InetSocketAddress> getServerAddresses() {
        return serverAddresses;
    }
}
