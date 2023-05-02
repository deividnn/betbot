/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.repositories;

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
public class UsuarioRepository extends BasicRepository<Usuario, Long> implements Serializable {

    public UsuarioRepository(Class<Usuario> entityKlasse) {
        super(entityKlasse);
    }

}
