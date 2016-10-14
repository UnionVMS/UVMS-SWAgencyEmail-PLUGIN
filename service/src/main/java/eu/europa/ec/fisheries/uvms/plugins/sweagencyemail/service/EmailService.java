/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.plugins.sweagencyemail.service;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.EmailType;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.havochvatten.common.ejb.ws.WsClientUtil;
import se.havochvatten.service.client.infraws.v1_0.AddressType;
import se.havochvatten.service.client.infraws.v1_0.BodyContentType;
import se.havochvatten.service.client.infraws.v1_0.EMail;
import se.havochvatten.service.client.infraws.v1_0.EMail.Body;
import se.havochvatten.service.client.infraws.v1_0.InfraCatchException;
import se.havochvatten.service.client.infraws.v1_0.InfraPortType;
import se.havochvatten.service.client.infraws.v1_0.InfraService;
import se.havochvatten.service.client.infraws.v1_0.SendEMail;

/**
 **/
@LocalBean
@Stateless
public class EmailService {

    final static Logger LOG = LoggerFactory.getLogger(EmailService.class);

    public String sendEmail(EmailType emailType, String endpoint) {
        try {

            InfraPortType infraPort = new InfraService().getInfraPortTypePort();
            WsClientUtil.setEndpointAddress(infraPort, endpoint);

            LOG.debug("Sending mail to: {} from: {} endpoint: {}", emailType.getTo(), emailType.getFrom(), endpoint);
            final SendEMail sendEMail = new SendEMail();
            EMail email = new EMail();
            final Body body = new Body();
            body.setValue(emailType.getBody());
            body.setContentType(BodyContentType.TEXT_HTML);
            email.setBody(body);
            final AddressType addressTypeFrom = new AddressType();
            addressTypeFrom.setAddress(emailType.getFrom());
            email.setFrom(addressTypeFrom);
            email.setSubject(emailType.getSubject());
            final AddressType address = new AddressType();
            address.setAddress(emailType.getTo());
            email.getTo().add(address);
            
            for (String ccAddresss : emailType.getCc()) {
                final AddressType cc = new AddressType();
                cc.setAddress(ccAddresss);
                email.getCc().add(cc);
            }
            
            sendEMail.setEMail(email);
            infraPort.sendEMail(sendEMail);
            LOG.debug("infraProxy: " + emailType.getTo());
            return emailType.getTo();
        } catch (InfraCatchException ex) {
            LOG.error("Unable to communicate with Infrastructure Service, HavErrorMessage: " + ex.getFaultInfo().getHaVText() + " HavErrorCode: " + ex.getFaultInfo().getHaVCode());
            return null;
        } catch (Exception e) {
            LOG.error("Unable to communicate with Infrastructure Service. Tried to send message to {} over {}. Exception message: {}", emailType.getTo(), endpoint, e.getMessage());
        }
    }
}