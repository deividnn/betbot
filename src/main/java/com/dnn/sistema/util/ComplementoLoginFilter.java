/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.sistema.util;

import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * complemento do loginfilter para autorizacao das paginas em sessoes ativas
 *
 * @author deivid
 */
public class ComplementoLoginFilter implements PhaseListener {
    
    private static final long serialVersionUID = -7607159318721947672L;

    // The phase where the listener is going to be called
    private final PhaseId phaseId = PhaseId.RENDER_RESPONSE;

    /**
     * verifica antes da requisicao via ajax se a url aponta para a pagina de
     * autenticacao
     *
     * @param event
     */
    @Override
    public void beforePhase(PhaseEvent event) {

        //verifica se foi criado uma arvore de componentes
        boolean arvoreDeComponentes = processViewTree(event.getFacesContext().getViewRoot());
        //url da requisicao
        String uri = ((HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest()).getRequestURI();
        //url da requisicao
        String url = ((HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest()).getRequestURL().toString();

        //sessao atual
        HttpSession sessao = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        
        String[] pi = uri.split("betbot");
        String[] pl = url.split("betbot");
        
        if (uri.equals("/betbot/acesso.cs") && !pi[1].equals(pl[1])) {
            App.redirecionarPagina("/acesso.cs");
        } else //verifica se o usuario eta logado
        if (sessao == null || !arvoreDeComponentes ) {
            PartialViewContext pvc = FacesContext.getCurrentInstance().getPartialViewContext();
            //verifica se foi feito uma requisica ajax
            if (pvc.isAjaxRequest()) {
                App.redirecionarPagina("/acesso.cs");
            }            
        }
    }
    
    @Override
    public void afterPhase(PhaseEvent event) {
    }
    
    @Override
    public PhaseId getPhaseId() {
        return phaseId;
    }

    /**
     * *
     * processa arvore de componentes do jsf
     *
     * @param component
     * @return
     */
    private boolean processViewTree(UIComponent component) {
        for (UIComponent child : component.getChildren()) {
            processViewTree(child);
            return true;
        }
        return false;
    }
    private static final Logger LOG = Logger.getLogger(ComplementoLoginFilter.class.getName());
    
}
