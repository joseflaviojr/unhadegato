
/*
 *  Copyright (C) 2016-2018 José Flávio de Souza Dias Júnior
 *
 *  This file is part of Unha-de-gato - <http://joseflavio.com/unhadegato/>.
 *
 *  Unha-de-gato is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Unha-de-gato is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Unha-de-gato. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  Direitos Autorais Reservados (C) 2016-2018 José Flávio de Souza Dias Júnior
 *
 *  Este arquivo é parte de Unha-de-gato - <http://joseflavio.com/unhadegato/>.
 *
 *  Unha-de-gato é software livre: você pode redistribuí-lo e/ou modificá-lo
 *  sob os termos da Licença Pública Menos Geral GNU conforme publicada pela
 *  Free Software Foundation, tanto a versão 3 da Licença, como
 *  (a seu critério) qualquer versão posterior.
 *
 *  Unha-de-gato é distribuído na expectativa de que seja útil,
 *  porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 *  COMERCIABILIDADE ou ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a
 *  Licença Pública Menos Geral do GNU para mais detalhes.
 *
 *  Você deve ter recebido uma cópia da Licença Pública Menos Geral do GNU
 *  junto com Unha-de-gato. Se não, veja <http://www.gnu.org/licenses/>.
 */

package com.joseflavio.unhadegato;

import com.joseflavio.copaiba.Copaiba;
import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.urucum.comunicacao.Consumidor;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Gerenciador de {@link CopaibaConexao conexões} a uma {@link Copaiba}.
 * @author José Flávio de Souza Dias Júnior
 * @see CopaibaComunicador
 */
class CopaibaGerenciador {
    
    private String nome;
    
    private String endereco;
    
    private int porta;
    
    private boolean segura;
    
    private boolean ignorarCertificado;
    
    private String usuario;
    
    private String senha;
    
    private int totalConexoes;
    
    private List<CopaibaComunicador> comunicadores = new LinkedList<>();
    
    private Queue<Consumidor> consumidores = new ConcurrentLinkedDeque<>();
    
    public CopaibaGerenciador( String nome, String endereco, int porta, boolean segura, boolean ignorarCertificado, String usuario, String senha, int totalConexoes ) {
        this.nome = nome;
        this.endereco = endereco;
        this.porta = porta;
        this.segura = segura;
        this.ignorarCertificado = ignorarCertificado;
        this.usuario = usuario;
        this.senha = senha;
        this.totalConexoes = totalConexoes;
    }
    
    @Override
    public boolean equals( Object obj ) {
        CopaibaGerenciador g = (CopaibaGerenciador) obj;
        if( ! nome.equals( g.nome ) ) return false;
        if( ! endereco.equals( g.endereco ) ) return false;
        if( porta != g.porta ) return false;
        if( segura != g.segura ) return false;
        if( ignorarCertificado != g.ignorarCertificado ) return false;
        if( ! usuario.equals( g.usuario ) ) return false;
        if( ! senha.equals( g.senha ) ) return false;
        if( totalConexoes != g.totalConexoes ) return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        return nome.hashCode();
    }
    
    public void iniciar() {
        for( int i = 0; i < totalConexoes; i++ ){
            CopaibaComunicador comunicador = new CopaibaComunicador( this );
            comunicadores.add( comunicador );
            comunicador.start();
        }
    }
    
    public void parar() {
        for( CopaibaComunicador comunicador : comunicadores ){
            comunicador.interrupt();
        }
        comunicadores.clear();
    }
    
    public void encerrar() {
        parar();
        for( Consumidor consumidor : consumidores ){
            try{
                consumidor.fechar();
            }catch( Exception e ){
            }
        }
        consumidores.clear();
    }
    
    public boolean atualizar( String endereco, int porta, boolean segura, boolean ignorarCertificado, String usuario, String senha, int totalConexoes ) {
    
        boolean igual = true;
    
        if( ! nome.equals( this.nome ) ) igual = false;
        else if( ! endereco.equals( this.endereco ) ) igual = false;
        else if( porta != this.porta ) igual = false;
        else if( segura != this.segura ) igual = false;
        else if( ignorarCertificado != this.ignorarCertificado ) igual = false;
        else if( ! usuario.equals( this.usuario ) ) igual = false;
        else if( ! senha.equals( this.senha ) ) igual = false;
        else if( totalConexoes != this.totalConexoes ) igual = false;
        
        if( ! igual ){
    
            parar();
    
            this.endereco = endereco;
            this.porta = porta;
            this.segura = segura;
            this.ignorarCertificado = ignorarCertificado;
            this.usuario = usuario;
            this.senha = senha;
            this.totalConexoes = totalConexoes;
    
            iniciar();
            
            return true;
            
        }
        
        return false;
        
    }
    
    /**
     * Obtém o primeiro {@link Consumidor} da fila de atendimento.
     * @return null, se fila vazia.
     */
    public Consumidor obterConsumidor() {
        return consumidores.poll();
    }
    
    /**
     * Insere um {@link Consumidor} na fila de atendimento.
     */
    public void inserirConsumidor( Consumidor consumidor ) {
        consumidores.offer( consumidor );
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getEndereco() {
        return endereco;
    }
    
    public int getPorta() {
        return porta;
    }
    
    public boolean isSegura() {
        return segura;
    }
    
    public boolean isIgnorarCertificado() {
        return ignorarCertificado;
    }
    
    public String getUsuario() {
        return usuario;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public int getTotalConexoes() {
        return totalConexoes;
    }
    
}
