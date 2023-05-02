/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.repositories;

import com.dnn.sistema.entidades.Aposta;
import com.dnn.sistema.entidades.ApostaDet;
import java.io.Serializable;
import javax.faces.bean.ApplicationScoped;
import javax.transaction.Transactional;

/**
 *
 * @author deivid
 */
@ApplicationScoped
@Transactional(rollbackOn = Throwable.class)
public class ApostaDetRepository extends BasicRepository<ApostaDet, Long> implements Serializable {

    public ApostaDetRepository(Class<ApostaDet> entityKlasse) {
        super(entityKlasse);
    }

}
