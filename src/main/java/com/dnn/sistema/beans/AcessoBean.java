/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import static com.dnn.sistema.beans.ControleAcessoBean.sessoes;
import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.repositories.BancaRepository;
import com.dnn.sistema.repositories.UsuarioRepository;
import com.dnn.sistema.util.App;
import static com.dnn.sistema.util.App.usuarios;
import com.dnn.sistema.util.CryptoUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class AcessoBean implements Serializable {

    private Usuario usuario;
    private UsuarioRepository usuarioRepository;
    private EventoRepository eventoRepository;
    private String so;
    private String navegador;
    private String hh;
    private String movel;

    @PostConstruct
    public void init() {
        usuario = new Usuario();
        this.usuarioRepository = new UsuarioRepository(Usuario.class);
        this.eventoRepository = new EventoRepository(Evento.class);
    }

    public void verificasessao() {
        Usuario usu = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usu != null) {
            App.criarObjetoDeSessao(usu, "usuario");
            App.redirecionarPagina("restrito/index.cs");
            usuario = new Usuario();
        }
    }

    public void logar() throws NoSuchAlgorithmException {
        try {
            criarusuarioteste();
            App.criarObjetoDeSessao(false, "jaselecionado");
            App.criarObjetoDeSessao(so, "so");
            App.criarObjetoDeSessao(navegador, "navegador");
            if (movel.equals("Desktop")) {
                App.criarObjetoDeSessao(Integer.valueOf(hh) - 480, "hh");
            } else {
                App.criarObjetoDeSessao(300, "hh");
            }
            App.executarJavascript("PF('statusDialog').show()");
            List<Object> param = new ArrayList<>();
            CryptoUtil cri = new CryptoUtil();
            param.add(usuario.getUsuario());
            param.add(cri.encrypt("tghtj", usuario.getSenha()));
            Usuario usu = this.usuarioRepository.objetoPorHqlParam(
                    "select vo from Usuario vo where vo.usuario=?1 and vo.senha=?2",
                    param);
            if (usu == null) {
                param = new ArrayList<>();
                String co = usuario.getUsuario().replaceAll("\\D", "");
                if (!co.isEmpty()) {
                    param.add(Long.valueOf(co));
                    param.add(cri.encrypt("tghtj", usuario.getSenha()));
                    usu = this.usuarioRepository.objetoPorHqlParam(
                            "select vo from Usuario vo where vo.id=?1 and vo.senha=?2",
                            param);
                }
            }
            if (usu != null) {
                if (usu.getLucro() == null) {
                    usu.setLucro(BigDecimal.ZERO);
                }
                if (usu.getValorbanca() == null) {
                    usu.setValorbanca(BigDecimal.ZERO);
                }
                if (usu.getStatus().equals("ATIVO")) {
                    HttpSession s = null;
                    for (Iterator<ControleAcessoBean.Sessao> iterator = sessoes.iterator(); iterator.hasNext();) {
                        ControleAcessoBean.Sessao next = iterator.next();
                        if (next.getUsuario() == usu.getId()) {
                            s = next.getSessao();
                            if (s != null) {
                                LocalDateTime start = LocalDateTime.now();
                                LocalDateTime end = App.convertToLocalDateTime(new Date(s.getLastAccessedTime()));

                                Duration duration = Duration.between(start, end);
                                if (duration.abs().getSeconds() > 30) {
                                    try {
                                        s.invalidate();
                                    } catch (Exception e) {
                                        App.log(e);
                                    }

                                    iterator.remove();
                                    s = null;
                                    break;
                                }
                            }
                        }
                    }
                    if (s == null) {
                        logadocomsucesso(usu);
                    } else {
                        App.executarJavascript("PF('statusDialog').hide();");
                        App.criarMensagemErro("usuário já logado no sistema");
                        Evento e = new Evento();
                        e.setTipo("login");
                        e.setDataAlteracao(Calendar.getInstance().getTime());
                        e.setUsuarioAlteracao(usu.getId());
                        e.setDescricao("usuário já logado no sistema, so=" + so + " ,navegador=" + navegador + ",dispositivo=" + movel + " ");
                        this.eventoRepository.salvar(e);
                    }
                } else {
                    usuarioinativooubloqueado(usu);

                }
            } else {
                usuarioousenhaincorreto();

            }
        } catch (Exception e) {
            App.log(e);
            App.executarJavascript("PF('statusDialog').hide();");
            App.criarMensagemErro("erro nao catalogado:" + e);
        }
    }

    private void usuarioousenhaincorreto() {
        Evento e = new Evento();
        e.setTipo("login");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setDescricao("tentativa login-usuario/senha incorretos,usuario=" + usuario.getUsuario() + ", so=" + so + " ,navegador=" + navegador + ",dispositivo=" + movel + " ");
        this.eventoRepository.salvar(e);
        App.executarJavascript("PF('statusDialog').hide();");
        App.criarMensagemErro("usuario/senha incorretos");
    }

    private void usuarioinativooubloqueado(Usuario usu) {
        Evento e = new Evento();
        e.setTipo("login");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao(usu.getId());
        e.setDescricao("tentativa login-usuario inativo/bloqueado, so=" + so + " ,navegador=" + navegador + ",dispositivo=" + movel + " ");
        this.eventoRepository.salvar(e);
        App.executarJavascript("PF('statusDialog').hide();");
        App.criarMensagemWarning("usuario inativo/bloqueado");
    }

    private void logadocomsucesso(Usuario usu) {

        ControleAcessoBean.Sessao s = new ControleAcessoBean.Sessao();
        s.setSessao(App.pegarSessao());
        s.setUsuario(usu.getId());
        ControleAcessoBean.Sessao ss = sessoes.stream().filter(v -> v.getUsuario() == usu.getId()).findAny().orElse(null);
        if (ss == null) {
            App.criarObjetoDeSessao(usu, "usuario");
            App.criarObjetoDeSessao(usu.getNivel(), "nivel");
            App.criarObjetoDeSessao(usu.getId(), "idn");
            sessoes.add(s);

            if (usuarios == null) {
                usuarios = new HashMap<>();
                List<Usuario> u = new UsuarioRepository(Usuario.class).listarHQL("select vo from Usuario vo");
                for (Usuario x : u) {
                    usuarios.put(x.getId(), x);
                }
            }
            Evento e = new Evento();
            e.setTipo("login");
            e.setDataAlteracao(Calendar.getInstance().getTime());
            e.setUsuarioAlteracao(usu.getId());
            e.setDescricao("login sucesso, so=" + so + " ,navegador=" + navegador + ",dispositivo=" + movel + " ");
            this.eventoRepository.salvar(e);
            App.redirecionarPagina("restrito/index.cs");
            App.executarJavascript("PF('statusDialog').hide();");

            usuario = new Usuario();
        }
    }

    private void criarusuarioteste() {
        List<Usuario> ux = usuarioRepository.listarHQL("SELECT vo FROM Usuario vo order by id desc");
        if (ux.isEmpty()) {
            usuarioRepository.comitSqli("INSERT INTO public.usuario (id, data_alteracao, nivel, nome, senha, status, usuario, usuario_alteracao, valorbanca,cpf) \n"
                    + "VALUES (1, "
                    + "now(),"
                    + " 'MASTER', "
                    + "'Teste',\n"
                    + " 'fK+Nhr0F66nX/JO662CPFw==', "
                    + "'ATIVO',"
                    + " 'teste',"
                    + " 1, "
                    + "0,"
                    + "'00000000000');\n"
                    + "ALTER SEQUENCE public.usuario_id_seq RESTART WITH 2;");
        }
    }

    public String logout() {

        try {
            Usuario usu = (Usuario) App.pegarObjetoDaSessao("usuario");
            so = (String) App.pegarObjetoDaSessao("so");
            navegador = (String) App.pegarObjetoDaSessao("navegador");

            Evento e = new Evento();
            e.setTipo("login");
            e.setDataAlteracao(Calendar.getInstance().getTime());
            e.setUsuarioAlteracao(usu.getId());
            e.setDescricao("saiu do sistema, so=" + so + " ,navegador=" + navegador + ",dispositivo=" + movel + " ");
            this.eventoRepository.salvar(e);

            App.criarObjetoDeSessao(false, "jaselecionado");

            for (Iterator<ControleAcessoBean.Sessao> iterator = sessoes.iterator(); iterator.hasNext();) {
                ControleAcessoBean.Sessao next = iterator.next();
                if (next.getUsuario() == usu.getId()) {
                    iterator.remove();
                    break;
                }
            }
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            }
            return "/acesso?faces-redirect=true";
        } catch (Exception e) {
            App.log(e);

            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            return "/acesso?faces-redirect=true";
        }

    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }

    public void setUsuarioRepository(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String getSo() {
        return so;
    }

    public void setSo(String so) {
        this.so = so;
    }

    public String getNavegador() {
        return navegador;
    }

    public void setNavegador(String navegador) {
        this.navegador = navegador;
    }

    public String getHh() {
        return hh;
    }

    public void setHh(String hh) {
        this.hh = hh;
    }

    public String getMovel() {
        return movel;
    }

    public void setMovel(String movel) {
        this.movel = movel;
    }

}
