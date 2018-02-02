package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.*;
import com.alar.cellowar.shared.messaging.*;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.util.*;
import java.util.logging.Logger;

public class CelloWarGameManager {
    private final static Logger LOGGER = Logger.getLogger("CelloWarGameManager");
    private static CelloWarGameManager _ins = null;

    // TODO
    // indicates if someone already searching.
    private boolean isSearched = false;
    // session UUID for the waiting players.
    private UUID searchedUUID = null;
    // the waiting clients for a pairing.
    private List<Client> waitingClients = null;

    private CelloWarGameManager(){

    }

    public static CelloWarGameManager getInstance(){
        if(_ins == null)
            _ins = new CelloWarGameManager();
        return _ins;
    }

    public void askJoinPool(MessageRequestJoinPool msg) {
        if(msg.client == null || msg.id == null)
            return;

        Client requestingClient = msg.client;
        UUID requestingId = msg.id;

        // logic goes like this:
        // the first player waits gets session UUID which sends to the client.
        // the session has information which says it shouldn't start a game.
        // the next one joining getting the same UUID, untill full group formed, then packet new session sent to all.

        CelloWarGameData data = BoardGenerator.createNewBoard();

//        MatchingPool.getInstance().setSingleJoin();

        Session sessionReturned = MatchingPool.getInstance().joinPool(requestingClient);
        sessionReturned.setGameData(data);

        for(Client c : sessionReturned.getClientList()) {
            MessageResponseSession sessionMsg;
            sessionMsg = new MessageResponseSession();
            sessionMsg.responseClient = c;
            sessionMsg.responseId = requestingId;
            sessionMsg.activeSession = sessionReturned;

            Packet p = new Packet();
            p.date = System.currentTimeMillis();
            p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(sessionMsg));

            MessageQueues.getInstance().addPacket(c, p);
        }
    }

//    public void getUsers(MessageRequestAvailableClients msg){
//        if(msg.client == null || msg.id == null)
//            return;
//        Client requestingClient = msg.client;
//        UUID requestingId = msg.id;
//
//        Client[] clients = TemporaryDB.getInstance().getPlayingClients();
//        MessageResponseClientList returnMsg = new MessageResponseClientList();
//        returnMsg.clients = clients;
//        returnMsg.responseId = requestingId;
//        returnMsg.responseClient = requestingClient;
//
//        Packet p = new Packet();
//        p.date = System.currentTimeMillis();
//        p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(returnMsg));
//
//        MessageQueues.getInstance().addPacket(requestingClient, p);
//    }

    public void setFinishMove(MessageRequestSetMove msg) {
        if (msg.client == null || msg.client.getCurrSessionId() == null || msg.move == null)
            return;
        CelloWarGameData data = msg.move;
        UUID gameId = msg.client.getCurrSessionId();

        CelloWarGameData serverData = TemporaryDB.getInstance().findSession(gameId).getGameData();
        if (serverData == null) {
            LOGGER.severe(ErrorStrings.SERVER_ERROR);
            return;
        }

        switch (serverData.state) {
            case ANT_PLACEMENT:
                // means first player sent his move.
                serverData.ants.addAll(data.ants);
                serverData.state = CelloWarGameData.State.WAIT_FOR_OTHER;
                break;
            case SHOW_RESULT:
                // should do nothing.
                break;
            case WAIT_FOR_OTHER:
                // means it's the second player sending now.
                serverData.ants.addAll(data.ants);
                serverData.state = CelloWarGameData.State.SHOW_RESULT;
                break;
        }
    }

        //TODO:
//    public void sendTextMessage(MessageRequestSms msg) {
//        if(msg.client == null || msg.id == null || msg.text == null
//                || msg.client.getCurrSessionId() == null || msg.client.getName() == null)
//            return;
//        String name = msg.client.getName();
//        String text = msg.text;
//        UUID gameId = msg.client.getCurrSessionId();
//        Client clientExcluded = msg.client;
//        UUID responseId = msg.id;
//
//        Session session = null;
//        session = TemporaryDB.getInstance().findSession(gameId);
//        if(session == null)
//            return;
//        LOGGER.info("about to post text message to session " + session.getSessionId());
//        for(Client client: session.getClientList()) {
//            if(client.equals(clientExcluded))
//                continue;
//
//            LOGGER.info("about to post text message to client " + client.getId());
//
//            MessageResponseSms returnMsg = new MessageResponseSms();
//            returnMsg.responseClient = client;
//            returnMsg.responseId = responseId;
//            returnMsg.name = name;
//            returnMsg.text = text;
//
//            Packet p = new Packet();
//            p.date = System.currentTimeMillis();
//            p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(returnMsg));
//
//            MessageQueues.getInstance().addPacket(client, p);
//        }
//    }
}
