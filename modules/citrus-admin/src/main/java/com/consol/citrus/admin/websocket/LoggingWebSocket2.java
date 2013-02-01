package com.consol.citrus.admin.websocket;

import com.consol.citrus.admin.launcher.ProcessListener;
import org.eclipse.jetty.websocket.WebSocket;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Used for publishing log messages to connected clients via the web socket api.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.01.28
 */
public class LoggingWebSocket2 implements WebSocket.OnTextMessage, ProcessListener {

    private enum LogEvent {
        PING,
        START,
        MESSAGE,
        SUCCESS,
        FAILED;
    }

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoggingWebSocket2.class);

    /**
     * Web Socket connections
     * TODO MM thread safe
     */
    private List<Connection> connections = new ArrayList<Connection>();

    public LoggingWebSocket2() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOG.debug("Pinging client to keep connection alive");
                ping();
            }
        };
        timer.schedule(task, 60000, 60000);
    }

    /**
     * {@inheritDoc}
     */
    public void onOpen(Connection connection) {
        LOG.info("Accepted a new connection");
        this.connections.add(connection);
    }

    /**
     * {@inheritDoc}
     */
    public void onClose(int closeCode, String message) {
        LOG.debug("Web socket connection closed");
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection == null || !connection.isOpen()) {
                itor.remove();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public void onMessage(String data) {
        LOG.info("Received web socket client message: " + data);
    }

    /**
     * Invoked on start process event
     *
     * @param processId the id of the process
     */
    public void start(String processId) {
        logMessage(createMessage(processId, LogEvent.START, "process started"));
    }

    /**
     * Invoked on successful completion event
     *
     * @param processId the id of the completed process
     */
    public void success(String processId) {
        logMessage(createMessage(processId, LogEvent.SUCCESS, "process completed successfully"));
    }

    /**
     * Invoked on failed completion event, with the process exit code
     *
     * @param processId the id of the process
     * @param exitCode the exitcode returned from the process
     */
    public void fail(String processId, int exitCode) {
        logMessage(createMessage(processId, LogEvent.FAILED, "process failed with exit code " + exitCode));
    }

    /**
     * Invoked on failed completion event, with the exception that was caught
     *
     * @param processId the id of the process
     * @param e the exception caught within the ProcessLauncher
     */
    public void fail(String processId, Exception e) {
        logMessage(createMessage(processId, LogEvent.FAILED, "process failed with exception " + e.getLocalizedMessage()));
    }

    /**
     * Invoked on output message event with output data from process
     *
     * @param processId the id of the process
     * @param output
     */
    public void output(String processId, String output) {
        logMessage(createMessage(processId, LogEvent.MESSAGE, output));
    }

    public void ping() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", LogEvent.PING.name());
        logMessage(jsonObject.toString());
    }

    private String createMessage(String processId, LogEvent logEvent, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("processId", processId);
        jsonObject.put("event", logEvent.name());
        jsonObject.put("msg", message);
        return jsonObject.toString();
    }

    private void logMessage(String message) {
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection != null && connection.isOpen()) {
                try {
                    connection.sendMessage(message);
                } catch (IOException e) {
                    LOG.error("Error sending message", e);
                }
            } else {
                itor.remove();
            }
        }
    }
}
