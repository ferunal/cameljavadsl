/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.SimpleExpression;

/**
 *
 * @author netrunner
 */
public class RutaPrueba extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        Expression expression = new SimpleExpression("select * from sys_informe l2");
        from("timer:hola?repeatCount=1").setBody(expression).
                bean("rb", "setSql(${body})").
                log("${body}").bean("rb", "getSql").
                to("jdbc:dsPrueba").split().
                body().
                log("${body['inf_nombre']}").
                to("activemq:cola:informes");

        from("activemq:cola:informes").log("Datos de la cola: ${body['inf_nombre']}").to("mock:salidafin");

//        from("cxfrs://bean://jaxrssfb").log("${body}").to("mock:rs");
    }

}
