/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.util.App;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class EventosBean implements Serializable {

    private Evento evento;
    private Evento eventoc;
    private List<Evento> eventos;
    private String permissao;
    private boolean novo;
    private EventoRepository eventoRepository;
    private String campo;
    private Date datai;
    private String valor;
    private Date dataf;

//
    @PostConstruct
    public void init() {
        App.verificaPermissao("eventos", (String) App.pegarObjetoDaSessao("nivel"));
        campo = "id";
        valor = "";
        novo = false;

        datai = Calendar.getInstance().getTime();
        dataf = Calendar.getInstance().getTime();
        this.evento = new Evento();
        this.eventoRepository = new EventoRepository(Evento.class);
        listar();

    }

    public Usuario pu(Long id) {
        return App.usu(id);
    }

    public void listar() {
        this.eventos = this.eventoRepository.listarHQL50("SELECT vo FROM Evento vo order by dataAlteracao desc");

    }

    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

    public void pesquisar() {
        this.eventos = this.eventoRepository.listarHQL("SELECT vo FROM Evento vo"
                + " where upper(cast(vo." + campo + " as text)) like '%"
                + valor.replace("'", "").toUpperCase() + "%'"
                + " and (cast(dataAlteracao as date) between "
                + " '" + sd.format(datai) + "' "
                + " and '" + sd.format(dataf) + "')"
                + " order by vo." + campo + " asc ");

        if (this.eventos.isEmpty()) {
            App.criarMensagemWarning("nenhum resultado encontrado");
        } else {
            App.criarMensagem(this.eventos.size() + " registro(s) encontrado(s)");
        }
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }

    public String getPermissao() {
        return permissao;
    }

    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }

    public Evento getEventoc() {
        return eventoc;
    }

    public void setEventoc(Evento eventoc) {
        this.eventoc = eventoc;
    }

    public boolean isNovo() {
        return novo;
    }

    public void setNovo(boolean novo) {
        this.novo = novo;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Date getDatai() {
        return datai;
    }

    public void setDatai(Date datai) {
        this.datai = datai;
    }

    public Date getDataf() {
        return dataf;
    }

    public void setDataf(Date dataf) {
        this.dataf = dataf;
    }

}
