
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
import com.joseflavio.urucum.comunicacao.Servidor;
import com.joseflavio.urucum.comunicacao.SocketServidor;
import com.joseflavio.urucum.texto.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Concentrador de {@link Copaiba}s.
 * @author José Flávio de Souza Dias Júnior
 */
public class Concentrador {
    
    private static File configuracao;
    
    private static Map<String, CopaibaGerenciador> gerenciadores = new HashMap<>();
    
    private static final Logger log = LogManager.getLogger( Concentrador.class.getPackage().getName() );
    
    /**
     * {@link CopaibaGerenciador#iniciar() Iniciar}, {@link CopaibaGerenciador#atualizar(String, int, boolean, boolean, String, String, int) atualizar}
     * e/ou {@link CopaibaGerenciador#encerrar() encerrar} {@link CopaibaGerenciador}'s.
     * @param arquivo Arquivo de configuração de {@link CopaibaConexao}'s.
     */
    private static void executarCopaibas( File arquivo ) {
    
        try{
            
            if( ! arquivo.exists() ){
                try(
                    InputStream  is = Concentrador.class.getResourceAsStream( "/copaibas.conf" );
                    OutputStream os = new FileOutputStream( arquivo );
                ){
                    IOUtils.copy( is, os );
                }
            }
    
            Properties props = new Properties();
            
            try( FileInputStream fis = new FileInputStream( arquivo ) ){
                props.load( fis );
            }
            
            for( Object chave : props.keySet() ){
    
                try{
                    
                    String nome = chave.toString();
        
                    String[] p = props.getProperty( nome ).split( "\",\"" );
        
                    String  endereco    = p[0].substring( 1 );
                    int     porta       = Integer.parseInt( p[1] );
                    boolean segura      = p[2].equals( "TLS" ) || p[2].equals( "SSL" );
                    boolean ignorarCert = p[3].equals( "S" );
                    String  usuario     = p[4];
                    String  senha       = p.length >= 7 ? p[5] : p[5].substring( 0, p[5].length() - 1 );
                    int     conexoes    = p.length >= 7 ? Integer.parseInt( p[6].substring( 0, p[6].length() - 1 ) ) : 5;
        
                    CopaibaGerenciador gerenciador = gerenciadores.get( nome );
        
                    if( gerenciador == null ){
                        log.info( Util.getMensagem( "copaiba.iniciando", nome ) );
                        gerenciador = new CopaibaGerenciador( nome, endereco, porta, segura, ignorarCert, usuario, senha, conexoes );
                        gerenciadores.put( nome, gerenciador );
                        gerenciador.iniciar();
                        log.info( Util.getMensagem( "copaiba.iniciada", nome ) );
                    }else{
                        log.info( Util.getMensagem( "copaiba.verificando", nome ) );
                        if( gerenciador.atualizar( endereco, porta, segura, ignorarCert, usuario, senha, conexoes ) ){
                            log.info( Util.getMensagem( "copaiba.atualizada", nome ) );
                        }else{
                            log.info( Util.getMensagem( "copaiba.inalterada", nome ) );
                        }
                    }
    
                    try( CopaibaConexao cc = new CopaibaConexao( endereco, porta, segura, ignorarCert, usuario, senha ) ){
                        cc.verificar();
                        log.info( Util.getMensagem( "copaiba.conexao.teste.exito", nome ) );
                    }catch( Exception e ){
                        log.info( Util.getMensagem( "copaiba.conexao.teste.erro", nome, e.getMessage() ) );
                        log.error( e.getMessage(), e );
                    }
                    
                }catch( Exception e ){
                    log.error( e.getMessage(), e );
                }
    
            }
    
            Iterator<CopaibaGerenciador> it = gerenciadores.values().iterator();
            while( it.hasNext() ){
                CopaibaGerenciador gerenciador = it.next();
                String nome = gerenciador.getNome();
                if( ! props.containsKey( nome ) ){
                    try{
                        log.info( Util.getMensagem( "copaiba.encerrando", nome ) );
                        it.remove();
                        gerenciador.encerrar();
                        log.info( Util.getMensagem( "copaiba.encerrada", nome ) );
                    }catch( Exception e ){
                        log.error( e.getMessage(), e );
                    }
                }
            }
            
        }catch( Exception e ){
            log.error( e.getMessage(), e );
        }
    
    }
    
