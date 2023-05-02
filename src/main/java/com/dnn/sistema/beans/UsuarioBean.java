/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.repositories.BancaRepository;
import com.dnn.sistema.repositories.UsuarioRepository;
import com.dnn.sistema.util.App;
import com.dnn.sistema.util.CryptoUtil;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
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
import org.apache.commons.lang.SerializationUtils;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class UsuarioBean implements Serializable {

    private Usuario usuario;
    private Usuario usuarioc;
    private List<Usuario> usuarios;
    private String permissao;
    private boolean novo;
    private UsuarioRepository usuarioRepository;
    private EventoRepository eventoRepository;

    private String campo;
    private String valor;
    private Usuario usu;

    private List<String> niveis;

//
    @PostConstruct
    public void init() {
        App.verificaPermissao("usuarios", (String) App.pegarObjetoDaSessao("nivel"));
        campo = "id";
        valor = "";
        novo = false;
        this.usuario = new Usuario();
        this.usuarioRepository = new UsuarioRepository(Usuario.class);
        this.eventoRepository = new EventoRepository(Evento.class);

        usu = (Usuario) App.pegarObjetoDaSessao("usuario");

        niveis = new ArrayList<>();

        niveis.add("MASTER");
        niveis.add("USUARIO");

        listar();

    }

    public boolean isValidEmailAddress(String email) {
        String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
        Matcher matcher;
        Pattern pattern;
        pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void novo() {

        novo = true;
        this.usuario = new Usuario();
        this.usuario.setNivel("USUARIO");
        this.usuario.setValorbanca(BigDecimal.ZERO);
        this.usuario.setCpf("");

        usuarioc = new Usuario();

    }

    public void cancelar() {
        tipos = "EXTRATO";
        novo = false;
        usuarioc = new Usuario();
        usuario = new Usuario();
        listar();
    }

    private String senhas;

    public void editar() {

        novo = true;
        this.usuario = usuarioc;

        senhas = (String) SerializationUtils.clone(usuario.getSenha());
        usuarioc = new Usuario();

    }

    public void listar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.usuarioRepository.listarHQL("SELECT vo FROM Usuario vo where usuario!='teste' "
                    + "order by id desc");
        } 
    }

    public void salvar() {
        boolean ok = true;
        try {

            Usuario uu = (Usuario) App.pegarObjetoDaSessao("usuario");

            if (usuario.getId() != null && usu.getId().intValue() == usuario.getId().intValue() && !usu.getStatus().equals(usuario.getStatus())) {
                ok = false;
                App.criarMensagemErro("nao é possivel alterar proprio status");
            }
            if (usuario.getCpf() != null) {
                if (!usuario.getCpf().trim().isEmpty()) {
                    if (usuario.getCpf().trim().length() != 11) {
                        ok = false;
                        App.criarMensagemErro("cpf deve ter 11 digitos");
                    }
                }
            }
            if (ok) {
                CryptoUtil cri = new CryptoUtil();

                usuario.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                usuario.setDataAlteracao(Calendar.getInstance().getTime());

                if (usuario.getId() == null) {

                    ok = salvarnovousuario(cri, ok);
                } else {
                    ok = editarusuario(ok, cri);
                }

            }
        } catch (Exception e) {
            App.log(e);
            ok = false;
            usuario.setId(null);
            App.criarMensagemErro("erro ao salvar usuario:" + e);
        }
        if (ok) {
            listar();
            cancelar();
        }
    }

    private boolean editarusuario(boolean ok, CryptoUtil cri) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        Usuario verifica = this.usuarioRepository.porHQL("select vo from  Usuario vo "
                + " where (vo.usuario='" + usuario.getUsuario() + "' "
                + ")"
                + "  and vo.id!=" + usuario.getId() + " ");
        if (verifica != null) {
            App.criarMensagemErro("usuario ja existe");
            ok = false;
        } else {

            if (this.usuario.getSenha() == null) {
                usuario.setSenha(senhas);
            }
            if (this.usuario.getSenha().isEmpty()) {
                usuario.setSenha(senhas);
            }
            if (!senhas.equals(this.usuario.getSenha())) {
                usuario.setSenha(cri.encrypt("tghtj", usuario.getSenha()));
            }

            this.usuarioRepository.atualizar(usuario);

            Evento e = new Evento();
            e.setTipo("usuario");
            e.setDataAlteracao(Calendar.getInstance().getTime());
            e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
            e.setDescricao("atualizou usuario codigo=" + usuario.getId() + "," + usuario.getUsuario() + " ,nome=" + usuario.getNome() + ", cpf=" + usuario.getCpf() + ", nivel=" + usuario.getNivel() + ", status=" + usuario.getStatus() + ", telefone=" + usuario.getTelefone() + ", wpp=" + usuario.getWpp() + ", valorbanca=" + usuario.getValorbanca());
            this.eventoRepository.salvar(e);

            App.criarMensagemWarning("usuario atualizado com sucesso");

        }
        return ok;
    }

    private boolean salvarnovousuario(CryptoUtil cri, boolean ok) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException {
        usuario.setSenha(cri.encrypt("tghtj", usuario.getSenha()));
        usuario.setCpf(usuario.getCpf().replaceAll("\\D", ""));
        Usuario verifica = this.usuarioRepository.porHQL("select vo from  Usuario vo "
                + " where vo.usuario='" + usuario.getUsuario() + "' "
                + " ");
        if (verifica != null) {
            App.criarMensagemErro("usuario/email ja existe");
            ok = false;
        } else {
            this.usuarioRepository.salvar(usuario);
            App.usuarios.put(usuario.getId(), usuario);

            Usuario uu = this.usuarioRepository.porHQL("select vo from Usuario vo where id=" + usuario.getUsuarioAlteracao() + " ");

            uu.setDataAlteracao(Calendar.getInstance().getTime());
            uu.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
            this.usuarioRepository.salvara(uu);
            App.usuarios.put(uu.getId(), uu);
            

            Evento e = new Evento();
            e.setTipo("usuario");
            e.setDataAlteracao(Calendar.getInstance().getTime());
            e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
            e.setDescricao("cadastrou usuario codigo=" + usuario.getId() + "," + usuario.getUsuario() + " ,nome=" + usuario.getNome() + ", cpf=" + usuario.getCpf() + ", nivel=" + usuario.getNivel() + ", status=" + usuario.getStatus() + ", telefone=" + usuario.getTelefone() + ", wpp=" + usuario.getWpp() + ", valorbanca=" + usuario.getValorbanca());
            this.eventoRepository.salvar(e);

            App.criarMensagem("usuario salvo com sucesso");

        }
        return ok;
    }

    public void pesquisar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.usuarioRepository.listarHQL("SELECT vo FROM Usuario vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    + " and  usuario!='teste' "
                    //  + " AND vo.id!=" + usux.getId() + ""
                    + " order by vo." + campo + " asc ");
        } else {
            this.usuarios = this.usuarioRepository.listarHQL("SELECT vo FROM Usuario vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    + " and  usuario!='teste'"
                    //  + " and vo.usuarioMaster=" + usux.getId() + ""
                    + " and vo.status in ('ATIVO') "
                    //    + " AND vo.id!=" + usux.getId() + " "
                    + " order by vo." + campo + " asc ");
        }
        if (this.usuarios.isEmpty()) {
            App.criarMensagemWarning("nenhum resultado encontrado");
        } else {
            App.criarMensagem(this.usuarios.size() + " registro(s) encontrado(s)");
        }

    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public String getPermissao() {
        return permissao;
    }

    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }

    public Usuario getUsuarioc() {
        return usuarioc;
    }

    public void setUsuarioc(Usuario usuarioc) {
        this.usuarioc = usuarioc;
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

    public List<String> getNiveis() {
        return niveis;
    }

    public void setNiveis(List<String> niveis) {
        this.niveis = niveis;
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
        if (usuario != null) {
            if (usuario.getId() == null) {
                return true;
            } else {
                return usu.getNivel().equals("MASTER");
            }
        }
        return false;
    }

    private static final Logger LOG = Logger.getLogger(UsuarioBean.class.getName());
}
