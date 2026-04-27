package com.vitali.framework.api.offer.helpers;

import com.vitali.framework.api.offer.requests.UpdateOfferRequest;
import com.vitali.framework.api.offer.responses.GetOfferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OfferMapper {

    OfferMapper INSTANCE = Mappers.getMapper(OfferMapper.class);

    UpdateOfferRequest toUpdateOfferRequest(GetOfferResponse response);
}
