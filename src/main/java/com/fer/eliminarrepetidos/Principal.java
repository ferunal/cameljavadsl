/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import com.fer.eliminarrepetidos.beans.PlanillaRecursoBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jdbc.JdbcComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author netrunner
 */
public class Principal {

    public static void main(String[] args) {
        try {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setUrl("jdbc:postgresql://192.168.0.34:5432/portal_reportes");
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUsername("simple");
            dataSource.setPassword("Seguro23*");

            dataSource.setInitialSize(4);
            dataSource.setMaxIdle(4);
            dataSource.setMaxTotal(16);

            JAXRSServerFactoryBean jaxrssfb = new JAXRSServerFactoryBean();
            jaxrssfb.setServiceClass(PlanillaRecursoBean.class);
            jaxrssfb.setAddress("http://localhost:9080/");
             
            
            ActiveMQConnectionFactory amqcf = new ActiveMQConnectionFactory();
            amqcf.setBrokerURL("vm://localhost?broker.presistence=false");

            PooledConnectionFactory pcf = new PooledConnectionFactory();
            pcf.setMaxConnections(100);
            pcf.setConnectionFactory(amqcf);

            JmsTemplate jmsTemplate = new JmsTemplate(pcf);

            CamelContext context = new DefaultCamelContext();
            RepetidosBean repetidosBean = new RepetidosBean();
            //Registrar beans
            context.getRegistry().bind("rb", RepetidosBean.class, repetidosBean);
            context.getRegistry().bind("dsPrueba", dataSource);
            context.getRegistry().bind("amqcf", amqcf);
            context.getRegistry().bind("pcf", pcf);
            context.getRegistry().bind("jmsTemplate", jmsTemplate);
            context.getRegistry().bind("jaxrssfb", jaxrssfb);
            //Instanciar rutas
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
