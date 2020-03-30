import com.google.protobuf.ByteString;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

public class TransactionMove {
    public static CompletableFuture<BlockEvent.TransactionEvent> moveAmount(HFClient client, Channel channel, ChaincodeID chaincodeID, String from, String to, String moveAmount, User user, ArrayList<ByteString> trace) throws Exception {

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setArgs(from, to, moveAmount);
        transactionProposalRequest.setProposalWaitTime(500);
        if (user != null) { // specific user use that
            //System.out.println(user.getName());
            //Enrollment enrollment = user.getEnrollment();
            //System.out.println(enrollment.getCert());
            //System.out.println(enrollment.getClass().getTypeName());
            /*if(enrollment instanceof  org.hyperledger.fabric.sdk.identity.IdemixEnrollment){
                System.out.println(((IdemixEnrollment) enrollment).getIpk().getAttributeNames());
                System.out.println(((IdemixEnrollment) enrollment).getOu());
                System.out.println(((IdemixEnrollment) enrollment).getCri());
            }
            System.out.println(user.getAccount());
            System.out.println(user);*/
            transactionProposalRequest.setUserContext(user);
        }

        System.out.printf("sending transaction proposal to all peers with arguments: move(%s,%s,%s)\n", from, to, moveAmount);

        Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(transactionProposalRequest);
        for (ProposalResponse response : invokePropResp) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                System.out.printf("Successful transaction proposal response Txid: %s from peer %s\n", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        // Check that all the proposals are consistent with each other. We should have only one set
        // where all the proposals above are consistent.
        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(invokePropResp);
        if (proposalConsistencySets.size() != 1) {
            //fail(format("Expected only one set of consistent move proposal responses but got %d", proposalConsistencySets.size()));
            System.out.printf("Expected only one set of consistent move proposal responses but got %d\n", proposalConsistencySets.size());
        }

        System.out.printf("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d\n",
                invokePropResp.size(), successful.size(), failed.size());
        //System.out.println(invokePropResp.iterator().next());

        //for (ProposalResponse proposalResponse :successful) {
        //    printInfo(proposalResponse, trace,proposalResponse.getPeer().getName());
        //}
        printInfo(((LinkedList<ProposalResponse>) successful).get(0), trace,((LinkedList<ProposalResponse>) successful).get(0).getPeer().getName());


        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

            throw new ProposalException(format("Not enough endorsers for invoke(move %s,%s,%s):%d endorser error:%s. Was verified:%b",
                    from, to, moveAmount, firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));
        }
        System.out.println("Successfully received transaction proposal responses.");

        ////////////////////////////
        // Send transaction to orderer
        System.out.printf("Sending chaincode transaction(move %s,%s,%s) to orderer.\n", from, to, moveAmount);
        if (user != null) {
            return channel.sendTransaction(successful, user);
        }

        return channel.sendTransaction(successful);
    }

    public static void printInfo(ProposalResponse proposalResponse,ArrayList<ByteString> trace,String PeerName) {
        try {
            // System.out.println(proposalResponse.getChaincodeID().getName());
            // System.out.println(proposalResponse.isVerified());
            //System.out.println(proposalResponse.getTransactionID());
            //ByteString sig = proposalResponse.getProposalResponse().getEndorsement().getSignature();
            //byte[] signature = sig.toByteArray();
            //System.out.println(signature);
            //System.out.println("sig");
            //System.out.println(sig.toStringUtf8());
            //Identities.SerializedIdentity endorser = Identities.SerializedIdentity
            //      .parseFrom(proposalResponse.getProposalResponse().getEndorsement().getEndorser());
            // System.out.println(endorser.getMspid());
            //System.out.println(endorser.getIdBytes().toStringUtf8());
            //System.out.println("Proposal Header");
            //System.out.println(proposalResponse.getProposal().getHeader().toStringUtf8());

            trace.add(proposalResponse.getProposal().getHeader());

            //System.out.println("Proposal payload");
            //System.out.println(proposalResponse.getProposal().getPayload().toStringUtf8());
            //System.out.println("Proposal getExtension");
            //System.out.println(proposalResponse.getProposal().getExtension().toStringUtf8());

            //System.out.println("Proposal Response messagebytes");
            //System.out.println(proposalResponse.getProposalResponse().getResponse().getMessageBytes().toStringUtf8());

            //System.out.println("Proposal Response playload");
            //System.out.println(proposalResponse.getProposalResponse().getPayload().toStringUtf8());
            //System.out.println("chaincode action payload "+new String(proposalResponse.getChaincodeActionResponsePayload(), StandardCharsets.US_ASCII));
            //System.out.println("Proposal Extension");
            //System.out.println(proposalResponse.getProposal().getExtension().toStringUtf8());
            //System.out.println(proposalResponse.getProposal().getPayload().toStringUtf8());


            //System.out.println(proposalResponse.getProposalResponse().getPayload().toStringUtf8());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
