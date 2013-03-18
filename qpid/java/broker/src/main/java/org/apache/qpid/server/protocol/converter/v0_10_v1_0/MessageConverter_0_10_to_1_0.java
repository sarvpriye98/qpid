package org.apache.qpid.server.protocol.converter.v0_10_v1_0;

import java.util.ArrayList;
import java.util.List;
import org.apache.qpid.amqp_1_0.messaging.SectionEncoder;
import org.apache.qpid.amqp_1_0.type.Binary;
import org.apache.qpid.amqp_1_0.type.Section;
import org.apache.qpid.amqp_1_0.type.Symbol;
import org.apache.qpid.amqp_1_0.type.UnsignedByte;
import org.apache.qpid.amqp_1_0.type.UnsignedInteger;
import org.apache.qpid.amqp_1_0.type.messaging.ApplicationProperties;
import org.apache.qpid.amqp_1_0.type.messaging.Header;
import org.apache.qpid.amqp_1_0.type.messaging.Properties;
import org.apache.qpid.server.protocol.v0_10.MessageTransferMessage;
import org.apache.qpid.server.protocol.v1_0.MessageConverter_to_1_0;
import org.apache.qpid.server.protocol.v1_0.MessageMetaData_1_0;
import org.apache.qpid.transport.DeliveryProperties;
import org.apache.qpid.transport.MessageDeliveryMode;
import org.apache.qpid.transport.MessageProperties;

public class MessageConverter_0_10_to_1_0  extends MessageConverter_to_1_0<MessageTransferMessage>
{
    @Override
    public Class<MessageTransferMessage> getInputClass()
    {
        return MessageTransferMessage.class;
    }


    @Override
    protected MessageMetaData_1_0 convertMetaData(MessageTransferMessage serverMessage,
                                                  SectionEncoder sectionEncoder)
    {
        List<Section> sections = new ArrayList<Section>(3);
        final MessageProperties msgProps = serverMessage.getHeader().getMessageProperties();
        final DeliveryProperties deliveryProps = serverMessage.getHeader().getDeliveryProperties();

        Header header = new Header();
        if(deliveryProps != null)
        {
            header.setDurable(deliveryProps.hasDeliveryMode() && deliveryProps.getDeliveryMode() == MessageDeliveryMode.PERSISTENT);
            if(deliveryProps.hasPriority())
            {
                header.setPriority(UnsignedByte.valueOf((byte) deliveryProps.getPriority().getValue()));
            }
            if(deliveryProps.hasTtl())
            {
                header.setTtl(UnsignedInteger.valueOf(deliveryProps.getTtl()));
            }
            sections.add(header);
        }

        Properties props = new Properties();
        if(msgProps != null)
        {
        //        props.setAbsoluteExpiryTime();
            if(msgProps.hasContentEncoding())
            {
                props.setContentEncoding(Symbol.valueOf(msgProps.getContentEncoding()));
            }

            if(msgProps.hasCorrelationId())
            {
                props.setCorrelationId(msgProps.getCorrelationId());
            }
        //        props.setCreationTime();
        //        props.setGroupId();
        //        props.setGroupSequence();
            if(msgProps.hasMessageId())
            {
                props.setMessageId(msgProps.getMessageId());
            }
            if(msgProps.hasReplyTo())
            {
                props.setReplyTo(msgProps.getReplyTo().getExchange()+"/"+msgProps.getReplyTo().getRoutingKey());
            }
            if(msgProps.hasContentType())
            {
                props.setContentType(Symbol.valueOf(msgProps.getContentType()));

                // Modify the content type when we are dealing with java object messages produced by the Qpid 0.x client
                if(props.getContentType() == Symbol.valueOf("application/java-object-stream"))
                {
                    props.setContentType(Symbol.valueOf("application/x-java-serialized-object"));
                }
            }
        //        props.setReplyToGroupId();
            props.setSubject(serverMessage.getRoutingKey());
        //        props.setTo();
            if(msgProps.hasUserId())
            {
                props.setUserId(new Binary(msgProps.getUserId()));
            }

            sections.add(props);

            if(msgProps.getApplicationHeaders() != null)
            {
                sections.add(new ApplicationProperties(msgProps.getApplicationHeaders()));
            }
        }
        return new MessageMetaData_1_0(sections, sectionEncoder);
    }
}
