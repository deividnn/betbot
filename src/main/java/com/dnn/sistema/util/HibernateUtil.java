/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.util;

import com.dnn.sistema.entidades.Aposta;
import com.dnn.sistema.entidades.ApostaDet;
import com.dnn.sistema.entidades.Banca;
import com.dnn.sistema.entidades.Evento;
import com.dnn.sistema.entidades.Saque;
import com.dnn.sistema.entidades.Usuario;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

/**
 *
 * @author deivid
 */
public class HibernateUtil {

    private static StandardServiceRegistry registry;
    public static SessionFactory sessionFactory;
    public static boolean shiniciado;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {

                StandardServiceRegistryBuilder registryBuilder
                        = new StandardServiceRegistryBuilder();
                Map<String, Object> settings = new HashMap<>();
                settings.put(Environment.DRIVER, "org.postgresql.Driver");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
                settings.put(Environment.URL, "jdbc:postgresql://" + App.host + ":" + App.porta + "/" + App.bd + "");
                settings.put(Environment.USER, App.userbd);
                settings.put(Environment.PASS, App.passbd);
                settings.put("hibernate.current_session_context_class", "thread");
                settings.put(Environment.HBM2DDL_AUTO, "update");
                // settings.put(Environment.SHOW_SQL, true);
                settings.put("hibernate.temp.use_jdbc_metadata_defaults", false);
                //   settings.put("org.hibernate.envers.default_schema", "audit");
                // HikariCP settings
                // Maximum waiting time for a connection from the pool
                settings.put("hibernate.hikari.connectionTimeout", "20000");
                // Minimum number of ideal connections in the pool
                settings.put("hibernate.hikari.minimumIdle", "10");
                // Maximum number of actual connection in the pool
                settings.put("hibernate.hikari.maximumPoolSize", "20");
                // Maximum time that a connection is allowed to sit ideal in the pool
                settings.put("hibernate.hikari.idleTimeout", "300000");
                //cache
                settings.put("hibernate.cache.use_second_level_cache", "true");
                settings.put("hibernate.cache.use_query_cache", "true");
                settings.put("hibernate.cache.use_structured_entries", "true");
                settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
                settings.put("net.sf.ehcache.configurationResourceName", "/ehcache-config.xml");
                settings.put("hibernate.cache.ehcache.missing_cache_strategy", "create");
                settings.put("hibernate.cache.use_minimal_puts", "true");

                // Create registry
                registryBuilder.applySettings(settings);

                registry = registryBuilder.build();

                // Create MetadataSources
                MetadataSources sources = new MetadataSources(registry).
                        addPackage("com.dnn.sistema.entidades").
                        addAnnotatedClass(Usuario.class).
                        addAnnotatedClass(Banca.class).
                        addAnnotatedClass(Aposta.class).
                        addAnnotatedClass(ApostaDet.class).
                        addAnnotatedClass(Saque.class).
                        addAnnotatedClass(Evento.class);

                // Create Metadata
                Metadata metadata = sources.getMetadataBuilder().build();

                // Create SessionFactory
                sessionFactory = metadata.getSessionFactoryBuilder().build();

            } catch (Exception e) {
                App.log(e);

                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

}
