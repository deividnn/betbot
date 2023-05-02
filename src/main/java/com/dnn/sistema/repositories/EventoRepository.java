/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.repositories;

import com.dnn.sistema.entidades.Evento;
import java.io.Serializable;
import java.util.UUID;
import javax.faces.bean.ApplicationScoped;
import javax.transaction.Transactional;

/**
 *
 * @author deivid
 */
@ApplicationScoped
@Transactional(rollbackOn = Throwable.class)
public class EventoRepository extends BasicRepository<Evento, UUID> implements Serializable {

    public EventoRepository(Class<Evento> entityKlasse) {
        super(entityKlasse);
    }

}
