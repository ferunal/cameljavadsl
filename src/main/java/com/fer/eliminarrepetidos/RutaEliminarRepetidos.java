/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import org.apache.camel.Expression;
import org.apache.camel.builder.PredicateClause;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.SimpleExpression;

/**
 *
 * @author netrunner
 */
public class RutaEliminarRepetidos extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        String strSql = " select distinct np.pbj_archivofuente from nt_probacionobjeto np \n"
                + "where np.pbj_archivofuente  is not null ";

        StringBuilder strBSqlHPVD = new StringBuilder(" select hash_personavacunadosis from nt_agendaactividades_simple inner join \n");
        strBSqlHPVD.append("nt_probacionobjeto po on nt_agendaactividades_simple.hash_unificado = po.hash_unificado\n");
        strBSqlHPVD.append("where po.pbj_archivofuente = '${body['pbj_archivofuente']}'\n");
        strBSqlHPVD.append("group by hash_personavacunadosis\n");
        strBSqlHPVD.append("having count(hash_personavacunadosis)>1 ");
        
        String strCantidadPorHPVD = "select count(*) cantidad from nt_agendaactividades_simple nas where hash_personavacunadosis = '${body['hash_personavacunadosis']}'";
        String strCantidadPorHPVDFechaAtt = "select count(*) cantidad from nt_agendaactividades_simple nas "
                + " where hash_personavacunadosis = '${header[hpvd]}' and nas.aacts_fechaatencion is not null";
        
        String strObtenerIdMinConFechaAtt ="select min(nas.aacts_id) as minid from nt_agendaactividades_simple nas "
                + " where nas. hash_personavacunadosis = '${header[hpvd]}'\n" +
"and nas.aacts_fechaatencion is not null";
        String strEliminarMinFechaAtt = "delete from nt_agendaactividades_simple nas \n" +
"where nas.aacts_id <> 17933184 \n" +
"and nas.hash_personavacunadosis = '${header[hpvd]}'";
        
        Expression expressionArchivos = new SimpleExpression(strSql);
        Expression expressionHashPVD = new SimpleExpression(strBSqlHPVD.toString());
        Expression expressionCantidadPorHPVD = new SimpleExpression(strCantidadPorHPVD);
        Expression expressionConsultaSiFechaAtt = new SimpleExpression(strCantidadPorHPVDFechaAtt);
        Expression expressionObtenerIdMinConFechaAtt = new SimpleExpression(strObtenerIdMinConFechaAtt);

        from("timer:hola?repeatCount=1").setBody(expressionArchivos).
                //                bean("rb", "setSql(${body})").
                //                log("${body}").bean("rb", "getSql").
                to("jdbc:dsPolinotificador").split().
                body().
                // log("${body['pbj_archivofuente']}").
                setBody(expressionHashPVD).
                //  log("${body}").
                //to("mock:archivos");
                to("activemq:cola:consultarep");

        from("activemq:cola:consultarep").
                // log("Datos de la cola: ${body}").
                to("jdbc:dsPolinotificador").split().
                body().setHeader("hpvd", new SimpleExpression("${body[hash_personavacunadosis]}"))
                .setBody(expressionCantidadPorHPVD).
                to("jdbc:dsPolinotificador").split().
                body(). setHeader("cantidadEnconrada",new SimpleExpression("${body[cantidad]}")).
                choice().
                when(header("cantidadEnconrada").isNotEqualTo(1L)).
                  
                    to("direct:fechaatt").
                when(header("cantidadEnconrada").isEqualTo(1L)).
                  
                to("direct:fin").endChoice();

        from("direct:fechaatt").
                log("Se elimina ${header[hpvd]} cantidad ${header[cantidadEnconrada]}").
                setBody(expressionConsultaSiFechaAtt).
               to("jdbc:dsPolinotificador").split().
                body(). setHeader("cantidadFechaAtt",new SimpleExpression("${body[cantidad]}")).
                choice().
                    when(header("cantidadFechaAtt").isNotEqualTo(0L)).
                    to("direct:eliminarconfechatt").
                    when(header("cantidadFechaAtt").isEqualTo(0L)).
                    to("direct:eliminarsinfechatt").
                endChoice();
        
        from("direct:eliminarconfechatt").setBody(expressionObtenerIdMinConFechaAtt).
                to("jdbc:dsPolinotificador").split().body().setHeader("minidfa", new SimpleExpression("${body[minid]}"))
                .setBody();
        
        from("direct:fin").log("No se elimina ${header[hpvd]} cantidad ${header[cantidadEnconrada]}").to("mock:rsn");
    }

}
