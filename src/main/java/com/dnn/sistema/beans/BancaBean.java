/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Banca;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.EventoRepository;
import com.dnn.sistema.repositories.BancaRepository;
import com.dnn.sistema.repositories.UsuarioRepository;
import com.dnn.sistema.util.App;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
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
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class BancaBean implements Serializable {

    private Banca banca;
    private Banca bancac;
    private List<Banca> usuarios;
    private String permissao;
    private boolean novo;
    private BancaRepository bancaRepository;
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
        this.banca = new Banca();
        this.bancaRepository = new BancaRepository(Banca.class);
        this.eventoRepository = new EventoRepository(Evento.class);

        usu = (Usuario) App.pegarObjetoDaSessao("usuario");

        listar();
        //   App.verificaPermissaoSM("banca", (String) App.pegarObjetoDaSessao("nivel"));

    }

    public void novo() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        novo = true;
        this.banca = new Banca();
        this.banca.setStatus("SOLICITACAO");
        this.banca.setValor(BigDecimal.ZERO);
        this.banca.setUsuario(usux.getId());
        bancac = new Banca();
        u = null;

    }

    public void cancelar() {
        tipos = "EXTRATO";
        novo = false;
        bancac = new Banca();
        banca = new Banca();
        listar();
    }

    private String senhas;

    public void editar() {

        novo = true;
        this.banca = bancac;

        String pasta = FacesContext.getCurrentInstance().
                getExternalContext().getRealPath("/resources/arquivos/comprovantes/");
        File p = new File(pasta);
        if (!p.exists()) {
            p.mkdirs();
        }

        File ud = new File(p.getAbsolutePath() + File.separator + banca.getId() + ".pdf");

        if (ud.exists()) {
            u = ud;
            arquivop = ud.getName();
        }

        bancac = new Banca();

    }

    private String arquivop;
    private File u;

    public String getArquivop() {
        return arquivop;
    }

    public void setArquivop(String arquivop) {
        this.arquivop = arquivop;
    }

    public StreamedContent salvarArquivoe() {

        String pasta = FacesContext.getCurrentInstance().
                getExternalContext().getRealPath("/resources/arquivos/comprovantes/");
        File p = new File(pasta);
        if (!p.exists()) {
            p.mkdirs();
        }

        if (!u.exists()) {
            App.criarMensagemErro("arquivo nao existe");
        }
        return salvarArquivox();

    }

    public StreamedContent salvarArquivox() {
        try {
            return salvarArquivo(this.u.getAbsolutePath(), "application/octet-stream", "" + this.arquivop);
        } catch (Exception e) {
            App.log(e);
            return null;
        }

    }

    public StreamedContent salvarArquivo(String caminho, String tipo, String nomeSaida) throws FileNotFoundException {
        InputStream input = new FileInputStream(new File(caminho));
        DefaultStreamedContent sd = DefaultStreamedContent.builder().name(nomeSaida).contentEncoding("UTF-8").stream(() -> input).contentType(tipo).build();
        return sd;
    }

    public void arquivopdf(FileUploadEvent event) {
        try {

            String pasta = FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/resources/arquivos/comprovantes/");
            File p = new File(pasta);
            if (!p.exists()) {
                p.mkdirs();
            }
            u = new File(p.getAbsolutePath() + File.separator + event.getFile().getFileName());
            arquivop = u.getName();

            try (OutputStream out = new FileOutputStream(u)) {
                out.write(event.getFile().getContent());
            }

        } catch (Exception e) {
            App.log(e);
            App.criarMensagemErro("erro ao processar " + e);
        }
    }

    public void listar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.bancaRepository.listarHQL("SELECT vo FROM Banca vo order by id desc");
        } else {
            this.usuarios = this.bancaRepository.listarHQL("SELECT vo FROM Banca vo where vo.usuario=" + usux.getId() + " "
                    + " order by id desc");
        }

    }

    public void salvar() {
        boolean ok = true;
        try {

            Usuario uu = (Usuario) App.pegarObjetoDaSessao("usuario");

            if (arquivop == null) {
                ok = false;
                App.criarMensagemErro("favor anexar comprovante");
            } else {
                if (arquivop.isEmpty()) {
                    ok = false;
                    App.criarMensagemErro("favor anexar comprovante");
                }
            }

            if (ok) {

                banca.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                banca.setDataAlteracao(Calendar.getInstance().getTime());

                if (banca.getId() == null) {

                    ok = salvarnovobanca(ok);
                } else {
                    ok = editarbanca(ok);
                }

            }
        } catch (Exception e) {
            App.log(e);
            ok = false;
            banca.setId(null);
            App.criarMensagemErro("erro ao salvar banca:" + e);
        }
        if (ok) {
            listar();
            cancelar();
        }
    }

    private boolean editarbanca(boolean ok) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        this.bancaRepository.atualizar(banca);

        if (banca.getStatus().equals("ACEITO")) {
            UsuarioRepository rn = new UsuarioRepository(Usuario.class);
            Usuario usu = rn.porHQL("select vo from Usuario vo where id=" + banca.getUsuario() + " ");
            if (usu != null) {
                BigDecimal va = usu.getValorbanca();
                BigDecimal van = va.add(banca.getValor());
                usu.setValorbanca(van);
                usu.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                usu.setDataAlteracao(Calendar.getInstance().getTime());
                rn.atualizar(usu);

                Evento e = new Evento();
                e.setTipo("usuario");
                e.setDataAlteracao(Calendar.getInstance().getTime());
                e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
                e.setDescricao("banca " + banca.getId() + ",atualizou valor banca do usuario " + banca.getUsuario() + " de " + va + " para " + van + " ");
                this.eventoRepository.salvar(e);

                App.usuarios.put(usu.getId(), usu);
                ControleAcessoBean.Sessao ss = ControleAcessoBean.sessoes.stream().filter(v -> v.getUsuario() == usu.getId()).findAny().orElse(null);
                if (ss != null) {
                    ss.getSessao().setAttribute("usuario", usu);
                }
            }
        }

        Evento e = new Evento();
        e.setTipo("banca");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
        e.setDescricao("atualizou banca codigo=" + banca.getId() + ", cod.ref=" + banca.getIdref() + ", usuario=" + banca.getUsuario() + ", status=" + banca.getStatus() + ", valor=" + banca.getValor());
        this.eventoRepository.salvar(e);

        App.criarMensagemWarning("banca atualizado com sucesso");

        return ok;
    }

    private boolean salvarnovobanca(boolean ok) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException, IOException {

        this.bancaRepository.salvar(banca);
        //  App.usuarios.put(banca.getId(), banca);

        Evento e = new Evento();
        e.setTipo("banca");
        e.setDataAlteracao(Calendar.getInstance().getTime());
        e.setUsuarioAlteracao((long) App.pegarObjetoDaSessao("idn"));
        e.setDescricao("cadastrou banca codigo=" + banca.getId() + ", cod.ref=" + banca.getIdref() + ", usuario=" + banca.getUsuario() + ", status=" + banca.getStatus() + ", valor=" + banca.getValor());
        this.eventoRepository.salvar(e);

        if (u != null) {
            String pasta = FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/resources/arquivos/comprovantes/");
            File p = new File(pasta);
            if (!p.exists()) {
                p.mkdirs();
            }
            File ud = new File(p.getAbsolutePath() + File.separator + banca.getId() + ".pdf");

            FileUtils.copyFile(u, ud);
        }
        App.criarMensagem("banca salvo com sucesso");

        return ok;
    }

    public void pesquisar() {
        Usuario usux = (Usuario) App.pegarObjetoDaSessao("usuario");
        if (usux.getNivel().equals("MASTER")) {
            this.usuarios = this.bancaRepository.listarHQL("SELECT vo FROM Banca vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    //  + " and  banca!='teste' "
                    //  + " AND vo.id!=" + usux.getId() + ""
                    + " order by vo." + campo + " asc ");
        } else {
            this.usuarios = this.bancaRepository.listarHQL("SELECT vo FROM Banca vo"
                    + " where upper(cast(vo." + campo + " as text)) like '%"
                    + valor.replace("'", "").toUpperCase() + "%'"
                    //  + " and  banca!='teste'"
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

    public Banca getBanca() {
        return banca;
    }

    public void setBanca(Banca banca) {
        this.banca = banca;
    }

    public List<Banca> getBancas() {
        return usuarios;
    }

    public void setBancas(List<Banca> usuarios) {
        this.usuarios = usuarios;
    }

    public String getPermissao() {
        return permissao;
    }

    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }

    public Banca getBancac() {
        return bancac;
    }

    public void setBancac(Banca bancac) {
        this.bancac = bancac;
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
        if (banca != null) {
            if (banca.getId() == null) {
                return true;
            } else {
                return usu.getNivel().equals("MASTER");
            }
        }
        return false;
    }

    private static final Logger LOG = Logger.getLogger(BancaBean.class.getName());
}
