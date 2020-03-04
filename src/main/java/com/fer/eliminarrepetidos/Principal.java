/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * @author netrunner
 */
public class Principal {

    public static void main(String[] args) {
        try {
            CamelContext context = new DefaultCamelContext();
            RepetidosBean repetidosBean = new RepetidosBean();
            context.getRegistry().bind("rb", repetidosBean);
            RutaPrueba rutaPrueba = new RutaPrueba();
            context.addRoutes(rutaPrueba);
            context.start();
            System.out.println("Running for 10 seconds and then stopping");
            Thread.sleep(10000);
            context.stop();
        } catch (Exception ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
