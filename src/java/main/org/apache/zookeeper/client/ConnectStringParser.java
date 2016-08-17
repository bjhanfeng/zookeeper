package org.apache.zookeeper.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * @author hanfeng
 * @date 16/8/17
 */
public interface ConnectStringParser {
    public String getChrootPath();
    public ArrayList<InetSocketAddress> getServerAddresses();
}
