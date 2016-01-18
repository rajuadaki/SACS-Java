package com.sabre.api.sacs.soap.interceptor;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;

import com.sabre.api.sacs.contract.soap.MessageHeader;
import com.sabre.api.sacs.contract.soap.Security;
import com.sabre.api.sacs.soap.pool.SessionPool;

/**
 * Responsible for returning a session to a pool.
 * Should be added to a last call of the flow.
 */

@Controller
public class SessionPoolInterceptor extends AbstractSessionInterceptor {

    private static final Logger LOG = LogManager.getLogger(SessionPoolInterceptor.class);

    @Autowired
    private SessionPool sessionPool;
    
	@Override
	public boolean handleRequest(MessageContext messageContext) {
		return true;
	}

	@SuppressWarnings("serial")
    @Override
	public boolean handleResponse(MessageContext messageContext) {
	       String token = null;
	        String conversationId = null;
	        Security security = null;
	        try {
	            security = extractSecurityFromMessageContext(messageContext);
	            MessageHeader header = extractMessageHeaderFromMessageContext(messageContext);

	            token = security.getBinarySecurityToken();
	            conversationId = header.getConversationId();

	        } catch (JAXBException | NullPointerException e) {
	            LOG.fatal( "Error occurred during retrieving session token", e );
	        }

	        if( token == null | conversationId == null ) {
	            throw new WebServiceClientException( "Couldn't retrieve session token from message" ) {
	            };
	        }

	        logTokenAndConversationIdFromMessage(token, conversationId);

			sessionPool.returnToPool(security, conversationId);
	        return true;
	}

}
