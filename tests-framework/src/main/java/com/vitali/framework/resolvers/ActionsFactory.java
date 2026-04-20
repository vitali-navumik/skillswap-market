package com.vitali.framework.resolvers;

import com.vitali.framework.api.offer.OfferActions;
import com.vitali.framework.api.users.UsersActions;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.connectors.Sender;

public class ActionsFactory {
    public static ActionsContainer createGlobalActions(Sender sender, GetUserResponse userInfo) {

        UsersActions usersActions = new UsersActions(sender);
        OfferActions offerActions = new OfferActions(sender);

        return new ActionsContainer(userInfo, usersActions, offerActions);
    }
}
