package dev.sheldan.abstracto.core.provider;

import dev.sheldan.abstracto.core.models.provider.InformationRequest;
import dev.sheldan.abstracto.core.models.provider.ProviderInformation;

public interface InformationProvider {
    <R extends InformationRequest> boolean handlesRequest(R informationRequest);
    <I extends ProviderInformation, R extends InformationRequest> I retrieveInformation(R informationRequest);
}
