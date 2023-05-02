/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.repositories;

/**
 *
 * @author deivid
 */
import com.dnn.sistema.util.App;
import com.dnn.sistema.util.HibernateUtil;
import java.util.List;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

abstract class BasicRepository<ENTITY, ID> {

    Session sessao;
    Transaction tx;
    private final Class<ENTITY> classe;

    public BasicRepository() {
        this.classe = null;
        sessao = HibernateUtil.getSessionFactory().openSession();
    }

    public BasicRepository(Class<ENTITY> entityKlasse) {
        this.classe = entityKlasse;
        sessao = HibernateUtil.getSessionFactory().openSession();

    }

    public ENTITY porHQL(String hql) {
        return (ENTITY) this.sessao.createQuery(hql).setMaxResults(1).uniqueResult();
    }

    public List<ENTITY> listarHQL(String hql) {
        return this.sessao.createQuery(hql).list();
    }

    public List<ENTITY> listarHQL50(String hql) {
        return this.sessao.createQuery(hql).setMaxResults(50).list();
    }

    public Object objetoPorHql(String hql) {
        return this.sessao.createQuery(hql).setMaxResults(1).uniqueResult();
    }

    public ENTITY objetoPorHqlParam(String hql, List<Object> param) {
        Query q = this.sessao.createQuery(hql);
        for (int i = 0; i < param.size(); i++) {
            q.setParameter(i + 1, param.get(i));
        }
        return (ENTITY) q.setMaxResults(1).uniqueResult();
    }

    public List<ENTITY> listaobjetoPorHqlParam(String hql, List<Object> param) {
        Query q = this.sessao.createQuery(hql);
        for (int i = 0; i < param.size(); i++) {
            q.setParameter(i + 1, param.get(i));
        }
        return q.list();
    }

    public List<Object> listaObjetoPorHql(String hql) {
        return this.sessao.createQuery(hql).list();
    }

    public List<ENTITY> listarSQL(String sql) {
        return this.sessao.createNativeQuery(sql).list();
    }

    public Object objetoPorSql(String sql) {
        return this.sessao.createNativeQuery(sql).setMaxResults(1).uniqueResult();
    }

    public Object[] objetoPorSqlv(String sql) {
        return (Object[]) this.sessao.createNativeQuery(sql).setMaxResults(1).uniqueResult();
    }

    public void comitSql(String sql) {
        this.sessao.createNativeQuery(sql).executeUpdate();
    }

    public void comitSqli(String sql) {
        try {
            tx = this.sessao.beginTransaction();
            this.sessao.createNativeQuery(sql).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            rollback();
            App.log(e);
            if (FacesContext.getCurrentInstance() != null) {
                App.criarMensagemErro("entre em contato com administrador do sistema");
            }

        }
    }

    public List<Object> listaObjetoPorSql(String sql) {
        return this.sessao.createNativeQuery(sql).list();
    }

    public List<Object[]> listaObjetoPorSqlo(String sql) {
        return this.sessao.createNativeQuery(sql).list();
    }

    public ENTITY salvar(ENTITY entity) {
        try {
            tx = this.sessao.beginTransaction();
            this.sessao.save(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            rollback();
            App.log(e);
            if (FacesContext.getCurrentInstance() != null) {
                App.criarMensagemErro("entre em contato com administrador do sistema");
            }
            return null;
        }

    }

    public ENTITY salvara(ENTITY entity) {
        try {
            tx = this.sessao.beginTransaction();
            this.sessao.saveOrUpdate(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            rollback();
            App.log(e);
            if (FacesContext.getCurrentInstance() != null) {
                App.criarMensagemErro("entre em contato com administrador do sistema");
            }
            return null;
        }

    }

    public ENTITY atualizar(ENTITY entity) {
        try {
            tx = this.sessao.beginTransaction();
            this.sessao.saveOrUpdate(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            rollback();
            App.log(e);
            if (FacesContext.getCurrentInstance() != null) {
                App.criarMensagemErro("entre em contato com administrador do sistema");
            }
            return null;
        }

    }

    public boolean excluir(ENTITY entity) {
        try {
            tx = this.sessao.beginTransaction();
            this.sessao.remove(this.sessao.merge(entity));
            tx.commit();
            return true;
        } catch (Exception e) {
            rollback();
            App.log(e);
            if (FacesContext.getCurrentInstance() != null) {
                App.criarMensagemErro("registro vinculado a outro registro:" + e);
            }
            return false;
        }
    }

    public void rollback() {
        if (tx != null) {
            tx.rollback();
        }
    }

}
