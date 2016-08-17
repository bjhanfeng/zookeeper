package org.apache.zookeeper;

import org.apache.zookeeper.client.IntelligenceConnectStringParser;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author hanfeng
 * @date 16/8/16
 */

public class InteligenceNormalConnectStringParserTest {

    @Test
    public void testoneIp() {
        IntelligenceConnectStringParser intelligenceConnectStringParser = new IntelligenceConnectStringParser("127.0.0.1:2181");
        List<InetSocketAddress> inetSocketAddresses = intelligenceConnectStringParser.getServerAddresses();
        Assert.assertArrayEquals(new InetSocketAddress[]{InetSocketAddress.createUnresolved("127.0.0.1", 2181)}, inetSocketAddresses.toArray());
    }

    @Test
    /**
     * my local ip is start with "10.75"
     */
    public void testMultipleInIp() {
        IntelligenceConnectStringParser intelligenceConnectStringParser = new IntelligenceConnectStringParser("10.75.11.11:2181,10.11.11.11:2181,10.36.22.1:2181,10.75.11.1:2181,10.22.11.1:2181");
        List<InetSocketAddress> inetSocketAddresses = intelligenceConnectStringParser.getServerAddresses();
        Assert.assertArrayEquals(new InetSocketAddress[]{InetSocketAddress.createUnresolved("10.75.11.11", 2181), InetSocketAddress.createUnresolved("10.75.11.1", 2181), InetSocketAddress.createUnresolved("10.36.22.1", 2181), InetSocketAddress.createUnresolved("10.11.11.11", 2181), InetSocketAddress.createUnresolved("10.22.11.1", 2181)}, inetSocketAddresses.toArray());
    }

}
