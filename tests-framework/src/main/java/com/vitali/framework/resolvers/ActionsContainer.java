package com.vitali.framework.resolvers;

import com.vitali.framework.api.users.UsersActions;
import com.vitali.framework.api.users.responses.GetUserResponse;
import lombok.Getter;

public record ActionsContainer(@Getter GetUserResponse userInfo,
                               UsersActions usersActions) {
}
