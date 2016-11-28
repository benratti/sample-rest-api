package fr.ratti.sample.api.rest.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Created by bratti on 20/08/2016.
 */
public class SampleResourceImpl extends AbstractSimpleResource<SampleData> implements SampleResource{

    private final Logger logger = LoggerFactory.getLogger(SampleResourceImpl.class);

    public SampleResourceImpl() {

    }

    @Override
    protected Logger getLogger() {
        return logger;
    }


    @Override
    protected Response handleException(Exception exception) {

        if ( exception instanceof ExpectedException) {
            return Response.status(BAD_REQUEST).build();
        } else {
            return super.handleException(exception);
        }
    }
}
