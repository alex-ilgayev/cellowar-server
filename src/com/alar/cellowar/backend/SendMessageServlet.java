package com.alar.cellowar.backend;

import com.alar.cellowar.backend.controller.ErrorStrings;
import com.alar.cellowar.backend.controller.MessageHandler;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.logging.Logger;

public class SendMessageServlet extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger("SendMessageServlet");
    private final static Gson GSON = new Gson();

    /**
     * Main request handler.
     * the request must be as POST, and contain Packet object which contains two parameters:
     * Payload - serialized message encoded in base64
     * Date - timestamp (epoch) of the message
     *
     * from this both parameters, creates Packet object, and send it the handler function.
     * the request returns json list of packets (same parameters)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // FIRST VALIDATING REUQEST FULLY.
        Packet packet = new Gson().fromJson(request.getReader(), Packet.class);
        if(packet == null) {
            LOGGER.warning("Status 404: " + ErrorStrings.WRONG_PACKET_FORMAT_ERROR);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorStrings.WRONG_PACKET_FORMAT_ERROR);
            return;
        }

        // can't work without payload
        if(packet.payload == null || packet.payload.equals("")) {
            LOGGER.warning("Status 404: " + ErrorStrings.NO_PACKET_PAYLOAD_INCLUDED_ERROR);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorStrings.NO_PACKET_PAYLOAD_INCLUDED_ERROR);
            return;
        }

        // checks base64 decode failure.
        byte[] packetPayload = null;
        try {
            packetPayload = Base64.getDecoder().decode(packet.payload);
        } catch (Exception e) {
            LOGGER.warning("Status 404: " + ErrorStrings.PAYLOAD_NOT_VALID_BASE_64);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorStrings.PAYLOAD_NOT_VALID_BASE_64);
            return;
        }

        // checks deserialization failure
        IMessage msg = null;
        try {
            msg = MessageCompression.getInstance().decompress(packetPayload);
        } catch (Exception e) {
            LOGGER.warning("Status 404: " + ErrorStrings.PAYLOAD_NOT_VALID_MESSAGE);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorStrings.PAYLOAD_NOT_VALID_MESSAGE);
            return;
        }
        if(msg == null) {
            LOGGER.warning("Status 404: " + ErrorStrings.PAYLOAD_NOT_VALID_MESSAGE);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorStrings.PAYLOAD_NOT_VALID_MESSAGE);
            return;
        }

        // FINISHED HANDLING REQUEST.

        Packet[] resPackets = MessageHandler.getInstance().handleMessage(msg);

        if(resPackets == null) {
            LOGGER.warning("Status 500: " + ErrorStrings.SERVER_ERROR);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorStrings.SERVER_ERROR);
            return;
        }

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        out.print(GSON.toJson(resPackets));
        out.flush();
    }
}
