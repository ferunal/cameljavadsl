/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jdbc.JdbcComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author netrunner
 */
public class Principal {

    public static void main(String[] args) {
        try {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setUrl("jdbc:postgresql://localhost:5432/pruebacamel");
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setPassword("auditoria");
            dataSource.setUsername("auditoria");
            dataSource.setInitialSize(4);
            dataSource.setMaxIdle(4);
            dataSource.setMaxTotal(16);

            CamelContext context = new DefaultCamelContext();
            RepetidosBean repetidosBean = new RepetidosBean();
            context.getRegistry().bind("rb", repetidosBean);
            context.getRegistry().bind("dsPrueba", dataSource);
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