    private static class Portal extends Thread {
        
        private Servidor servidor;
    
        public Portal( Servidor servidor ) {
            this.servidor = servidor;
        }
    
        @Override
        public void run() {
    
            Consumidor consumidor = null;

            int  mem_passo  = 0;
            long mem_maxima = Runtime.getRuntime().maxMemory();
            long mem_alocada, mem_livre;
    
            while( true ){
    
                try{

                    if( ++mem_passo == 100 ){
                        mem_passo  = 0;
                        mem_alocada = Runtime.getRuntime().totalMemory();
                        mem_livre   = Runtime.getRuntime().freeMemory();
                        while( ( mem_alocada / (double) mem_maxima ) > 0.9d && ( mem_livre / (double) mem_alocada ) < 0.1d ){
                            log.warn( Util.getMensagem( "sistema.memoria.pouca", mem_livre, mem_alocada ) );
                            System.gc();
                            Thread.sleep( 10000 );
                        }
                    }
                    
                    consumidor = servidor.aceitar();
    
                    consumidor.setTempoEspera( 600 );
    
                    String nome = Util.receberString( consumidor.getInputStream() );
                    String resultado = null;
                    
                    if( nome.equals( "##Unha-de-gato.VERSAO" ) ){
                        
                        resultado = UnhaDeGato.VERSAO;
                        
                    }else{
    
                        CopaibaGerenciador gerenciador = gerenciadores.get( nome );

                        if( gerenciador != null ){
                            gerenciador.inserirConsumidor( consumidor );
                        }else{
                            resultado = "##Unha-de-gato.ERRO@" + IllegalArgumentException.class.getName() + "@" + Util.getMensagem( "copaiba.desconhecida", nome );
                        }
                        
                    }
                    
                    if( resultado != null ){
    
                        Util.enviarTexto( consumidor.getOutputStream(), resultado );
    
                        try{
                            consumidor.getInputStream().read();
                        }catch( Exception e ){
                        }finally{
                            Util.fechar( consumidor );
                            consumidor = null;
                        }
                        
                    }
                    
                }catch( Exception e ){
                    
                    if( consumidor != null ){
                        Util.fechar( consumidor );
                        consumidor = null;
                    }
                    
                    if( e instanceof InterruptedException ) return;
                    
                }
        
            }
            
        }
        
    }
    
