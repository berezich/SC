package com.berezich.sportconnector.backend.Endpoint;

import com.google.api.server.spi.ServiceException;

/**
 * Created by berezkin on 27.06.2015.
 */
public class InstanceAlreadyExists extends ServiceException {
    public InstanceAlreadyExists(String message) {
        super(408, message);
    }
}