package com.freedom.messagebus.client.handler.request;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RealRequester extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealRequester.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        Message reqMsg = context.getMessages()[0];
        IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(reqMsg.getMessageType());
        byte[] msgBody = msgBodyProcessor.box(reqMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderProcessor.box(reqMsg.getMessageHeader());
        try {
            ProxyProducer.produceWithTX(CONSTS.PROXY_EXCHANGE_NAME,
                                        context.getChannel(),
                                        context.getQueueNode().getRoutingKey(),
                                        msgBody,
                                        properties);
            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        }
    }
}