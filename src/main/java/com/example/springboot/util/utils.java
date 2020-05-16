package com.example.springboot.util;

import com.github.samyuan1990.FabricJavaPool.ExecuteResult;
import com.github.samyuan1990.FabricJavaPool.FabricConnectionPoolFactory;
import com.github.samyuan1990.FabricJavaPool.Util;
import com.github.samyuan1990.FabricJavaPool.api.FabricConnection;
import com.google.protobuf.ByteString;
import org.apache.commons.pool2.ObjectPool;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.String.format;

public class utils {

    public static String NetWorkConfig = "./src/main/resources/Networkconfig.json";
    public static String configUserPath = "./src/main/resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore";
    private static ChaincodeID cci = Util.generateChainCodeID("mycc", "1.0");

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

    private  static  ObjectPool<FabricConnection>  fabricJavaPool = FabricConnectionPoolFactory.getPool(getUser(), "mychannel");

    //GenericObjectPool<FabricConnection> fabricJavaPool = FabricConnectionPoolFactory.getPool(getUser(),"mychannel");//new FabricJavaPool(getUser(), "mychannel");

    public static User getUser() {
        User appuser = null;
        File sampleStoreFile = new File(System.getProperty("user.home") + "/test.properties");
        if (sampleStoreFile.exists()) { //For testing start fresh
            sampleStoreFile.delete();
        }
        final SampleStore sampleStore = new SampleStore(sampleStoreFile);
        try {
            appuser = sampleStore.getMember("peer1", "Org1", "Org1MSP",
                    new File(String.valueOf(findFileSk(Paths.get(configUserPath).toFile()))),
                    new File("./src/main/resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appuser;
    }

    public static String QueryWithPool(){
        String rs = "";
        try {
            FabricConnection myConnection = fabricJavaPool.borrowObject();
            //rs = query(myChannel, "mycc", "query", "a");
            ExecuteResult result = myConnection.query("mycc", "query","a");
            rs = result.getResult();
            fabricJavaPool.returnObject(myConnection);
        } catch (Exception e){
            e.printStackTrace();
        }
        return rs;
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
            NetworkConfig networkConfig = NetworkConfig.fromJsonFile(new File(NetWorkConfig));
            hfclient.setUserContext(appuser);
            hfclient.loadChannelFromConfig("mychannel", networkConfig);
            myChannel = hfclient.getChannel("mychannel");
            myChannel.initialize();
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("mycc")
                    .setVersion("1.0")
                    .build();
            QueryByChaincodeRequest transactionProposalRequest = hfclient.newQueryProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn("query");
            transactionProposalRequest.setArgs("a");
            transactionProposalRequest.setUserContext(appuser);
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
