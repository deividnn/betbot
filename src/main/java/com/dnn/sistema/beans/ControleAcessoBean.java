/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.beans;

import com.dnn.sistema.util.App;
import static com.dnn.sistema.util.App.pasta;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author deivi
 */
public class ControleAcessoBean implements Serializable {

    public static List<Sessao> sessoes = new ArrayList<>();
    public static boolean verificando = false;

    public static class Sessao implements Serializable {

        private int id;
        private HttpSession sessao;
        private long usuario;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public HttpSession getSessao() {
            return sessao;
        }

        public void setSessao(HttpSession sessao) {
            this.sessao = sessao;
        }

        public long getUsuario() {
            return usuario;
        }

        public void setUsuario(long usuario) {
            this.usuario = usuario;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + this.id;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else {
                return getClass() == obj.getClass();
            }
        }

    }

    public static class Log implements Runnable {

        @Override
        public void run() {

            while (verificando) {
                Connection con = null;
                

                try {
                    String p = pasta() + File.separator + "logs";
                    File pastax = new File(p);
                    if (pastax.exists()) {
                        for (File ff : pastax.listFiles()) {
                            if (ff.isFile()) {
                                limparlog(ff.getAbsolutePath());
                            }
                        }
                    }
//
                    String catalinaBase = System.getProperty("catalina.base");
                    File catalinaBaseContext = new File(catalinaBase, "logs");
                    if (catalinaBaseContext.exists()) {
                        for (File ff : catalinaBaseContext.listFiles()) {
                            if (ff.isFile()) {
                                limparlog(ff.getAbsolutePath());
                            }
                        }
                    }
                    Thread.sleep(30000);

                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    App.log(e);
                }

            }
        }

        private void limparlog(String filename) throws IOException {
            File log1 = new File(filename);
            if (log1.exists()) {
                long bytes = log1.length();
                long kilobytes = (bytes / 1024);
                long megabytes = (kilobytes / 1024);

                long d = days2(new Date(log1.lastModified()), new Date());
                if (d <= 7) {
                    if (megabytes > 10) {
                        FileUtils.writeStringToFile(log1, "", StandardCharsets.UTF_8);
                    }
                } else {
                    FileUtils.deleteQuietly(log1);
                }
            }
        }

    }

    public static long days2(Date start, Date end) {
        //Ignore argument check

        Calendar c1 = Calendar.getInstance();
        c1.setTime(start);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(end);

        //end Saturday to start Saturday 
        return (c2.getTimeInMillis() - c1.getTimeInMillis()) / (1000 * 60 * 60 * 24);

    }

    public static class T implements Runnable {

        @Override
        public void run() {
            while (verificando) {
                try {
                    for (Iterator<Sessao> iterator = sessoes.iterator(); iterator.hasNext();) {
                        Sessao next = iterator.next();
                        try {
                            LocalDateTime start = LocalDateTime.now();
                            LocalDateTime end = App.convertToLocalDateTime(new Date(next.getSessao().getLastAccessedTime()));
                            Duration duration = Duration.between(start, end);
                            if (duration.abs().getSeconds() > 30) {
                                next.getSessao().invalidate();
                                iterator.remove();
                            }
                        } catch (Exception e) {
                            App.log(e);
                            iterator.remove();
                        }

                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    App.log(e);
                }

            }
        }

    }

}
