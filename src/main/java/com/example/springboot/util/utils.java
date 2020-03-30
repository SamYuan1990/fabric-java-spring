package com.example.springboot.util;

import com.google.protobuf.ByteString;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.NetworkConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import static java.lang.String.format;

public class utils {

    public static String config_network_path = "./src/main/resources/Networkconfig.json";
    public static String config_user_path = ".././fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore";
    public static ArrayList<ByteString> x509Header = new ArrayList<ByteString>();

    public static File findFileSk(File directory) {

        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

        if (null == matches) {
            throw new RuntimeException(format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
        }

        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(), matches.length));
        }

        return matches[0];

    }

    public static NetworkConfig loadConfig(String config_network_path) {
        try {
            return NetworkConfig.fromJsonFile(new File(config_network_path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
