
/*
 *  Copyright (C) 2016 José Flávio de Souza Dias Júnior
 *  
 *  This file is part of Unha-de-gato - <http://www.joseflavio.com/unhadegato/>.
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
 *  Direitos Autorais Reservados (C) 2016 José Flávio de Souza Dias Júnior
 * 
 *  Este arquivo é parte de Unha-de-gato - <http://www.joseflavio.com/unhadegato/>.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.joseflavio.copaiba.Copaiba;
import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.copaiba.CopaibaException;

/**
 * Concentrador de {@link Copaiba}s.
 * @author José Flávio de Souza Dias Júnior
 */
public class Concentrador {
    
    private static File configuracao;
    
    private static List<Processador> processadores = Collections.synchronizedList( new ArrayList<Processador>() );
    
    private static long ultimaDataConfGeral;
    private static long ultimaDataConfCopaibas;
    
    private static long intervalo = 50;
    
    private static final Logger log = LogManager.getLogger( Concentrador.class.getPackage().getName() );
    
    /**
     * @see CopaibaConexao#CopaibaConexao(String, int, boolean, boolean, String, String)
     */
    private static class Conexao {
        
        private String endereco;
        private int porta;
        private boolean segura;
        private boolean ignorarCertificado;
        private String usuario;
        private String senha;
        
        public Conexao( String conexao ) {
            try{
                String[] p = conexao.split( "\",\"" );
                this.endereco = p[0].substring( 1 );
                this.porta = Integer.parseInt( p[1] );
                this.segura = p[2].equals( "TLS" ) || p[2].equals( "SSL" );
                this.ignorarCertificado = p[3].equals( "S" );
                this.usuario = p[4];
                this.senha = p[5].substring( 0, p[5].length() - 1 );
            }catch( Exception e ){
                throw new IllegalArgumentException( "Formato incorreto: " + conexao );
            }
        }
        
        @Override
        public boolean equals( Object obj ) {
            Conexao c = (Conexao) obj;
            if( ! endereco.equals( c.endereco ) ) return false;
            if( porta != c.porta ) return false;
            if( segura != c.segura ) return false;
            if( ignorarCertificado != c.ignorarCertificado ) return false;
            if( ! usuario.equals( c.usuario ) ) return false;
            if( ! senha.equals( c.senha ) ) return false;
            return true;
        }
        
        @Override
        public int hashCode() {
            return endereco.hashCode() + porta;
        }
        
        public CopaibaConexao conectar() throws CopaibaException {
            return new CopaibaConexao( endereco, porta, segura, ignorarCertificado, usuario, senha );
        }
        
    }

    private static class Processador extends Thread {

        private Context contexto;
        private ZMQ.Socket cliente;
        private Map<String,Conexao> conexoes;
        private Map<String,CopaibaConexao> copaibas;
        private Map<String,Conexao> atualizacao;

        private Processador( Context contexto, Map<String,Conexao> conexoes ) {
            this.contexto = contexto;
            this.conexoes = conexoes;
            this.copaibas = new HashMap<>();
        }

        @Override
        public void run() {
            
            try{
                cliente = contexto.socket( ZMQ.REP );
                cliente.connect( "inproc://unhadegato" );
                cliente.setReceiveTimeOut( 0 );
            }catch( Exception e ){
                log.error( e.getMessage(), e );
            }
            
            while( ! isInterrupted() ){
                
                if( atualizacao != null ){
                    fecharCopaibas();
                    conexoes = atualizacao;
                    atualizacao = null;
                }
                
                byte[] mensagem = cliente.recv( 0 );
                
                if( mensagem == null ){
                    try{
                        Thread.sleep( intervalo );
                        continue;
                    }catch( InterruptedException e ){
                        break;
                    }
                }
                
                try{
                    
                    if( mensagem.length <= 2 ){
                        enviarErro( new IOException( "Mensagem incompleta." ), cliente );
                        continue;
                    }
                    
                    int instrucao = mensagem[0];
                    int total     = mensagem[1];
                    
                    if( instrucao < 1 || instrucao > 6 ){
                        enviarErro( new IOException( "Instrução desconhecida: " + instrucao ), cliente );
                        continue;
                    }
                    
                    String conteudo = new String( mensagem, 2, mensagem.length - 2, "UTF-8" );
                    String[] param  = new String[total];
                    
                    int pos1 = 0;
                    int pos2 = conteudo.indexOf( '\b' );
                    for( int i = 0; i < total; i++ ){
                        param[i] = conteudo.substring( pos1, pos2 );
                        pos1 = pos2 + 1;
                        pos2 = conteudo.indexOf( '\b', pos1 );
                    }
                    
                    String copaibaNome = param[0];
                    CopaibaConexao copaiba = getCopaiba( copaibaNome );
                    String retorno = null;
                    
                    try{
						retorno = processar( copaiba, instrucao, param );
					}catch( Exception e ){
						if( isIOException( e ) ){
							try{
								copaiba.verificar();
								throw e;
							}catch( Exception f ){
								fecharCopaiba( copaibaNome );
								copaiba = getCopaiba( copaibaNome );
								retorno = processar( copaiba, instrucao, param );
							}
						}else{
							throw e;
						}
					}
                    
                    if( retorno == null ) retorno = "";
                    cliente.send( retorno.getBytes( "UTF-8" ), 0 );
                    
                }catch( Throwable e ){
                    enviarErro( e, cliente );
                    continue;
                }
                
            }
            
            processadores.remove( this );
            fecharCopaibas();
            
            try{
                cliente.close();
            }catch( Exception f ){
            }
            
            contexto    = null;
            cliente     = null;
            conexoes    = null;
            copaibas    = null;
            atualizacao = null;
            
        }
        
