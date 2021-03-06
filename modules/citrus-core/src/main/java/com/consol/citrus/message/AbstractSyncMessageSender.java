package com.consol.citrus.message;

import com.consol.citrus.TestActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.Message;

/**
 * Abstract base class for message sender with functionality common for all
 * synchronous message senders. Synchronous communication requires a {@link ReplyMessageHandler} to be
 * informed about receipt of synchronous response messages for further processing.
 * 
 * In parallel testing reply messages need message correlation via {@link ReplyMessageCorrelator} implementation.
 *
 * @author Christoph Deppisch, roland
 * @since 06.09.12
 */
abstract public class AbstractSyncMessageSender implements MessageSender, BeanNameAware {

    /** Logger */
    protected  Logger log = LoggerFactory.getLogger(getClass());

    /** The reply message handler */
    protected ReplyMessageHandler replyMessageHandler;

    /** The reply message correlator */
    private ReplyMessageCorrelator correlator = null;

    /** Test actor linked to this message sender */
    private TestActor actor;

    /** This sender's name */
    private String name = getClass().getSimpleName();

    /**
     * Informs reply message handler for further processing
     * of reply message.
     * @param responseMessage the reply message.
     * @param requestMessage the initial request message.
     */
    protected void informReplyMessageHandler(Message<?> responseMessage, Message<?> requestMessage) {
        if (replyMessageHandler != null) {
            log.info("Informing reply message handler for further processing");

            if (correlator != null) {
                replyMessageHandler.onReplyMessage(responseMessage, correlator.getCorrelationKey(requestMessage));
            } else {
                replyMessageHandler.onReplyMessage(responseMessage);
            }
        }
    }

    /**
     * Set the reply message handler.
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
    
    /**
     * Gets the replyMessageHandler.
     * @return the replyMessageHandler
     */
    public ReplyMessageHandler getReplyMessageHandler() {
        return replyMessageHandler;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        this.actor = actor;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
