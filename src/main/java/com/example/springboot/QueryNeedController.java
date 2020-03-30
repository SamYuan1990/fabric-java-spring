/****************************************************** 
 *  Copyright 2019 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */

package com.example.springboot;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import com.example.springboot.util.utils;
import com.google.protobuf.ByteString;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servlet implementation class QueryAllActiveNeedsServlet
 */
@RestController
public class QueryNeedController {
	private static final long serialVersionUID = 1L;

	@RequestMapping("/QueryNeed")
	public String index(){
		JSONObject req = new JSONObject();
		req.put("uname", "usr1");
		req.put("needId", "N1");
		return queryNeed(req);
	}

	private static String queryNeed(JSONObject req) {
		String stringResponse="";

		try {
			System.out.println("Start Running");
			NetworkConfig networkConfig = utils.loadConfig(utils.config_network_path);
			HFClient hfclient = HFClient.createNewInstance();
			CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			hfclient.setCryptoSuite(cryptoSuite);
			User appuser = null;
			File tempFile = File.createTempFile("teststore", "properties");
			tempFile.deleteOnExit();

			File sampleStoreFile = new File(System.getProperty("user.home") + "/test.properties");
			if (sampleStoreFile.exists()) { //For testing start fresh
				sampleStoreFile.delete();
			}
			ArrayList<ByteString> trace = null;
			final SampleStore sampleStore = new SampleStore(sampleStoreFile);
			trace = utils.x509Header;
			appuser = sampleStore.getMember("peer1", "Org1", "Org1MSP",
					new File(String.valueOf(utils.findFileSk(Paths.get(utils.config_user_path).toFile()))),
					new File(".././fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem"));

			hfclient.setUserContext(appuser);
			hfclient.loadChannelFromConfig("mychannel", networkConfig);
			System.out.println(networkConfig.getPeerNames());
			Channel mychannel = hfclient.getChannel("mychannel");
			mychannel.initialize();
			ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("mycc")
					.setVersion("1.0")
					.build();

			TransactionProposalRequest transactionProposalRequest = hfclient.newTransactionProposalRequest();
			transactionProposalRequest.setChaincodeID(chaincodeID);
			transactionProposalRequest.setFcn("queryAllNeeds");
			String[] arguments = { req.getString("needId")};
			transactionProposalRequest.setArgs(arguments);
			transactionProposalRequest.setProposalWaitTime(500);
			transactionProposalRequest.setUserContext(appuser);

			Collection<ProposalResponse> invokePropResp = mychannel.sendTransactionProposal(transactionProposalRequest);
			for (ProposalResponse response : invokePropResp) {
				if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
					System.out.printf("Successful transaction proposal response Txid: %s from peer %s\n", response.getTransactionID(), response.getPeer().getName());
					System.out.printf(response.toString());
					System.out.printf(new String(response.getChaincodeActionResponsePayload()));
					stringResponse =  new String(response.getChaincodeActionResponsePayload());
				}
			}

			mychannel.sendTransaction(invokePropResp);

		} catch (Exception e) {
			System.out.printf(e.toString());
		}
		return stringResponse;
	}
}
