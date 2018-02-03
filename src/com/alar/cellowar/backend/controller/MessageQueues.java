package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.alar.cellowar.shared.messaging.MessageResponseClientList;
import com.alar.cellowar.shared.messaging.MessageResponseSession;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Alex on 4/28/2015.
 *
 * Holds a message queue for each client.
 * The client will be polling the server for available messages any moment.
 */
public class MessageQueues {
    private final static Logger LOGGER = Logger.getLogger("MessageQueues");

    private static MessageQueues _ins = null;

    public static MessageQueues getInstance(){
        if(_ins == null)
            _ins = new MessageQueues();
        return _ins;
    }

    public void addPacket(Client client, Packet packet){
        if(TemporaryDB.getInstance().findClientById(client.getId()) == null) {
            LOGGER.severe(ErrorStrings.SERVER_ERROR_PACKET_FROM_UNRECOGNIZED_USER);
            return;
        }
        TemporaryDB.getInstance().getQueue(client).add(packet);
    }

    /**
     * Getting unread packets for the client.
     * The packets oldest to the newest.
     * also adding session message to the queue.
     *
     * @param client
     * @return New awaiting packets.
     */
    public Packet[] getAwaitingPackets(Client client, UUID id){
        TemporaryDB.getInstance().setTimestamp(client, System.currentTimeMillis());

        // adding session message to the user.
        UUID clientSessionId;
        Session serverSession;
        if(((clientSessionId = client.getCurrSessionId()) != null)
                && ((serverSession = TemporaryDB.getInstance().findSession(clientSessionId)) != null)) { // user is part of active session.
            MessageResponseSession returnMsg = new MessageResponseSession();
            returnMsg.responseClient = client;
            returnMsg.responseId = id;
            returnMsg.activeSession = serverSession;
            MessageQueues.getInstance().addPacket(client, MessageToPacket(returnMsg));
        } else { // give a non-playing player list of connected clients.
            MessageResponseClientList returnMsg = new MessageResponseClientList();
            returnMsg.responseClient = client;
            returnMsg.responseId = id;
            returnMsg.clients = TemporaryDB.getInstance().getAllClients();
            MessageQueues.getInstance().addPacket(client, MessageToPacket(returnMsg));
        }

        Packet[] packets = TemporaryDB.getInstance().getQueue(client).toArray(new Packet[0]);
        TemporaryDB.getInstance().clearQueue(client);
        return packets;
    }

    public static Packet MessageToPacket(IMessage msg) {
        Packet p = new Packet();
        p.date = System.currentTimeMillis();
        p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(msg));
        return p;
    }
}
