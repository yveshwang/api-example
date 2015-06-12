package com.macyves.api.restricted;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.macyves.entities.Customer;
import com.macyves.facade.BaseDBFacadeInterface;
import com.macyves.facade.CustomerFacade;

/**
 * The root url for the unrestricted API
 * 
 * @author yves
 * 
 */
@Path("/customer/")
public class CustomerResource extends CRUDBaseResource<Customer> {

    private AtomicInteger count = new AtomicInteger(0);

    @Inject
    private CustomerFacade facade;

    @Override
    protected Class<Customer> getEntityClass() {
        return Customer.class;
    }

    @Override
    protected BaseDBFacadeInterface<Customer> getFacade() {
        return facade;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("gen")
    public Response gen(@Context HttpServletRequest req) {
        final Customer tmp = new Customer();
        tmp.setName("John Doe " + count.getAndIncrement());
        return saveModel(tmp, req, false);
    }
}
