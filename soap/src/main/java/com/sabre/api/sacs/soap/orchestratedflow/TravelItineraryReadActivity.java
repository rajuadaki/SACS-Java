package com.sabre.api.sacs.soap.orchestratedflow;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.sabre.api.sacs.configuration.SacsConfiguration;
import com.sabre.api.sacs.contract.travelitinerary.TravelItineraryReadRQ;
import com.sabre.api.sacs.contract.travelitinerary.TravelItineraryReadRS;
import com.sabre.api.sacs.soap.common.GenericRequestWrapper;
import com.sabre.api.sacs.workflow.Activity;
import com.sabre.api.sacs.workflow.SharedContext;

/**
 * Activity class to be used in the workflow. It runs the TravelItineraryRead request.
 */
@Controller
@Scope("prototype")
public class TravelItineraryReadActivity implements Activity {

    private static final Logger LOG = LogManager.getLogger(TravelItineraryReadActivity.class);
    
    @Autowired
    private GenericRequestWrapper<TravelItineraryReadRQ, TravelItineraryReadRS> tir;
    
    @Autowired
    private SacsConfiguration configuration;
    
    @Override
    public Activity run(SharedContext context) {
        Marshaller marsh;
        try {
            marsh = JAXBContext.newInstance("com.sabre.api.sacs.contract.travelitinerary").createMarshaller();
            StringWriter sw = new StringWriter();
            tir.setRequest(getRequestBody(context.getResult("PNR").toString()));
            tir.setLastInFlow(true);
            TravelItineraryReadRS result = tir.executeRequest(context); 
            marsh.marshal(result, sw);
            context.putResult("TravelItineraryReadRQ", sw.toString());
        } catch (JAXBException e) {
            LOG.error("Error while marshalling the response.", e);
        }

        return null;
    }
    
    private TravelItineraryReadRQ getRequestBody(String pnr) {

        TravelItineraryReadRQ body = new TravelItineraryReadRQ();

        body.setVersion(configuration.getSoapProperty("TravelItineraryReadRQVersion"));

        TravelItineraryReadRQ.MessagingDetails details = new TravelItineraryReadRQ.MessagingDetails();
        TravelItineraryReadRQ.MessagingDetails.SubjectAreas subjectAreas = new TravelItineraryReadRQ.MessagingDetails.SubjectAreas();
        subjectAreas.getSubjectArea().add("PNR");
        details.setSubjectAreas(subjectAreas);
        body.setMessagingDetails(details);

        TravelItineraryReadRQ.UniqueID uid = new TravelItineraryReadRQ.UniqueID();
        uid.setID(pnr);
        body.setUniqueID(uid);

        return body;
    }


}
