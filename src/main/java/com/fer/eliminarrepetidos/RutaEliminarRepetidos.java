/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fer.eliminarrepetidos;

import java.io.File;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.SimpleExpression;

/**
 *
 * @author netrunner
 */
public class RutaEliminarRepetidos extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        String archivoLog = "c:" + File.separator + "progs" + File.separator + "logeliminarrepetidos.txt";
//        String strSql = " select distinct np.pbj_archivofuente from nt_probacionobjeto np \n"
//                + "where np.pbj_archivofuente  is not null and crg_id = 10";
        String strSql = "select  nc.crg_nombre pbj_archivofuente from nt_cargabase nc where nc.crg_estadocarga = 'cargado'\n"
                + "and crg_eliminarrepetidos = false and crg_id > 1693";

        String strBSqlHPVD = " select aas.hash_personavacunadosis from nt_agendaactividades_simple aas inner join \n"
                + "nt_eventos evt on aas.hash_personavacunadosis = evt.hash_personavacunadosis\n"
                + "and evt.evt_archivofuente = '${body[pbj_archivofuente]}'\n"
                + "group by aas.hash_personavacunadosis\n"
                + "having count(aas.hash_personavacunadosis)>1 ";

        String strCantidadPorHPVD = "select count(*) cantidad from nt_agendaactividades_simple nas where hash_personavacunadosis = '${body['hash_personavacunadosis']}'";
        String strCantidadPorHPVDFechaAtt = "select count(*) cantidad from nt_agendaactividades_simple nas "
                + " where hash_personavacunadosis = '${header[hpvd]}' and nas.aacts_fechaatencion is not null";

        String strObtenerIdMinConFechaAtt = "select min(nas.aacts_id) as minid from nt_agendaactividades_simple nas "
                + " where nas. hash_personavacunadosis = '${header[hpvd]}'\n"
                + "and nas.aacts_fechaatencion is not null";
        String strEliminarMinFechaAtt = "delete from nt_agendaactividades_simple nas \n"
                + "where nas.aacts_id <> ${header[minidfa]} \n"
                + "and nas.hash_personavacunadosis = '${header[hpvd]}'";

        String strObtenerIdMinSinFechaAtt = "select min(nas.aacts_id ) as minidsf from nt_agendaactividades_simple "
                + "nas where hash_personavacunadosis = '${header[hpvd]}'";
        String strEliminarMinSinFechaAnt = "delete from nt_agendaactividades_simple nas "
                + "where nas.aacts_id > ${header[minidsf]} and nas.hash_personavacunadosis = '${header[hpvd]}'";

        Expression expressionArchivos = new SimpleExpression(strSql);
        Expression expressionHashPVD = new SimpleExpression(strBSqlHPVD);
        Expression expressionCantidadPorHPVD = new SimpleExpression(strCantidadPorHPVD);
        Expression expressionConsultaSiFechaAtt = new SimpleExpression(strCantidadPorHPVDFechaAtt);
        Expression expressionObtenerIdMinConFechaAtt = new SimpleExpression(strObtenerIdMinConFechaAtt);
        Expression expressionEliminarIdMinConFechaAtt = new SimpleExpression(strEliminarMinFechaAtt);
        Expression expressionObtenerIdMinSinFechaAtt = new SimpleExpression(strObtenerIdMinSinFechaAtt);
        Expression expressionEliminarIdMinSinFechaAtt = new SimpleExpression(strEliminarMinSinFechaAnt);

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

//        from("activemq:cola:archivoproc").
//                log("Archivo elimar repetidos: ${body}").
//                setBody(expressionHashPVD).
//                  to("activemq:cola:consultarep");       

        from("activemq:cola:consultarep").
                // log("Datos de la cola: ${body}").
                to("jdbc:dsPolinotificador").split().
                body().setHeader("hpvd", new SimpleExpression("${body[hash_personavacunadosis]}"))
                .setBody(expressionCantidadPorHPVD).
                to("jdbc:dsPolinotificador").split().
                body().setHeader("cantidadEnconrada", new SimpleExpression("${body[cantidad]}")).
                choice().
                when(header("cantidadEnconrada").isNotEqualTo(1L)).
                to("direct:fechaatt").
                when(header("cantidadEnconrada").isEqualTo(1L)).
                to("direct:fin").endChoice();

        from("direct:fechaatt").wireTap("direct:log").
                //log("Se elimina ${header[hpvd]} con fecha atencion cantidad ${header[cantidadEnconrada]}").
                setBody(expressionConsultaSiFechaAtt).
                to("jdbc:dsPolinotificador").split().
                body().setHeader("cantidadFechaAtt", new SimpleExpression("${body[cantidad]}")).
                choice().
                when(header("cantidadFechaAtt").isNotEqualTo(0L)).
                to("direct:eliminarconfechatt").
                when(header("cantidadFechaAtt").isEqualTo(0L)).
                to("direct:eliminarsinfechatt").
                endChoice();
        from("direct:log").setBody(new SimpleExpression("${header[hpvd]}-${header[cantidadEnconrada]}"))
                .to("activemq:cola:log");
        from("activemq:cola:log").to("file://" + archivoLog);
        from("direct:eliminarconfechatt").setBody(expressionObtenerIdMinConFechaAtt).
                to("jdbc:dsPolinotificador").
                split().body().
                setHeader("minidfa", new SimpleExpression("${body[minid]}")).
                setBody(expressionEliminarIdMinConFechaAtt).to("activemq:cola:consultaelmfechaatt");

        from("activemq:cola:consultaelmfechaatt").to("jdbc:dsPolinotificador");

        from("direct:eliminarsinfechatt").setBody(expressionObtenerIdMinSinFechaAtt).
                to("jdbc:dsPolinotificador").
                split().body().
                //   log("${body}")
                setHeader("minidsf", new SimpleExpression("${body[minidsf]}")).
                setBody(expressionEliminarIdMinSinFechaAtt).to("activemq:cola:consultaelmsinfechaatt");

        from("activemq:cola:consultaelmsinfechaatt").to("jdbc:dsPolinotificador");

        from("direct:fin").
                // log("No eliminado: ${header[hpvd]}").
                to("mock:fin");
    }

}
