/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.repositories;

import com.dnn.sistema.entidades.Banca;
import com.dnn.sistema.entidades.Usuario;
import java.io.Serializable;
import javax.faces.bean.ApplicationScoped;
import javax.transaction.Transactional;

/**
 *
 * @author deivid
 */
@ApplicationScoped
@Transactional(rollbackOn = Throwable.class)
public class BancaRepository extends BasicRepository<Banca, Long> implements Serializable {

    public BancaRepository(Class<Banca> entityKlasse) {
        super(entityKlasse);
    }

}