    /**
     * @param args [0] = Diretório de configurações.
     */
    public static void main( String[] args ) {
        
        log.info( Util.getMensagem( "unhadegato.iniciando" ) );
        
        try{
            
            /***********************/
            
            if( args.length > 0 ){
                if( ! args[0].isEmpty() ){
                    configuracao = new File( args[0] );
                    if( ! configuracao.isDirectory() ){
                        String msg = Util.getMensagem( "unhadegato.diretorio.incorreto" );
                        System.out.println( msg );
                        log.error( msg );
                        System.exit( 1 );
                    }
                }
            }
            
            if( configuracao == null ){
                configuracao = new File( System.getProperty( "user.home" ) + File.separator + "unhadegato" );
                configuracao.mkdirs();
            }
    
            log.info( Util.getMensagem( "unhadegato.diretorio.endereco", configuracao.getAbsolutePath() ) );
            
            /***********************/
    
            File confGeralArq = new File( configuracao, "unhadegato.conf" );
            
            if( ! confGeralArq.exists() ){
                try(
                    InputStream  is = Concentrador.class.getResourceAsStream( "/unhadegato.conf" );
                    OutputStream os = new FileOutputStream( confGeralArq );
                ){
                    IOUtils.copy( is, os );
                }
            }
    
            Properties confGeral = new Properties();
            
            try( FileInputStream fis = new FileInputStream( confGeralArq ) ){
                confGeral.load( fis );
            }
            
            String prop_porta         = confGeral.getProperty( "porta" );
            String prop_porta_segura  = confGeral.getProperty( "porta.segura" );
            String prop_seg_pri       = confGeral.getProperty( "seguranca.privada" );
            String prop_seg_pri_senha = confGeral.getProperty( "seguranca.privada.senha" );
            String prop_seg_pri_tipo  = confGeral.getProperty( "seguranca.privada.tipo" );
            String prop_seg_pub       = confGeral.getProperty( "seguranca.publica" );
            String prop_seg_pub_senha = confGeral.getProperty( "seguranca.publica.senha" );
            String prop_seg_pub_tipo  = confGeral.getProperty( "seguranca.publica.tipo" );
            
            if( StringUtil.tamanho( prop_porta         ) == 0 ) prop_porta         = "8885";
            if( StringUtil.tamanho( prop_porta_segura  ) == 0 ) prop_porta_segura  = "8886";
            if( StringUtil.tamanho( prop_seg_pri       ) == 0 ) prop_seg_pri       = "servidor.jks";
            if( StringUtil.tamanho( prop_seg_pri_senha ) == 0 ) prop_seg_pri_senha = "123456";
            if( StringUtil.tamanho( prop_seg_pri_tipo  ) == 0 ) prop_seg_pri_tipo  = "JKS";
            if( StringUtil.tamanho( prop_seg_pub       ) == 0 ) prop_seg_pub       = "cliente.jks";
            if( StringUtil.tamanho( prop_seg_pub_senha ) == 0 ) prop_seg_pub_senha = "123456";
            if( StringUtil.tamanho( prop_seg_pub_tipo  ) == 0 ) prop_seg_pub_tipo  = "JKS";
    
            /***********************/
    
            File seg_pri = new File( prop_seg_pri );
            if( ! seg_pri.isAbsolute() ) seg_pri = new File( configuracao.getAbsolutePath() + File.separator + prop_seg_pri );
    
            if( seg_pri.exists() ){
                System.setProperty( "javax.net.ssl.keyStore",         seg_pri.getAbsolutePath() );
                System.setProperty( "javax.net.ssl.keyStorePassword", prop_seg_pri_senha );
                System.setProperty( "javax.net.ssl.keyStoreType",     prop_seg_pri_tipo );
            }
            
            File seg_pub = new File( prop_seg_pub );
            if( ! seg_pub.isAbsolute() ) seg_pub = new File( configuracao.getAbsolutePath() + File.separator + prop_seg_pub );
    
            if( seg_pub.exists() ){
                System.setProperty( "javax.net.ssl.trustStore",         seg_pub.getAbsolutePath() );
                System.setProperty( "javax.net.ssl.trustStorePassword", prop_seg_pub_senha );
                System.setProperty( "javax.net.ssl.trustStoreType",     prop_seg_pub_tipo );
            }
    
            /***********************/
            
            new Thread() {
    
                File arquivo = new File( configuracao, "copaibas.conf" );
                
                long ultimaData = -1;
                
                @Override
                public void run() {
                    
                    while( true ){
    
                        long data = arquivo.lastModified();
    
                        if( data > ultimaData ){
                            executarCopaibas( arquivo );
                            ultimaData = data;
                        }
    
                        try{
                            Thread.sleep( 5 * 1000 );
                        }catch( InterruptedException e ){
                            return;
                        }
    
                    }
    
                }
                
            }.start();
            
            /***********************/
    
            log.info( Util.getMensagem( "unhadegato.conexao.esperando" ) );
    
            log.info( Util.getMensagem( "copaiba.porta.normal.abrindo", prop_porta ) );
            Portal portal1 = new Portal( new SocketServidor( Integer.parseInt( prop_porta ), false, true ) );
    
            log.info( Util.getMensagem( "copaiba.porta.segura.abrindo", prop_porta_segura ) );
            Portal portal2 = new Portal( new SocketServidor( Integer.parseInt( prop_porta_segura ), true, true ) );
            
            portal1.start();
            portal2.start();
            
            portal1.join();
            
            /***********************/
            
        }catch( Exception e ){
            
            log.error( e.getMessage(), e );
            
        }finally{
    
            for( CopaibaGerenciador gerenciador : gerenciadores.values() ) gerenciador.encerrar();
            gerenciadores.clear();
            gerenciadores = null;
            
        }
        
    }
    
}
