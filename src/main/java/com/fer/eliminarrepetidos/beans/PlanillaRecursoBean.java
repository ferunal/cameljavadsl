/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos.beans;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 * @author lchacon
 */
@Path("/planilla")
public class PlanillaRecursoBean {

    public PlanillaRecursoBean() {
    }

    @GET
    @Path("/planillas/{nmnumeroplanilla}/")
    public String getCustomer(@PathParam("nmnumeroplanilla") String name) {
        return null;
    }
}
