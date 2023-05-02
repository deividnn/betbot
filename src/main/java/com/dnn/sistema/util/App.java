/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.util;

import com.dnn.sistema.beans.ControleAcessoBean;
import com.dnn.sistema.entidades.Usuario;
import com.dnn.sistema.repositories.UsuarioRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.internal.SessionFactoryImpl;
import org.postgresql.util.PSQLException;
import org.primefaces.PrimeFaces;

/**
 *
 * @author deivid
 */
@WebListener
public class App implements ServletContextListener {

    public static Map<Long, Usuario> usuarios;
    public static String userbd;
    public static String passbd;
    public static String host;
    public static String bd;
    public static String porta;
    public static String pgadmin;

    static {
        userbd = "teste";
        passbd = "teste";
        pgadmin = "";
        host = "localhost";
        bd = "aposta";
        porta = "5432";
    }

    public static Session pegaSessaoAtual() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    public static void listFilesForFolder(File folder, int dias, String extensao) {
        try {
            if (folder.listFiles().length > 30) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        listFilesForFolder(fileEntry, dias, extensao);
                    } else {
                        if (days2(new Date(fileEntry.lastModified()), new Date()) > dias) {
                            if (FilenameUtils.getExtension(fileEntry.getName()).equals(extensao)) {
                                FileUtils.deleteQuietly(fileEntry);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log(e);
        }
    }

    public static long days2(Date start, Date end) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(start);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(end);

        //end Saturday to start Saturday 
        return (c2.getTimeInMillis() - c1.getTimeInMillis()) / (1000 * 60 * 60 * 24);

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Connection con = null;
        try {
            File pl1 = new File(pasta() + File.separator + "logs");
            if (pl1.exists() && pl1.isDirectory()) {
                listFilesForFolder(pl1, 30, "cs");
            }
            final String catalinaBase = System.getProperty("catalina.base");
            pl1 = new File(catalinaBase + File.separator + "logs");
            if (pl1.exists() && pl1.isDirectory()) {
                listFilesForFolder(pl1, 30, "log");
            }
            if (pl1.exists() && pl1.isDirectory()) {
                listFilesForFolder(pl1, 30, "txt");
            }
            String local = System.getProperty("java.io.tmpdir") + File.separator + "cache" + File.separator + "betbot";
            File pasta = new File(local);
            if (!pasta.exists()) {
                boolean cc = pasta.setWritable(true);
                LOG.log(Level.INFO, "{0}", cc);
                pasta.mkdirs();
            }
            try {
                File temp = new File(local);
                if (!temp.exists()) {
                    temp.mkdirs();
                } else {
                    FileUtils.cleanDirectory(temp);
                    FileUtils.deleteDirectory(temp);
                }
            } catch (Exception e) {
                log(e);
            }
            pegaroproperties(sce);

            con = atualizarsequences();

            //
            ControleAcessoBean.verificando = true;
            //
            new Thread(new ControleAcessoBean.Log()).start();
            new Thread(new ControleAcessoBean.T()).start();
         

        } catch (Exception e) {
            log(e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    log(ex);
                }
            }
        }

        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Connection atualizarsequences() throws SQLException, ClassNotFoundException {
        HibernateUtil.getSessionFactory();
        String h = "SELECT 'SELECT SETVAL(' ||\n"
                + "       quote_literal(quote_ident(PGT.schemaname) || '.' || quote_ident(S.relname)) ||\n"
                + "       ', COALESCE(MAX(' ||quote_ident(C.attname)|| '), 1) ) FROM ' ||\n"
                + "       quote_ident(PGT.schemaname)|| '.'||quote_ident(T.relname)|| ';' as seq\n"
                + "FROM pg_class AS S,\n"
                + "     pg_depend AS D,\n"
                + "     pg_class AS T,\n"
                + "     pg_attribute AS C,\n"
                + "     pg_tables AS PGT\n"
                + "WHERE S.relkind = 'S'\n"
                + "    AND S.oid = D.objid\n"
                + "    AND D.refobjid = T.oid\n"
                + "    AND D.refobjid = C.attrelid\n"
                + "    AND D.refobjsubid = C.attnum\n"
                + "    AND T.relname = PGT.tablename\n"
                + "ORDER BY S.relname;";
        Connection conx = pegarconexao();
        try (Statement st = conx.createStatement(); ResultSet rs = st.executeQuery(h)) {
            while (rs.next()) {
                String sql = rs.getString("seq");
                try (Statement stx = conx.createStatement()) {
                    stx.executeQuery(sql);
                }
            }
        }
        return conx;
    }

    private void pegaroproperties(ServletContextEvent sce) throws IOException {
        String inc2 = sce.getServletContext().getResource("/WEB-INF/inc.properties").getFile();
        File inc = new File(inc2);
        if (!inc.exists()) {
            boolean cc = inc.createNewFile();
            LOG.log(Level.INFO, "{0}", cc);
        }
        Properties prop = new Properties();
        try (FileInputStream infx = FileUtils.openInputStream(inc)) {
            prop.load(infx);
        }
        userbd = prop.getProperty("userbd");
        if (userbd == null) {
            userbd = "teste";
            passbd = "teste";
            porta = "5432";

            prop.setProperty("bd", bd);
            prop.setProperty("userbd", userbd);
            prop.setProperty("passbd", passbd);
            prop.setProperty("porta", porta);
            prop.setProperty("host", host);
            try (FileOutputStream inf = FileUtils.openOutputStream(inc)) {
                prop.store(inf, null);
            }
        } else {
            userbd = prop.getProperty("userbd");
            passbd = prop.getProperty("passbd");
            host = prop.getProperty("host");
            bd = prop.getProperty("bd");
            porta = prop.getProperty("porta");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ControleAcessoBean.verificando = false;
            ControleAcessoBean.sessoes = new ArrayList<>();
            HibernateUtil.shutdown();
            fechandoConexoes();
            cleanThreadLocals();
            preventTomcatNetbeansRedeployBug(sce.getServletContext());
            HibernateUtil.getSessionFactory().close();

        } catch (Exception e) {
            log(e);
        }
    }

    public static Usuario usu(Long id) {
        if (usuarios == null) {
            usuarios = new HashMap<>();
            List<Usuario> u = new UsuarioRepository(Usuario.class).listarHQL("select vo from Usuario vo");
            for (Usuario x : u) {
                usuarios.put(x.getId(), x);
            }
        }
        Usuario u = usuarios.get(id);
        if (u == null) {
            u = new UsuarioRepository(Usuario.class).porHQL("select vo from Usuario vo where id =" + id + " ");
            usuarios.put(id, u);
        }

        return u;
    }

    public static void atualizarForm(String form) {
        if (PrimeFaces.current() != null) {
            PrimeFaces.current().ajax().update(form);
        }
    }

    private static void preventTomcatNetbeansRedeployBug(ServletContext sce) {
        final String contextPath = sce.getContextPath();
        final String catalinaBase = System.getProperty("catalina.base");
        final File catalinaBaseContext = new File(catalinaBase, "conf/Catalina/localhost");
        if (catalinaBaseContext.exists() && catalinaBaseContext.canRead()) {
            final File[] contexts = catalinaBaseContext.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equals(contextPath.substring(1) + ".xml");
                }
            });

        }
    }

    private static void cleanThreadLocals() {
        try {
            // Get a reference to the thread locals table of the current thread
            Thread thread = Thread.currentThread();
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Object threadLocalTable = threadLocalsField.get(thread);

            // Get a reference to the array holding the thread local variables inside the
            // ThreadLocalMap of the current thread
            Class threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = threadLocalMapClass.getDeclaredField("table");
            tableField.setAccessible(true);
            Object table = tableField.get(threadLocalTable);

            // The key to the ThreadLocalMap is a WeakReference object. The referent field of this object
            // is a reference to the actual ThreadLocal variable
            Field referentField = Reference.class.getDeclaredField("referent");
            referentField.setAccessible(true);

            for (int i = 0; i < Array.getLength(table); i++) {
                // Each entry in the table array of ThreadLocalMap is an Entry object
                // representing the thread local reference and its value
                Object entry = Array.get(table, i);

                if (entry != null) {
                    // Get a reference to the thread local object and remove it from the table
                    ThreadLocal threadLocal = (ThreadLocal) referentField.get(entry);

                    if (threadLocal != null) {
                        threadLocal.remove();
                    }
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException | ArrayIndexOutOfBoundsException e) {
            App.log(e);
            log(e);
            // We will tolerate an exception here and just log it
            throw new IllegalStateException(e);
        }
    }

    private static boolean closeSessionFactoryIfC3P0ConnectionProvider() {
        boolean done = false;
        SessionFactory value = HibernateUtil.getSessionFactory();
        if (value instanceof SessionFactoryImpl) {
            value.close();
            done = true;
        }
        return done;
    }

    private void fechandoConexoes() {
        closeSessionFactoryIfC3P0ConnectionProvider();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                App.log(e);
                log(e);
            }
        }

    }

    /**
     * limpa todos os campos de um formulari
     *
     * @param idForm idForm id do formulario
     */
    public static void resetarFormulario(String idForm) {
        PrimeFaces.current().resetInputs(idForm);
    }

    /**
     * cria uma mensagem que sera mostrada no p:growl do primeface
     *
     * @param texto texto é a mensagem
     */
    public static void criarMensagem(String texto) {
        FacesMessage mesagem = new FacesMessage(texto);
        FacesContext.getCurrentInstance().addMessage(texto, mesagem);
    }

    public static void criarObjetoDeSessao(Object obj, String nome) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(nome, obj);
    }

    public static Object pegarObjetoDaSessao(String nomeSessao) {
        if (FacesContext.getCurrentInstance() != null) {
            HttpSession sessao = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            if (sessao != null) {
                return sessao.getAttribute(nomeSessao);
            }
            redirecionarPagina("acesso.cs");
            return null;
        } else {
            return null;
        }

    }

    public static HttpSession pegarSessao() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpSession sessao = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            if (sessao != null) {
                return sessao;
            }
            redirecionarPagina("acesso.cs");
            return null;
        } else {
            return null;
        }

    }

    public static void redirecionarPagina(String pagina) {
        String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url + "/" + pagina);
        } catch (IOException ex) {
            App.log(ex);

        }
    }

    /**
     * cria uma mensagem que sera mostrada no p:growl do primeface como aviso
     *
     * @param texto texto é a mensagem
     */
    public static void criarMensagemWarning(String texto) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesMessage mesagem = new FacesMessage(FacesMessage.SEVERITY_WARN, texto,
                    texto);
            FacesContext.getCurrentInstance().addMessage(texto,
                    mesagem);
        }
    }

    /**
     * cria uma mensagem que sera mostrada no p:growl do primeface como erro
     *
     * @param texto texto é a mensagem
     */
    public static void criarMensagemErro(String texto) {
        FacesMessage mesagem = new FacesMessage(FacesMessage.SEVERITY_ERROR, texto,
                texto);
        FacesContext.getCurrentInstance().addMessage(texto, mesagem);
    }

    public static void executarJavascript(String comando) {
        if (PrimeFaces.current() != null) {
            PrimeFaces.current().executeScript(comando);
        }
    }

    public static void verificaPermissao(String pagina, String nivel) {
        boolean ok = true;
        ok = nivel.equalsIgnoreCase("MASTER");
        if (!ok) {
            App.redirecionarPagina("restrito/index.cs");
        }
    }

 

    public static boolean verificaMenu(String pagina, String nivel) {
        return nivel.equalsIgnoreCase("MASTER");
    }

    public static Connection pegarconexao() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
                "jdbc:postgresql://" + App.host + ":" + App.porta + "/" + bd + "?charSet=UTF-8",
                App.userbd,
                App.passbd);
    }

    private SimpleDateFormat datahorasql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat datasql = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat sd = new SimpleDateFormat("ddMMyyyy-HHmm");
    private String pg = App.pgadmin;

    private SimpleDateFormat sdg = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat gg = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private SimpleDateFormat gg1 = new SimpleDateFormat("ddMMyyyy-HHmm");

    public static String pasta() {
        final String catalinaBase = System.getProperty("catalina.base");
        final File catalinaBaseContext = new File(catalinaBase, "webapps");
        File config = new File(catalinaBaseContext + File.separator + "betbot");
        if (!config.exists()) {
            config.mkdirs();
        }
        return config.getAbsolutePath();
    }

    public static void log(Exception ex) {
        String kk = "";
        try {
            SimpleDateFormat gg = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            SimpleDateFormat sdg = new SimpleDateFormat("dd-MM-yyyy");
            String stackTrace = ExceptionUtils.getStackTrace(ex);
            System.out.println(stackTrace);
            Logger.getGlobal().log(Level.SEVERE, stackTrace);
            String p = pasta() + File.separator + "logs";
            File ff = new File(p);
            if (!ff.exists()) {
                ff.mkdirs();
            }
            String filename = p + File.separator
                    + sdg.format(Calendar.getInstance().getTime()) + "_log.cs";

            File k = new File(filename);
            if (!k.exists()) {
                boolean cc = k.createNewFile();
                LOG.log(Level.INFO, "{0}", cc);
            }

            try (FileWriter fw = new FileWriter(filename, true) //the true will append the new data
                    ) {
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                kk = "";
                if (ex instanceof PSQLException) {
                    PSQLException e = (PSQLException) ex;
                    if (e.getServerErrorMessage() != null) {
                        kk += "\n" + e.getServerErrorMessage().toString();
                    }
                    kk += "\n" + e.getLocalizedMessage();
                    kk += "\n" + e.getMessage();
                }

                if (ex instanceof JDBCConnectionException) {
                    JDBCConnectionException e = (JDBCConnectionException) ex;
                    kk += "\n" + e.getSQL();
                    kk += "\n" + e.getSQLState();
                    kk += "\n" + e.getSQLException().toString();
                    kk += "\n" + e.getMessage();
                }

                if (ex instanceof ConstraintViolationException) {
                    ConstraintViolationException e = (ConstraintViolationException) ex;
                    kk += "\n" + e.getConstraintName();
                    kk += "\n" + e.getSQL();
                    kk += "\n" + e.getSQLState();
                    kk += "\n" + e.getSQLException().toString();
                    kk += "\n" + e.getMessage();
                }

                if (ex instanceof DataException) {
                    DataException e = (DataException) ex;
                    kk += "\n" + e.getSQL();
                    kk += "\n" + e.getSQLState();
                    kk += "\n" + e.getSQLException().toString();
                    kk += "\n" + e.getMessage();
                }
                if (ex instanceof SQLGrammarException) {
                    SQLGrammarException e = (SQLGrammarException) ex;
                    kk += "\n" + e.getSQL();
                    kk += "\n" + e.getSQLState();
                    kk += "\n" + e.getMessage();
                    kk += "\n" + e.getSQLException().getMessage();
                    kk += "\n" + e.getSQLException().getSQLState();
                }
                kk += errors.toString();
                fw.write(gg.format(Calendar.getInstance().getTime()) + ":" + kk + "\n");
//appends the string to the file
            }

        } catch (IOException e) {
            LOG.log(Level.INFO, "IOException: {0}", e.getMessage());
        }

    }

    private static String encryptionKey = "ABCDEFGHIJKLMNOP";
    //private static String characterEncoding = StandardCharsets.UTF_8;
    private static String cipherTransformation = "AES/CBC/PKCS5PADDING";
    private static String aesEncryptionAlgorithem = "AES";

    /**
     * Method for Encrypt Plain String Data
     *
     * @param plainText
     * @return encryptedText
     */
    public static String encrypt(String plainText) {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);

        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException E) {
            LOG.log(Level.INFO, "Encrypt Exception : {0}", E.getMessage());
        }
        return encryptedText;
    }

    /**
     * Method For Get encryptedText and Decrypted provided String
     *
     * @param encryptedText
     * @return decryptedText
     */
    public static String decrypt(String encryptedText) {
        String decryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] cipherText = decoder.decode(encryptedText.getBytes(StandardCharsets.UTF_8));
            decryptedText = new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);

        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException E) {
            LOG.log(Level.INFO, "decrypt Exception : {0}", E.getMessage());
        }
        return decryptedText;
    }

    public static void limparCache() {
        SessionFactory sf = HibernateUtil.sessionFactory;
        sf.getCache().evictCollectionData();
        sf.getCache().evictAllRegions();
        sf.getCache().evictDefaultQueryRegion();
        sf.getCache().evictEntityData();
        sf.getCache().evictNaturalIdData();
        sf.getCache().evictQueryRegions();

    }

    public static void log(String dados, String no, String pa) {
        try {
            String px = pasta() + File.separator + "logs";
            File ff = new File(px);
            if (!ff.exists()) {
                ff.mkdirs();
            }
            String p = pasta() + File.separator + "logs" + File.separator + pa;
            File pu = new File(p);
            if (!pu.exists()) {
                pu.mkdirs();
            }
            FileUtils.writeStringToFile(new File(p + File.separator + no), dados, StandardCharsets.UTF_8, true);
        } catch (Exception e) {
            log(e);
        }

    }

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Map<Long, Usuario> getUsuarios() {
        return usuarios;
    }

    public static void setUsuarios(Map<Long, Usuario> usuarios) {
        App.usuarios = usuarios;
    }

    public static String getUserbd() {
        return userbd;
    }

    public static void setUserbd(String userbd) {
        App.userbd = userbd;
    }

    public static String getPassbd() {
        return passbd;
    }

    public static void setPassbd(String passbd) {
        App.passbd = passbd;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        App.host = host;
    }

    public static String getBd() {
        return bd;
    }

    public static void setBd(String bd) {
        App.bd = bd;
    }

    public static String getPorta() {
        return porta;
    }

    public static void setPorta(String porta) {
        App.porta = porta;
    }

    public static String getPgadmin() {
        return pgadmin;
    }

    public static void setPgadmin(String pgadmin) {
        App.pgadmin = pgadmin;
    }

    public SimpleDateFormat getDatahorasql() {
        return datahorasql;
    }

    public void setDatahorasql(SimpleDateFormat datahorasql) {
        this.datahorasql = datahorasql;
    }

    public SimpleDateFormat getDatasql() {
        return datasql;
    }

    public void setDatasql(SimpleDateFormat datasql) {
        this.datasql = datasql;
    }

    public SimpleDateFormat getSd() {
        return sd;
    }

    public void setSd(SimpleDateFormat sd) {
        this.sd = sd;
    }

    public String getPg() {
        return pg;
    }

    public void setPg(String pg) {
        this.pg = pg;
    }

    public SimpleDateFormat getSdg() {
        return sdg;
    }

    public void setSdg(SimpleDateFormat sdg) {
        this.sdg = sdg;
    }

    public SimpleDateFormat getGg() {
        return gg;
    }

    public void setGg(SimpleDateFormat gg) {
        this.gg = gg;
    }

    public SimpleDateFormat getGg1() {
        return gg1;
    }

    public void setGg1(SimpleDateFormat gg1) {
        this.gg1 = gg1;
    }

    public static String getEncryptionKey() {
        return encryptionKey;
    }

    public static void setEncryptionKey(String encryptionKey) {
        App.encryptionKey = encryptionKey;
    }

    public static String getCipherTransformation() {
        return cipherTransformation;
    }

    public static void setCipherTransformation(String cipherTransformation) {
        App.cipherTransformation = cipherTransformation;
    }

    public static String getAesEncryptionAlgorithem() {
        return aesEncryptionAlgorithem;
    }

    public static void setAesEncryptionAlgorithem(String aesEncryptionAlgorithem) {
        App.aesEncryptionAlgorithem = aesEncryptionAlgorithem;
    }

    private static final Logger LOG = Logger.getLogger(App.class.getName());

}
