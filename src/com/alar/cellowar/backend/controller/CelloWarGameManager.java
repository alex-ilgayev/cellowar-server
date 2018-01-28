package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.*;
import com.alar.cellowar.shared.messaging.*;

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

        TemporaryDB.getInstance().addAndReplaceJoinPool(msg.client);

        // logic goes like this:
        // the first player waits gets session UUID which sends to the client.
        // the session has information which says it shouldn't start a game.
        // the next one joining getting the same UUID, untill full group formed, then packet new session sent to all.

        Session session;
        UUID sessionId = null;
        List<Client> clientList = new LinkedList<>();
        HashMap<Integer, Integer> clientOrder = new HashMap<>();
        CelloWarGameData data = createNewBoard();

        //TODO
        // hardcoded 2 players.
        if(isSearched) {
            sessionId = searchedUUID;
            waitingClients.add(requestingClient);

            // there is a match. creating session object and sending to the clients.
            // creating player order and client list.
            for(int i=0; i<waitingClients.size(); i++) {
                clientOrder.put(waitingClients.get(i).getId(), i+1);
            }
            clientList = waitingClients;
            session = new Session(sessionId, clientList, data, clientOrder);
            session.setIsSearching(false);

            TemporaryDB.getInstance().addAndReplaceSession(session);

            // creating a message for each client.
            for(Client c : clientList) {
                MessageResponseSession sessionMsg;
                sessionMsg = new MessageResponseSession();
                sessionMsg.responseClient = c;
                sessionMsg.responseId = requestingId;
                sessionMsg.activeSession = session;

                Packet p = new Packet();
                p.date = System.currentTimeMillis();
                p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(sessionMsg));

                MessageQueues.getInstance().addPacket(c, p);
            }
            waitingClients = new LinkedList<>();
            searchedUUID = null;
            isSearched = false;
        }
        else { // first one.
            isSearched = true;
            searchedUUID = UUID.randomUUID();
            waitingClients = new LinkedList<>();
            waitingClients.add(requestingClient);

            sessionId = searchedUUID;
            clientList.add(requestingClient);
            clientOrder.put(requestingClient.getId(), 1);
            session = new Session(sessionId, clientList, data, clientOrder);
            session.setIsSearching(true);

            TemporaryDB.getInstance().addAndReplaceSession(session);

            MessageResponseSession sessionMsg = new MessageResponseSession();
            sessionMsg.responseClient = requestingClient;
            sessionMsg.responseId = requestingId;
            sessionMsg.activeSession = session;

            Packet p = new Packet();
            p.date = System.currentTimeMillis();
            p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(sessionMsg));

            MessageQueues.getInstance().addPacket(requestingClient, p);
            return;
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

    public CelloWarGameData createNewBoard () {
        CelloWarGameData m = new CelloWarGameData();

        m = new CelloWarGameData();
        m.setWH(100.0f, 100.0f);
        m.obst.add(new Obstacle(900.0f, 450.0f, 1100.0f, 650.0f));

        m.obst.add(new Obstacle(300.0f, 600.0f, 600.0f, 1300.0f));


        m.ants.add(new Antenna(200.0f, 50.0f, 50.0f, Antenna.AntennaType.TRANSMISSION ));
        m.ants.add(new Antenna(80.0f, 650.0f, 650.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));
        m.ants.add(new Antenna(150.0f, 650.0f, 650.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));

        m.ants.add(new Antenna(250.0f, 640.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));
        m.ants.add(new Antenna(250.0f, 620.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));

//        int numBlue = 5;
//        int numRed = 2;
//
//        Antenna ant;
//        for(int i=0; i<numBlue; i++) {
//            ant = new Antenna(generateRandomCoord(), generateRandomCoord(),
//                    generateRandomCoord(), Antenna.AntennaType.TRANSMISSION);
//
//        }
//        for(int i=0; i<numRed; i++) {
//            ant = new Antenna(generateRandomCoord(), generateRandomCoord(),
//                    generateRandomCoord(), Antenna.AntennaType.ELECTONIC_WARFARE);
//        }

        return m;
    }

    // generate float num between 10 to 90.
    private float generateRandomCoord() {
        return (float)((new Random()).nextInt(81)+10);
    }
}
