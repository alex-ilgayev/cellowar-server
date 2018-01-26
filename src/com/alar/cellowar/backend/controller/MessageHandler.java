package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.*;

import java.util.LinkedList;
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
        // creating new client in required lists.
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
//                case REQUEST_AVAILABLE_CLIENTS:
//                    CelloWarGameManager.getInstance().getUsers((MessageRequestAvailableClients)msg);
//                    break;
//                case REQUEST_JOIN:
//                    CelloWarGameManager.getInstance().askJoinGame((MessageRequestJoin) msg);
//                    break;
                case REQUEST_JOIN_POOL:
                    CelloWarGameManager.getInstance().askJoinPool((MessageRequestJoinPool) msg);
                    break;
                case REQUEST_SET_MOVE:
                    CelloWarGameManager.getInstance().setFinishMove((MessageRequestSetMove)msg);
                    break;
//                case REQUEST_SMS:
//                    SudokuServerGameManager.getInstance().sendTextMessage((MessageRequestSms)msg);
//                    break;
//                case REQUEST_NEW_GAME:
//                    CelloWarGameManager.getInstance().startNewGame((MessageRequestNewGame)msg);
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
        // adding the client to the session pool.
//        UUID sessionId = msg.client.getGameId();
//        if(sessionId != null) {
//            Session session = TemporaryDB.getInstance().findSession(sessionId);
//            if(session == null) { // session exist.
//                TemporaryDB.getInstance().addAndReplaceSession(new Session(sessionId, new LinkedList<Client>()));
//            }
//            if(!session.getClientList().contains(msg.client))
//                session.getClientList().add(msg.client);
//        }

        if(msg.client == null || msg.id == null)
            return new Packet[0];

        Packet[] packets = MessageQueues.getInstance().getAwaitingPackets(msg.getClient(), msg.getId());
        return packets;
    }

    private void handleClient(Client client){

        MessageQueues.getInstance().addAndReplaceClient(client);

        //TODO:
        // change logic.
        // int his logic the client decides in which session he belongs to.
        // checking the validity of the session db on the server. (and update accordingly)
        for(Session session: TemporaryDB.getInstance().getAllSessions()) {
            if(session.getClientList().contains(client)) {
                // if another session contains that client. (or no session at all)
                if(client.getCurrSessionId() == null ||
                        (client.getCurrSessionId() != null &&
                                !client.getCurrSessionId().equals(session.getSessionId()))) {
                    if(client.getCurrSessionId() == null)
                        LOGGER.info("remove client: " + client.getName() + " from session: " +
                                "session.getSessionId()");
                    else
                        LOGGER.info("tranferring client: " + client.getName() + " from session: " +
                                "session.getSessionId()" +  " to session: " + client.getCurrSessionId().toString());
                    session.getClientList().remove(client);
                }
            }
        }

        if(client.getCurrSessionId() == null)
            return;
        // now updating the session data
        Session session = TemporaryDB.getInstance().findSession(client.getCurrSessionId());
        if(session == null) {
            //TODO:
//            LOGGER.info("creating new session in db: " + client.getCurrSessionId());
//            session = new Session(client.getCurrSession().getSessionId(), client.getCurrSession().getGameType());
//            session.getClientList().add(client);
//            TemporaryDB.getInstance().addAndReplaceSession(session);
            LOGGER.severe(ErrorStrings.SERVER_ERROR_CLIENT_SESSION_NOT_SYNCED_TO_DB);
            return;
        }

        LinkedList<Client> clients = session.getClientList();
        if(!clients.contains(client)) {
            LOGGER.info("Adding client to a active session" + client.getCurrSessionId());
            LOGGER.info("Now there is " + clients.size() + " clients in the session");
            clients.add(client);
        }
    }

    // clean the information once a "TIME_TO_CLEAN_QUEUES_MILLIS" time.
    // remove unreporting clients.
    // after removing all clients from the session,
    // removing empty sessions.
    private void cleanQueues(){
        long currTime = System.currentTimeMillis();
        if((currTime - _LAST_TIME_CLEANED_QUEUES) > Settings._TIME_TO_CLEAN_QUEUE_MILLIS){
            MessageQueues.getInstance().removeOldQueues();
            for(Session session: TemporaryDB.getInstance().getAllSessions()) {
                if(session.getClientList().size() == 0) {
                    LOGGER.info("removing empty session");
                    TemporaryDB.getInstance().removeSession(session);
                }
            }
            _LAST_TIME_CLEANED_QUEUES = currTime;
        }
    }
}
