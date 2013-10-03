package com.ft.api.jaxrs.errors;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * RuntimeExceptionMapper
 *
 * @author Simon.Gibbs
 */
public class RuntimeExceptionMapper  implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    public static final String GENERIC_MESSAGE = "server error";

    @Override
    public Response toResponse(RuntimeException exception) {

        // ensure exceptions are logged!
        LOG.error("unhandled exception", exception);


        if(exception instanceof WebApplicationException) {
            Response response = ((WebApplicationException) exception).getResponse();

            // skip processing of responses that are already present.
            if(response.getEntity() != null) {
                return response;
            }

            // fill out null responses
            String message = Objects.firstNonNull(exception.getMessage(),GENERIC_MESSAGE);

            if(!GENERIC_MESSAGE.equals(message)) {
                // Don't turn this off. You should be using ServerError and ClientError builders.
                LOG.warn("Surfaced exception message from unknown tier. Expected ErrorEntity from web tier.");
            }
            AbstractErrorBuilder<?,?> responseBuilder;
            if(response.getStatus()<500) {
                responseBuilder = ClientError.status(response.getStatus());
            } else {
                responseBuilder = ServerError.status(response.getStatus());
            }

            return responseBuilder.error(message).response();
        }

        // force a standard response for unexpected error types
        return ServerError.status(500).error(GENERIC_MESSAGE).response();
    }

}
