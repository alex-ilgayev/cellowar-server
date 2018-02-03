package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.*;

import java.util.List;
import java.util.logging.Logger;

public class MessageHandler {
    private final static Logger LOGGER = Logger.getLogger("SudokuMessageHandler");
    private static long _LAST_TIME_CLEANED_QUEUES = System.currentTimeMillis();

    private static MessageHandler _ins = null;

    private MessageHandler(){

    }

    public static MessageHandler getInstance(){
        if(_ins == null)
            _ins = new MessageHandler();
        return _ins;
    }

    public Packet[] handleMessage(IMessage msg){
        if(msg.getMessageType() != MessageType.REQUEST_POLL_MESSAGE_QUEUE)
            LOGGER.info("handling message number: " + msg.getMessageType());
        cleanQueues();
        handleClient(msg.getClient());
        // clean queues once a while. when someone polling clean queues after polling (not to lose messages)
        // when sending message first cleaning then sending (not to receive incorrect information).
        try {
            switch (msg.getMessageType()){
                case REQUEST_POLL_MESSAGE_QUEUE:
                    Packet[] result =  pollMessageQueue((MessageRequestPollMessageQueue)msg);
                    return result;
                case REQUEST_JOIN_POOL:
                    CelloWarGameManager.getInstance().askJoinPool((MessageRequestJoinPool) msg);
                    break;
                case REQUEST_SET_MOVE:
                    CelloWarGameManager.getInstance().setFinishMove((MessageRequestSetMove)msg);
                    break;
//                case REQUEST_SMS:
//                    SudokuServerGameManager.getInstance().sendTextMessage((MessageRequestSms)msg);
//                    break;
            }
            // INSERT TO DB.
        } catch(Exception e) {
            LOGGER.severe(ErrorStrings.SERVER_ERROR_UNKNOWN_MESSAGE_TYPE);
            return new Packet[0];
        }
        return new Packet[0];
    }

    private Packet[] pollMessageQueue(MessageRequestPollMessageQueue msg){
        if(msg.client == null || msg.id == null)
            return new Packet[0];

        Packet[] packets = MessageQueues.getInstance().getAwaitingPackets(msg.getClient(), msg.getId());
        return packets;
    }

    private void handleClient(Client client){

        TemporaryDB.getInstance().addAndReplaceClient(client);

        if(client.getCurrSessionId() == null)
            return;

        Session session = TemporaryDB.getInstance().findSession(client.getCurrSessionId());
        if(session == null) {
            LOGGER.severe(ErrorStrings.SERVER_ERROR_CLIENT_SESSION_NOT_SYNCED_TO_DB);
            return;
        }

        List<Client> clients = session.getClientList();
        if(!clients.contains(client)) {
            LOGGER.severe("Client is not included in given session.");
        }
    }

    // clean the information once a "TIME_TO_CLEAN_QUEUES_MILLIS" time.
    // remove unreporting clients.
    // after removing all clients from the session,
    // removing empty sessions.
    private void cleanQueues(){
        long currTime = System.currentTimeMillis();
        if((currTime - _LAST_TIME_CLEANED_QUEUES) > Settings._TIME_TO_CLEAN_QUEUE_MILLIS){
            TemporaryDB.getInstance().removeOldQueues();
            TemporaryDB.getInstance().removeUnusedSessions();
            _LAST_TIME_CLEANED_QUEUES = currTime;
        }
    }
}
