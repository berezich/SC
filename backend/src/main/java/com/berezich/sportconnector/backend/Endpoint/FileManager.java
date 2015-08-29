package com.berezich.sportconnector.backend.Endpoint;

import com.berezich.sportconnector.backend.AccountForConfirmation;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Created by Sashka on 29.08.2015.
 */
@Api(
        name = "sportConnectorApi",
        version = "v1",
        resource = "",
        namespace = @ApiNamespace(
                ownerDomain = "backend.sportconnector.berezich.com",
                ownerName = "backend.sportconnector.berezich.com",
                packagePath = ""
        )
)


public class FileManager {
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());

    @ApiMethod(
            name = "uploadFileHandle",
            path = "FileManager",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void uploadFileHandle(@Named("fileKey") String fileKey) throws BadRequestException {
        OAuth_2_0.check();

    }
}
