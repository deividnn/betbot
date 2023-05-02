/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.entidades.ApostaDet;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.ApostaDetRepository;
import com.dnn.sistema.util.App;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class DashboardBean implements Serializable {

    private BigDecimal t1, t2, t3, t4,t5;
    private Date di, df;
    private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

    @PostConstruct
    public void init() {
        try {

            t1 = BigDecimal.ZERO;
            t2 = BigDecimal.ZERO;
            t3 = BigDecimal.ZERO;
            t4 = BigDecimal.ZERO;
            //   App.verificaPermissaoSM("banca", (String) App.pegarObjetoDaSessao("nivel"));
            Calendar ci = Calendar.getInstance();
            ci.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
            di = ci.getTime();

            Calendar cf = Calendar.getInstance();
            cf.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
            df = cf.getTime();

            Usuario usu = (Usuario) App.pegarObjetoDaSessao("usuario");

            t1 = (BigDecimal) new ApostaDetRepository(ApostaDet.class).objetoPorSql("select sum(percentual) "
                    + " from apostadet where status='GANHOU' "
                    + " and usuario=" + usu.getId() + ""
                    + " and (cast(data_alteracao as date) between '" + sd.format(di) + "'"
                    + " and '" + sd.format(df) + "') ");
            if (t1 == null) {
                t1 = BigDecimal.ZERO;
            }

            t2 = (BigDecimal) new ApostaDetRepository(ApostaDet.class).objetoPorSql("select sum(percentual) "
                    + " from apostadet where status='GANHOU' "
                    + " and usuario=" + usu.getId() + ""
                    + " and (cast(data_alteracao as date) between '" + sd.format(Calendar.getInstance().getTime()) + "'"
                    + " and '" + sd.format(Calendar.getInstance().getTime()) + "') ");
            if (t2 == null) {
                t2 = BigDecimal.ZERO;
            }
            
             t3= (BigDecimal) new ApostaDetRepository(ApostaDet.class).objetoPorSql("select sum(valor) "
                    + " from apostadet where status='GANHOU' "
                    + " and usuario=" + usu.getId() + ""
                    + " and (cast(data_alteracao as date) between '" + sd.format(di) + "'"
                    + " and '" + sd.format(df) + "') ");
            if (t3 == null) {
                t3 = BigDecimal.ZERO;
            }

            t4 = (BigDecimal) new ApostaDetRepository(ApostaDet.class).objetoPorSql("select sum(valor) "
                    + " from apostadet where status='GANHOU' "
                    + " and usuario=" + usu.getId() + ""
                    + " and (cast(data_alteracao as date) between '" + sd.format(Calendar.getInstance().getTime()) + "'"
                    + " and '" + sd.format(Calendar.getInstance().getTime()) + "') ");
            if (t4 == null) {
                t4 = BigDecimal.ZERO;
            }

        } catch (Exception e) {
            App.log(e);
        }
    }

    public BigDecimal getT5() {
        return t5;
    }

    public void setT5(BigDecimal t5) {
        this.t5 = t5;
    }

    
    
    public BigDecimal getT1() {
        return t1;
    }

    public void setT1(BigDecimal t1) {
        this.t1 = t1;
    }

    public BigDecimal getT2() {
        return t2;
    }

    public void setT2(BigDecimal t2) {
        this.t2 = t2;
    }

    public BigDecimal getT3() {
        return t3;
    }

    public void setT3(BigDecimal t3) {
        this.t3 = t3;
    }

    public BigDecimal getT4() {
        return t4;
    }

    public void setT4(BigDecimal t4) {
        this.t4 = t4;
    }

}
