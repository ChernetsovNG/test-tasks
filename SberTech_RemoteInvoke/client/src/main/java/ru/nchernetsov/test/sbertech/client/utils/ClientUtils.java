package ru.nchernetsov.test.sbertech.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class ClientUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ClientUtils.class);

    /**
     * Находим список MAC-адресов данного хоста. Будем считать его уникальными именем клиента
     */
    public static String getMacAddress() {
        try {
            List<String> macList = new ArrayList<>();

            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], i < mac.length - 1 ? "-" : ""));
                    }
                    macList.add(sb.toString());
                }
            }

            return macList.stream().collect(Collectors.joining("|"));
        } catch (SocketException e) {
            LOG.error(e.getMessage());
        }

        return "";
    }

}