        private String processar( CopaibaConexao copaiba, int instrucao, String[] param ) throws CopaibaException {
			switch( instrucao ){
			    case 1 :
			        return (String) copaiba.executar( param[1], param[2], null, true );
			    case 2 :
			        copaiba.atribuir( param[1], param[2], param[3] );
			        return null;
			    case 3 :
			        return (String) copaiba.obter( param[1], true );
			    case 4 :
			        return (String) copaiba.obter( param[1], param[2], true );
			    case 5 :
			        copaiba.remover( param[1] );
			        return null;
			    case 6 :
			        return copaiba.solicitar( param[1], param[2], param[3] );
			    default :
			    		return null;
			}
        }
        
        private CopaibaConexao getCopaiba( String nome ) throws IOException, CopaibaException {
        		CopaibaConexao copaiba = copaibas.get( nome );
        		if( copaiba == null || ! copaiba.isAberta() ){
                Conexao conexao = conexoes.get( nome );
                if( conexao == null ) throw new IOException( "Copaíba desconhecida: " + nome );
                copaiba = conexao.conectar();
                copaibas.put( nome, copaiba );
            }
        		return copaiba;
        }
        
        private void fecharCopaiba( String nome ) {
        		try{
        			CopaibaConexao copaiba = copaibas.get( nome );
        			if( copaiba == null ) return;
                copaiba.fechar( true );
            }catch( Exception e ){
            }finally{
            		copaibas.remove( nome );
            }
        }
        
        private void fecharCopaibas() {
            if( copaibas == null ) return;
            for( CopaibaConexao copaiba : copaibas.values() ){
                try{
                    copaiba.fechar( true );
                }catch( Exception e ){
                }
            }
            copaibas.clear();
        }
        
        private boolean isIOException( Throwable e ) {
        		if( e == null ) return false;
        		return e instanceof IOException ? true : isIOException( e.getCause() );
        }
        
    }
    
    private static void enviarErro( Throwable erro, ZMQ.Socket cliente ) {
        String mensagem = "Unha-de-gato.ERRO@" + erro.getClass().getName() + "@" + erro.getMessage();
        byte[] bytes = null;
        try{
            bytes = mensagem.getBytes( "UTF-8" );
        }catch( UnsupportedEncodingException e ){
            bytes = mensagem.getBytes();
        }
        cliente.send( bytes, 0 );
    }
    
    private static Properties carregarConfGeral() throws IOException {

        File arquivo = new File( configuracao, "unhadegato.conf" );
        Properties prop = new Properties();
        
        if( arquivo.exists() ){
            try( FileInputStream fis = new FileInputStream( arquivo ) ){
                prop.load( fis );
            }
        }else{
            prop.setProperty( "porta", "8885" );
            prop.setProperty( "processos", "5" );
            prop.setProperty( "intervalo", "50" );
            try( FileOutputStream fos = new FileOutputStream( arquivo ) ){
                prop.store( fos, "Unha-de-gato" );
            }
        }
        
        return prop;
        
    }
    
    private static void verificarConfGeral() throws IOException {
        
        long data = new File( configuracao, "unhadegato.conf" ).lastModified();
        
        if( data > ultimaDataConfGeral ){
            
            log.info( "Atualizando a configuração geral." );
            
            Properties conf = carregarConfGeral();
            
            intervalo = Integer.parseInt( conf.getProperty( "intervalo" ) );
            log.info( "Intervalo de espera = " + intervalo + " ms" );
            
            int conf_processos = Integer.parseInt( conf.getProperty( "processos" ) );
            if( conf_processos > 0 ){
                
                int diferenca = processadores.size() - conf_processos;
                
                if( diferenca > 0 ){
                    
                    log.info( "Reduzindo a quantidade de processos. Total = " + conf_processos );
                    
                    Processador[] remocao = new Processador[diferenca];
                    
                    for( int i = 0; i < diferenca; i++ ){
                        remocao[i] = processadores.get( i );
                    }
                    
                    for( Processador p : remocao ){
                        p.interrupt();
                    }
                    
                    remocao = null;
                    
                }else if( diferenca < 0 ){
                    
                    log.info( "Aumentando a quantidade de processos. Total = " + conf_processos );
                    
                    diferenca = - diferenca;
                    Context contexto = processadores.get( 0 ).contexto;
                    Map<String,Conexao> conexoes = carregarConfCopaibas();
                    
                    for( int i = 0; i < diferenca; i++ ){
                        Processador p = new Processador( contexto, conexoes );
                        processadores.add( p );
                        p.start();
                    }
                    
                }
                
            }

            ultimaDataConfGeral = data;
            
        }
        
    }
    
