/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos.test;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author netrunner
 */
public class PruebaEliminarRepetidos {

    private static final long DURATION_MILIS = 10000;
    private static final String SOURCE_FOLDER = "/home/netrunner/Desktop";
    private static final String DESTINATION_FOLDER
            = "/home/netrunner/Desktop/iconos";

    public PruebaEliminarRepetidos() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() throws Exception {
//        CamelContext camelContext = new DefaultCamelContext();
//        camelContext.addRoutes(new RouteBuilder() {
//            @Override
//            public void configure() throws Exception {
//                from("file://" + SOURCE_FOLDER + "?delete=false&noop=true").log("Hola").to("file://" + DESTINATION_FOLDER);
//            }
//        });
//        camelContext.start();
//        Thread.sleep(DURATION_MILIS);
//        camelContext.stop();
    }
}
