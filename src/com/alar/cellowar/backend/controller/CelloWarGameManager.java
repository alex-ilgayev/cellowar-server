package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.*;
import com.alar.cellowar.shared.messaging.*;

import java.util.*;
import java.util.logging.Logger;

public class CelloWarGameManager {
    private final static Logger LOGGER = Logger.getLogger("CelloWarGameManager");
    private static CelloWarGameManager _ins = null;

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
        // TODO: currently 2
        List<Client> retClients = TemporaryDB.getInstance().findJoinPoolMatch(1);
        if(retClients == null) //no match
            return;

        // there is a match. creating session object and sending to the clients.
        CelloWarGameData data = createNewBoard();
        Session session;

        // creating player order.
        HashMap<Integer, Integer> clientOrder = new HashMap<>();
        for(int i=0; i<retClients.size(); i++) {
            clientOrder.put(retClients.get(i).getId(), i+1);
        }

        session = new Session(data, clientOrder);
        TemporaryDB.getInstance().addAndReplaceSession(session);

        // creating a message for each client.
        for(Client c : retClients) {
            MessageResponseSession sessionMsg = new MessageResponseSession();
            sessionMsg.responseClient = c;
            sessionMsg.responseId = requestingId;
            sessionMsg.activeSession = session;

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

    public CelloWarGameData createNewBoard () {
        CelloWarGameData m = new CelloWarGameData();

        m.obst.add(new Obstacle(100.0f, 100.0f, 200.0f, 500.0f));
        m.obst.add(new Obstacle(300.0f, 100.0f, 500.0f, 110.0f));
        m.obst.add(new Obstacle(900.0f, 50.0f, 1100.0f, 150.0f));
        m.obst.add(new Obstacle(600.0f, 100.0f, 700.0f, 110.0f));


        m.obst.add(new Obstacle(100.0f, 1100.0f, 200.0f, 1500.0f));
        m.obst.add(new Obstacle(300.0f, 1100.0f, 500.0f, 1110.0f));
        m.obst.add(new Obstacle(900.0f, 1150.0f, 1100.0f, 1350.0f));
        m.obst.add(new Obstacle(600.0f, 1100.0f, 700.0f, 1110.0f));


        m.ants.add(new Antenna(200.0f, 50.0f, 50.0f, Antenna.AntennaType.TRANSMISSION ));
        m.ants.add(new Antenna(200.0f, 50.0f, 350.0f, Antenna.AntennaType.TRANSMISSION ));
        m.ants.add(new Antenna(200.0f, 650.0f, 650.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));

//        int numBlue = 5;
//        int numRed = 2;
//
//        Antenna ant;
//        for(int i=0; i<numBlue; i++) {
//            ant = new Antenna(generateRandomPercentage(), generateRandomPercentage(),
//                    generateRandomPercentage(), Antenna.AntennaType.TRANSMISSION);
//
//        }
//        for(int i=0; i<numRed; i++) {
//            ant = new Antenna(generateRandomPercentage(), generateRandomPercentage(),
//                    generateRandomPercentage(), Antenna.AntennaType.ELECTONIC_WARFARE);
//        }

        return m;
    }

    // generate float num between 10 to 90.
    private float generateRandomPercentage() {
        return (float)((new Random()).nextInt(81)+10);
    }
}
