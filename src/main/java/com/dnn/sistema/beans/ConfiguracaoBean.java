/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;


import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.repositories.UsuarioRepository;
import com.dnn.sistema.util.App;
import com.dnn.sistema.util.CryptoUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpSession;

/**
 *
 * @author deivid
 */
@ManagedBean(name = "configuracaoBean")
@ViewScoped
public class ConfiguracaoBean implements Serializable {

    private String senhaatual;
    private String senhanova;
    private String csenhanova;

    private EventoRepository eventoRepository;
    private boolean edicao;
    private boolean edicao100;

    @PostConstruct
    public void init() {
        try {
            edicao = false;
            edicao100 = false;
            this.eventoRepository = new EventoRepository(Evento.class);
            listar();
            listar100();

        } catch (Exception e) {
            App.log(e);
        }
    }

    public void listar() {
      

    }

    public void listar100() {

       
    }

  

    public void novo() {
        edicao = true;
       
    }

    public void novo100() {
        edicao100 = true;
      
    }

    public void cancelar() {
        edicao = false;
        listar();
    }

    public void cancelar100() {
        edicao100 = false;
        listar100();
    }

    public Usuario pu(Long id) {
        return App.usu(id);
    }

    public Usuario pun(Integer id) {
        if (id != null) {
            return App.usu(Long.valueOf(id));
        }
        return null;
    }

    public void salvarsenha() {
        try {

            boolean ok = true;
            CryptoUtil cri = new CryptoUtil();
            Usuario u = (Usuario) App.pegarObjetoDaSessao("usuario");
            if (!cri.encrypt("tghtj", senhaatual).equals(u.getSenha())) {
                ok = false;
                App.criarMensagemWarning("senha atual incorreta");
            }

            if (!senhanova.equals(csenhanova)) {
                ok = false;
                App.criarMensagemWarning("confirmacao da senha incorreta");
            }

            if (ok) {
                u.setSenha(cri.encrypt("tghtj", senhanova));
                u.setUsuarioAlteracao((Long) App.pegarObjetoDaSessao("idn"));
                u.setDataAlteracao(Calendar.getInstance().getTime());
                new UsuarioRepository(Usuario.class).atualizar(u);

                Evento e = new Evento();
                e.setTipo("configuracao");
                e.setDataAlteracao(Calendar.getInstance().getTime());
                e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                e.setDescricao("alterou a sennha");
                this.eventoRepository.salvar(e);

                App.criarMensagem("senha alterada com sucesso");
            }
        } catch (Exception e) {
            App.log(e);

            App.criarMensagemErro("erro " + e);
        }
    }
    
   
    public String getSenhaatual() {
        return senhaatual;
    }

    public void setSenhaatual(String senhaatual) {
        this.senhaatual = senhaatual;
    }

    public String getSenhanova() {
        return senhanova;
    }

    public void setSenhanova(String senhanova) {
        this.senhanova = senhanova;
    }

    public String getCsenhanova() {
        return csenhanova;
    }

    public void setCsenhanova(String csenhanova) {
        this.csenhanova = csenhanova;
    }

    String username = "";
    String password = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public void verificasessao() {
        HttpSession sessao = App.pegarSessao();
        if (sessao.isNew()) {
            LOG.log(Level.INFO, "id sessao:{0}", sessao.getId());
        }
    }

    public List<ControleAcessoBean.Sessao> sessoes() {
        return ControleAcessoBean.sessoes;
    }

    public Date getdate(long time) {
        return new Date(time);
    }

   

    public EventoRepository getEventoRepository() {
        return eventoRepository;
    }

    public void setEventoRepository(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public SimpleDateFormat getSd() {
        return sd;
    }

    public void setSd(SimpleDateFormat sd) {
        this.sd = sd;
    }

    public boolean isEdicao() {
        return edicao;
    }

    public void setEdicao(boolean edicao) {
        this.edicao = edicao;
    }

   

    public boolean isEdicao100() {
        return edicao100;
    }

    public void setEdicao100(boolean edicao100) {
        this.edicao100 = edicao100;
    }

    private static final Logger LOG = Logger.getLogger(ConfiguracaoBean.class.getName());

}
