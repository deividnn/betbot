/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Aposta;
import com.dnn.sistema.entidades.ApostaDet;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.ApostaDetRepository;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.repositories.ApostaRepository;
import com.dnn.sistema.repositories.UsuarioRepository;
import com.dnn.sistema.util.App;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.stream.Collectors;
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
public class ApostaBean implements Serializable {

    private Aposta aposta;
    private Aposta apostac;
    private List<Aposta> usuarios;
    private String permissao;
    private boolean novo;
    private ApostaRepository apostaRepository;
    private EventoRepository eventoRepository;

    private String campo;
    private String valor;
    private Usuario usu;
//

    @PostConstruct
    public void init() {
        App.verificaPermissao("apostas", (String) App.pegarObjetoDaSessao("nivel"));
        campo = "id";
        valor = "";
        novo = false;
        this.aposta = new Aposta();
        this.apostaRepository = new ApostaRepository(Aposta.class);
        this.eventoRepository = new EventoRepository(Evento.class);

        usu = (Usuario) App.pegarObjetoDaSessao("usuario");

        listar();

    }

    public void novo() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        novo = true;
        this.aposta = new Aposta();

        this.aposta.setPercentual(BigDecimal.ZERO);

        apostac = new Aposta();

    }

    public void cancelar() {
        tipos = "EXTRATO";
        novo = false;
        apostac = new Aposta();
        aposta = new Aposta();
        listar();
    }

    private String senhas;

    public void editar() {

        novo = true;
        this.aposta = apostac;

        apostac = new Aposta();

    }

    public void listar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.apostaRepository.listarHQL("SELECT vo FROM Aposta vo order by id desc");
        }

    }

    public void salvar() {
        boolean ok = true;
        try {

            Usuario uu = (Usuario) App.pegarObjetoDaSessao("usuario");

            if (ok) {

                aposta.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                aposta.setDataAlteracao(Calendar.getInstance().getTime());

                if (aposta.getId() == null) {

                    ok = salvarnovoaposta(ok);
                } else {
                    ok = editaraposta(ok);
                }

            }
        } catch (Exception e) {
            App.log(e);
            ok = false;
            aposta.setId(null);
            App.criarMensagemErro("erro ao salvar aposta:" + e);
        }
        if (ok) {
            listar();
            cancelar();
        }
    }

    private boolean editaraposta(boolean ok) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        this.apostaRepository.atualizar(aposta);

        Evento e = new Evento();
        e.setTipo("aposta");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
        e.setDescricao("atualizou aposta codigo=" + aposta.getId() + ", nome=" + aposta.getNome() + ", status=" + aposta.getStatus() + ", percentual=" + aposta.getPercentual());
        this.eventoRepository.salvar(e);

        App.criarMensagemWarning("aposta atualizado com sucesso");

        return ok;
    }

    private boolean salvarnovoaposta(boolean ok) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException {

        this.apostaRepository.salvar(aposta);
        //  App.usuarios.put(aposta.getId(), aposta);

        if (aposta.getStatus().equals("GANHOU") || aposta.getStatus().equals("PERDEU")) {
            UsuarioRepository rn = new UsuarioRepository(Usuario.class);
            List<Usuario> usul = rn.listarHQL("select vo from Usuario vo where status='ATIVO' and nivel='USUARIO' ");
            if (usul != null) {
                for (Usuario usu : usul) {

                    BigDecimal va = usu.getValorbanca();
                    BigDecimal van = BigDecimal.ZERO;
                    BigDecimal res = BigDecimal.ZERO;
                    if (usu.getLucro() == null) {
                        usu.setLucro(BigDecimal.ZERO);
                    }
                    BigDecimal lucro = usu.getLucro();
                    BigDecimal lucroa = usu.getLucro();
                    if (aposta.getStatus().equals("GANHOU")) {
                        res = va.multiply(aposta.getPercentual()).divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING);
                        lucro = lucro.add(res);
                        van = va;
                    } else {
                        res = va.multiply(aposta.getPercentual()).divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING);
                        van = va.subtract(res);
                    }
                    usu.setLucro(lucro);
                    usu.setValorbanca(van);
                    usu.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                    usu.setDataAlteracao(Calendar.getInstance().getTime());
                    rn.atualizar(usu);

                    ApostaDet det = new ApostaDet();
                    det.setIdaposta(aposta.getId());
                    det.setDataAlteracao(Calendar.getInstance().getTime());
                    det.setPercentual(aposta.getPercentual());
                    det.setStatus(aposta.getStatus());
                    det.setUsuario(usu.getId());
                    det.setUsuarioAlteracao(usu.getUsuarioAlteracao());
                    det.setValor(res);

                    new ApostaDetRepository(ApostaDet.class).atualizar(det);

                    Evento e = new Evento();
                    e.setTipo("usuario");
                    e.setDataAlteracao(Calendar.getInstance().getTime());
                    e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                    e.setDescricao("aposta " + aposta.getId() + ",atualizou valor banca do usuario " + usu.getId() + " de " + va + " para " + van + ",lucro de " + lucroa + " para " + usu.getLucro() + " - " + aposta.getStatus().toLowerCase() + " " + aposta.getPercentual() + "% ");
                    this.eventoRepository.salvar(e);

                    List<ControleAcessoBean.Sessao> uu = ControleAcessoBean.sessoes.stream().filter(v -> v.getUsuario() == usu.getId()).collect(Collectors.toList());
                    if (uu != null) {
                        for (ControleAcessoBean.Sessao sessao : uu) {
                            sessao.getSessao().setAttribute("usuario", usu);
                        }

                    }
                    App.usuarios.put(usu.getId(), usu);
                }
            }
        }

        Evento e = new Evento();
        e.setTipo("aposta");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
        e.setDescricao("cadastrou aposta codigo=" + aposta.getId() + ", nome=" + aposta.getNome() + ", status=" + aposta.getStatus() + ", percentual=" + aposta.getPercentual());
        this.eventoRepository.salvar(e);

        App.criarMensagem("aposta salvo com sucesso");

        return ok;
    }

    public void pesquisar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.apostaRepository.listarHQL("SELECT vo FROM Aposta vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    //  + " and  aposta!='teste' "
                    //  + " AND vo.id!=" + usux.getId() + ""
                    + " order by vo." + campo + " asc ");
        } else {
            this.usuarios = this.apostaRepository.listarHQL("SELECT vo FROM Aposta vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    //  + " and  aposta!='teste'"
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

    public Aposta getAposta() {
        return aposta;
    }

    public void setAposta(Aposta aposta) {
        this.aposta = aposta;
    }

    public List<Aposta> getApostas() {
        return usuarios;
    }

    public void setApostas(List<Aposta> usuarios) {
        this.usuarios = usuarios;
    }

    public String getPermissao() {
        return permissao;
    }

    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }

    public Aposta getApostac() {
        return apostac;
    }

    public void setApostac(Aposta apostac) {
        this.apostac = apostac;
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
        if (aposta != null) {
            if (aposta.getId() == null) {
                return true;
            } else {
                return usu.getNivel().equals("MASTER");
            }
        }
        return false;
    }

    private static final Logger LOG = Logger.getLogger(ApostaBean.class.getName());
}
