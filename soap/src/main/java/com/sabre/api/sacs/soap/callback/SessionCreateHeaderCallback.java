package com.sabre.api.sacs.soap.callback;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import com.sabre.api.sacs.configuration.SacsConfiguration;
import com.sabre.api.sacs.contract.soap.MessageHeader;
import com.sabre.api.sacs.contract.soap.Security;
import com.sabre.api.sacs.workflow.SharedContext;
/**
 * SpringWS callback used for adding credentials to the call, that creates a session.
 */
@Controller
public class SessionCreateHeaderCallback implements HeaderCallback {

    private static final String ACTION_STRING = "SessionCreateRQ";

    private MessageHeader header;
    private SharedContext workflowContext;
    private Security security;

    @Autowired
    private MessageHeaderFactory messageHeaderFactory;

    @Autowired
    private SacsConfiguration configuration;

    @Autowired
    private Jaxb2Marshaller marshaller;

    @Override
    public void doWithMessage(WebServiceMessage webServiceMessage) throws IOException, TransformerException {

        header = messageHeaderFactory.getMessageHeader(ACTION_STRING);
        header.setConversationId(workflowContext.getConversationId());

        security = getCredentialsSecurity();

        SoapHeader soapHeaderElement = ((SoapMessage) webServiceMessage).getSoapHeader();

        marshaller.marshal(header, soapHeaderElement.getResult());
        marshaller.marshal(security, soapHeaderElement.getResult());

    }

    public void setWorkflowContext(SharedContext workflowContext) {
        this.workflowContext = workflowContext;
    }

    private Security getCredentialsSecurity() {

        Security security = new Security();
        Security.UsernameToken usernameToken = new Security.UsernameToken();

        
        usernameToken.setDomain(configuration.getSoapProperty("domain"));
        usernameToken.setOrganization(configuration.getSoapProperty("organization"));
        usernameToken.setPassword(configuration.getSoapProperty("password"));
        usernameToken.setUsername(configuration.getSoapProperty("username"));
        security.setUsernameToken(usernameToken);

        return security;
    }

}