    private static Map<String,Conexao> carregarConfCopaibas() throws IOException {

        File arquivo = new File( configuracao, "copaibas.conf" );
        Properties prop = new Properties();
        
        if( arquivo.exists() ){
            try( FileInputStream fis = new FileInputStream( arquivo ) ){
                prop.load( fis );
            }
        }else{
            try( FileWriter fw = new FileWriter( arquivo ) ){
                fw.write( "# Copaibas\n\n" );
                fw.write( "# nome=\"endereço\",\"porta\",\"segurança: TLS, SSL ou vazio\",\"ignorar certificado: S ou N\",\"usuario\",\"senha\"\n" );
                fw.write( "# exemplo1=\"localhost\",\"8884\",\"\",\"N\",\"jose\",\"12345678\"\n" );
                fw.write( "# exemplo2=\"127.0.0.1\",\"8884\",\"TLS\",\"S\",\"maria\",\"12345678\"\n\n" );
                fw.write( "copaiba=\"localhost\",\"8884\",\"\",\"N\",\"jose\",\"12345678\"\n" );
            }
        }
        
        Map<String,Conexao> conexoes = new HashMap<>();
        for( Object nome : prop.keySet() ){
            conexoes.put( nome.toString(), new Conexao( prop.get( nome ).toString() ) );
        }
        return conexoes;
        
    }
    
    private static void verificarConfCopaibas() throws IOException {
        
        long data = new File( configuracao, "copaibas.conf" ).lastModified();
        
        if( data > ultimaDataConfCopaibas ){
            
            log.info( "Atualizando as Copaíbas." );
            
            Map<String,Conexao> novas = carregarConfCopaibas();
            for( Processador p : processadores ){
                p.atualizacao = novas;
            }
            
            ultimaDataConfCopaibas = data;
            
        }
        
    }
    
    /**
     * @param args Diretório de configurações.
     */
    public static void main( String[] args ) {
        
        log.info( "Iniciando." );
        
        Context contexto     = null;
        Socket  roteador     = null;
        Socket  distribuidor = null;
        
        try{
            
            /***********************/
            
            if( args.length > 0 ){
                if( ! args[0].isEmpty() ){
                    configuracao = new File( args[0] );
                    if( ! configuracao.isDirectory() ){
                        String msg = "Informar corretamente o diretório de configurações.";
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
            
            /***********************/
            
            ultimaDataConfGeral    = new File( configuracao, "unhadegato.conf" ).lastModified();
            ultimaDataConfCopaibas = new File( configuracao, "copaibas.conf" ).lastModified();
            
            /***********************/
            
            Properties conf = carregarConfGeral();
            int conf_porta = Integer.parseInt( conf.getProperty( "porta" ) );
            int conf_processos = Integer.parseInt( conf.getProperty( "processos" ) );
            
            /***********************/
            
            contexto = ZMQ.context( 1 );

            log.info( "Iniciando roteador - tcp://*:" + conf_porta );
            roteador = contexto.socket( ZMQ.ROUTER );
            roteador.bind( "tcp://*:" + conf_porta );

            log.info( "Iniciando distribuidor - inproc://unhadegato" );
            distribuidor = contexto.socket( ZMQ.DEALER );
            distribuidor.bind( "inproc://unhadegato" );
            
            /***********************/

            log.info( "Iniciando processos. Total = " + conf_processos );
            
            Map<String,Conexao> conexoes = carregarConfCopaibas();
            
            for( int i = 0; i < conf_processos; i++ ){
                Processador p = new Processador( contexto, conexoes );
                processadores.add( p );
                p.start();
            }
            
            /***********************/
            
            new ScheduledThreadPoolExecutor( 1 ).scheduleWithFixedDelay( new Runnable() {
                @Override
                public void run() {
                    try{
                        verificarConfCopaibas();
                        verificarConfGeral();
                    }catch( Exception e ){
                        log.error( e.getMessage(), e );
                    }
                }
            }, 30, 30, TimeUnit.SECONDS );
            
            /***********************/
            
            log.info( "Esperando conexões." );
            log.info( "Intervalo de espera = " + intervalo + " ms" );
            
            ZMQ.proxy( roteador, distribuidor, null );
            
            /***********************/
            
        }catch( Exception e ){
            
            log.error( e.getMessage(), e );
            
        }finally{
            
            try{
                if( roteador != null ) roteador.close();
            }catch( Exception e ){
            }finally{
                roteador = null;
            }
            
            try{
                if( distribuidor != null ) distribuidor.close();
            }catch( Exception e ){
            }finally{
                distribuidor = null;
            }
            
            try{
                if( contexto != null ) contexto.term();
            }catch( Exception e ){
            }finally{
                contexto = null;
            }
            
        }
        
    }
    
}
