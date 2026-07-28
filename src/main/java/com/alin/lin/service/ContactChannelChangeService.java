package com.alin.lin.service;

import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.ContactChannelChangeRequest;

public interface ContactChannelChangeService {
    AddressChangeDto save(String changeCaseNo, String channel, ContactChannelChangeRequest request);
}
