/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Saque;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.repositories.SaqueRepository;
import com.dnn.sistema.repositories.UsuarioRepository;
import com.dnn.sistema.util.App;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpSession;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class SaqueBean implements Serializable {

    private Saque saque;
    private Saque saquec;
    private List<Saque> usuarios;
    private String permissao;
    private boolean novo;
    private SaqueRepository saqueRepository;
    private EventoRepository eventoRepository;

    private String campo;
    private String valor;
    private Usuario usu;
//

    @PostConstruct
    public void init() {
        campo = "id";
        valor = "";
        novo = false;
        this.saque = new Saque();
        this.saqueRepository = new SaqueRepository(Saque.class);
        this.eventoRepository = new EventoRepository(Evento.class);

        usu = (Usuario) App.pegarObjetoDaSessao("usuario");

        listar();
     //   App.verificaPermissaoSM("saque", (String) App.pegarObjetoDaSessao("nivel"));

    }

    

    public void novo() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        novo = true;
        this.saque = new Saque();
        this.saque.setStatus("SOLICITACAO");
        this.saque.setValor(BigDecimal.ZERO);
        this.saque.setUsuario(usux.getId());
        saquec = new Saque();

    }

    public void cancelar() {
        tipos = "EXTRATO";
        novo = false;
        saquec = new Saque();
        saque = new Saque();
        listar();
    }

    private String senhas;

    public void editar() {

        novo = true;
        this.saque = saquec;

        saquec = new Saque();

    }

    public void listar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.saqueRepository.listarHQL("SELECT vo FROM Saque vo order by id desc");
        } else {
            this.usuarios = this.saqueRepository.listarHQL("SELECT vo FROM Saque vo where vo.usuario=" + usux.getId() + " "
                    + " order by id desc");
        }

    }

    public void salvar() {
        boolean ok = true;
        try {

            Usuario uu = (Usuario) App.pegarObjetoDaSessao("usuario");

            if (ok) {

                saque.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                saque.setDataAlteracao(Calendar.getInstance().getTime());

                if (saque.getId() == null) {

                    ok = salvarnovosaque(ok);
                } else {
                    ok = editarsaque(ok);
                }

            }
        } catch (Exception e) {
            App.log(e);
            ok = false;
            saque.setId(null);
            App.criarMensagemErro("erro ao salvar saque:" + e);
        }
        if (ok) {
            listar();
            cancelar();
        }
    }

    private boolean editarsaque(boolean ok) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        this.saqueRepository.atualizar(saque);

        if (saque.getStatus().equals("EFETUADO")) {
            UsuarioRepository rn = new UsuarioRepository(Usuario.class);
            Usuario usu = rn.porHQL("select vo from Usuario vo where id=" + saque.getUsuario() + " ");
            if (usu != null) {
                BigDecimal va = usu.getValorbanca().add(usu.getLucro());
                BigDecimal van = va.subtract(saque.getValor());
                usu.setValorbanca(van);
                usu.setLucro(BigDecimal.ZERO);
                usu.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                usu.setDataAlteracao(Calendar.getInstance().getTime());
                rn.atualizar(usu);

                Evento e = new Evento();
                e.setTipo("usuario");
                e.setDataAlteracao(Calendar.getInstance().getTime());
                e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                e.setDescricao("saque "+saque.getId()+",atualizou valor disponivel do usuario " + saque.getUsuario() + " de " + va + " para " + van + "  ");
                this.eventoRepository.salvar(e);

                App.usuarios.put(usu.getId(), usu);
                ControleAcessoBean.Sessao ss = ControleAcessoBean.sessoes.stream().filter(v->v.getUsuario()==usu.getId()).findAny().orElse(null);
                if(ss!=null){
                ss.getSessao().setAttribute("usuario", usu);
                }
              
            }
        }

        Evento e = new Evento();
        e.setTipo("saque");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
        e.setDescricao("atualizou saque codigo=" + saque.getId() + ", cod.ref=" + saque.getIdref() + ", usuario=" + saque.getUsuario() + ", status=" + saque.getStatus() + ", valor=" + saque.getValor());
        this.eventoRepository.salvar(e);

        App.criarMensagemWarning("saque atualizado com sucesso");

        return ok;
    }

    private boolean salvarnovosaque(boolean ok) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException {

        this.saqueRepository.salvar(saque);
        //  App.usuarios.put(saque.getId(), saque);

        Evento e = new Evento();
        e.setTipo("saque");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
        e.setDescricao("cadastrou saque codigo=" + saque.getId() + ", cod.ref=" + saque.getIdref() + ", usuario=" + saque.getUsuario() + ", status=" + saque.getStatus() + ", valor=" + saque.getValor());
        this.eventoRepository.salvar(e);

        App.criarMensagem("saque salvo com sucesso");

        return ok;
    }

    public void pesquisar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.saqueRepository.listarHQL("SELECT vo FROM Saque vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    //  + " and  saque!='teste' "
                    //  + " AND vo.id!=" + usux.getId() + ""
                    + " order by vo." + campo + " asc ");
        } else {
            this.usuarios = this.saqueRepository.listarHQL("SELECT vo FROM Saque vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    //  + " and  saque!='teste'"
                    + " and vo.usuario=" + usux.getId() + ""
                    // + " and vo.status in ('ATIVO') "
                    //    + " AND vo.id!=" + usux.getId() + " "
                    + " order by vo." + campo + " asc ");
        }
        if (this.usuarios.isEmpty()) {
            App.criarMensagemWarning("nenhum resultado encontrado");
        } else {
            App.criarMensagem(this.usuarios.size() + " registro(s) encontrado(s)");
        }

    }

    public Saque getSaque() {
        return saque;
    }

    public void setSaque(Saque saque) {
        this.saque = saque;
    }

    public List<Saque> getSaques() {
        return usuarios;
    }

    public void setSaques(List<Saque> usuarios) {
        this.usuarios = usuarios;
    }

    public String getPermissao() {
        return permissao;
    }

    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }

    public Saque getSaquec() {
        return saquec;
    }

    public void setSaquec(Saque saquec) {
        this.saquec = saquec;
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

    private String tipos;

    public String getTipos() {
        return tipos;
    }

    public void setTipos(String tipos) {
        this.tipos = tipos;
    }

    public boolean verificanivel() {
        Usuario usu = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (saque != null) {
            if (saque.getId() == null) {
                return true;
            } else {
                return usu.getNivel().equals("MASTER");
            }
        }
        return false;
    }

   

    private static final Logger LOG = Logger.getLogger(SaqueBean.class.getName());
}
