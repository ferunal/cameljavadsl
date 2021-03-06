/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author netrunner
 */
public class Principal {

    public static void main(String[] args) {
        try {
            
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setUrl("jdbc:postgresql://192.168.1.4:5432/polinotificadorfinal");
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUsername("auditoria");
            dataSource.setPassword("auditoria");

            dataSource.setInitialSize(4);
            dataSource.setMaxIdle(4);
            dataSource.setMaxTotal(16);

//            JAXRSServerFactoryBean jaxrssfb = new JAXRSServerFactoryBean();
//            jaxrssfb.setServiceClass(PlanillaRecursoBean.class);
//            jaxrssfb.setAddress("http://localhost:9080/");
             
            
            ActiveMQConnectionFactory amqcf = new ActiveMQConnectionFactory();
            amqcf.setBrokerURL("vm://localhost?broker.presistence=false");

            PooledConnectionFactory pcf = new PooledConnectionFactory();
            pcf.setMaxConnections(100);
            pcf.setConnectionFactory(amqcf);

            JmsTemplate jmsTemplate = new JmsTemplate(pcf);

            CamelContext context = new DefaultCamelContext();
            context.setStreamCaching(Boolean.TRUE);
            
            RepetidosBean repetidosBean = new RepetidosBean();
            //Registrar beans
            context.getRegistry().bind("rb", RepetidosBean.class, repetidosBean);
            context.getRegistry().bind("dsPolinotificador", dataSource);
            context.getRegistry().bind("amqcf", amqcf);
            context.getRegistry().bind("pcf", pcf);
            context.getRegistry().bind("jmsTemplate", jmsTemplate);
//            context.getRegistry().bind("jaxrssfb", jaxrssfb);
            //Instanciar rutas
            RutaEliminarRepetidos rutaPrueba = new RutaEliminarRepetidos();
            context.addRoutes(rutaPrueba);
            context.start();
            System.out.println("Esperar días parar contexto");
            Thread.sleep(1000*60*60*12*8);
            context.stop();
        } catch (Exception ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
