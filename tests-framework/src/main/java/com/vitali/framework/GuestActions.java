package com.vitali.framework;

import com.vitali.framework.api.login.LoginActions;
import com.vitali.framework.api.offer.OfferActions;
import com.vitali.framework.api.register.RegisterActions;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;

public record GuestActions(RegisterActions registerActions,
                           LoginActions loginActions,
                           OfferActions offerActions) {

    public static GuestActions create() {
        Sender sender = new Sender(null, new RestAssuredConnector());
        return new GuestActions(
                new RegisterActions(sender),
                new LoginActions(sender),
                new OfferActions(sender)
        );
    }
}
