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

    public static String networkConfig = "./src/main/resources/Networkconfig.json";
    public static String configUserPath = "./src/main/resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore";
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

    public static String QueryWithOutPool(){
        String payload = "";
        try {
            User appuser = null;
            File sampleStoreFile = new File(System.getProperty("user.home") + "/test.properties");
            if (sampleStoreFile.exists()) { //For testing start fresh
                sampleStoreFile.delete();
            }
            final SampleStore sampleStore = new SampleStore(sampleStoreFile);
            appuser = sampleStore.getMember("peer1", "Org1", "Org1MSP",
                    new File(String.valueOf(findFileSk(Paths.get(configUserPath).toFile()))),
                    new File("./src/main/resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem"));
            Channel myChannel;
            HFClient hfclient = HFClient.createNewInstance();
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            hfclient.setCryptoSuite(cryptoSuite);
            NetworkConfig networkConfig = NetworkConfig.fromJsonFile(new File(config_network_path));
            hfclient.setUserContext(appUser);
            hfclient.loadChannelFromConfig(channel, networkConfig);
            myChannel = hfclient.getChannel(channel);
            myChannel.initialize();
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chainCodeName)
                    .setVersion("1.0")
                    .build();
            HFClient hfclient = HFClient.createNewInstance();
            QueryByChaincodeRequest transactionProposalRequest = hfclient.newQueryProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn(fcn);
            transactionProposalRequest.setArgs(arguments);
            transactionProposalRequest.setUserContext(getUser());
            Collection<ProposalResponse> queryPropResp = myChannel.queryByChaincode(transactionProposalRequest);
            for (ProposalResponse response:queryPropResp) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    payload = response.getProposalResponse().getResponse().getPayload().toStringUtf8();
                }
            }
        } catch (Exception e) {
            System.out.printf(e.toString());
        }
        return payload;
    }
}
