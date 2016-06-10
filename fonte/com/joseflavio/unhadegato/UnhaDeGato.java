
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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

import org.zeromq.ZMQ;

import com.joseflavio.copaiba.Copaiba;
import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.copaiba.CopaibaException;

/**
 * Conexão ao {@link Concentrador}.
 * @author José Flávio de Souza Dias Júnior
 */
public class UnhaDeGato implements Closeable {
	
	private ZMQ.Context contexto;
	
	private ZMQ.Socket servidor;

	/**
	 * Conecta-se a um {@link Concentrador}.
	 * @param endereco {@link InetAddress Endereço} do {@link Concentrador}.
	 * @param porta Porta TCP do {@link Concentrador}.
	 */
	public UnhaDeGato( String endereco, int porta ) throws IOException {
		
		try{
		
			contexto = ZMQ.context( 1 );
			servidor = contexto.socket( ZMQ.REQ );
			servidor.connect( "tcp://" + endereco + ":" + porta );
		
		}catch( RuntimeException e ){
			throw e;
		}catch( Exception e ){
			throw new IOException( e );
		}
		
	}
	
	/**
	 * {@link CopaibaConexao#executar(String, String, java.io.Writer, boolean)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @return objeto resultante da rotina, em formato JSON.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String executar( String copaiba, String linguagem, String rotina ) throws RuntimeException, IOException {
		return enviar( 1, copaiba, linguagem, rotina );
	}
	
	/**
	 * {@link CopaibaConexao#atribuir(String, String, String)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public void atribuir( String copaiba, String variavel, String classe, String json ) throws RuntimeException, IOException {
		enviar( 2, copaiba, variavel, classe, json );
	}
	
	/**
	 * {@link CopaibaConexao#obter(String, boolean)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String obter( String copaiba, String variavel ) throws RuntimeException, IOException {
		return enviar( 3, copaiba, variavel );
	}
	
	/**
	 * {@link CopaibaConexao#obter(String, String, boolean, java.io.Serializable...)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String obter( String copaiba, String objeto, String metodo ) throws RuntimeException, IOException {
		return enviar( 4, copaiba, objeto, metodo );
	}
	
	/**
	 * {@link CopaibaConexao#remover(String)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public void remover( String copaiba, String variavel ) throws RuntimeException, IOException {
		enviar( 5, copaiba, variavel );
	}
	
	/**
	 * {@link CopaibaConexao#solicitar(String, String, String)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String solicitar( String copaiba, String classe, String estado, String metodo ) throws RuntimeException, IOException {
		return enviar( 6, copaiba, classe, estado, metodo );
	}
	
	private String enviar( int instrucao, String... parametros ) throws RuntimeException, IOException {
		
		if( servidor == null ) throw new IOException( "Fechado." );
		
		try{
			
			StringBuilder str = new StringBuilder( parametros.length * 20 );
			for( String parametro : parametros ){
				str.append( parametro );
				str.append( '\b' );
			}
			
			byte[] bytes_str = str.toString().getBytes( "UTF-8" );
			byte[] bytes = new byte[ 2 + bytes_str.length ];
			
			bytes[0] = (byte) instrucao;
			bytes[1] = (byte) parametros.length;
			System.arraycopy( bytes_str, 0, bytes, 2, bytes_str.length );
			
			servidor.send( bytes, 0 );
			
			String retorno = new String( servidor.recv( 0 ), "UTF-8" );

			//Unha-de-gato.ERRO@Classe@Mensagem
			if( retorno.startsWith( "Unha-de-gato.ERRO" ) ){
				String[] p = retorno.split( "@" );
				dispararErro( p[1], p[2] );
			}
			
			return retorno;
		
		}catch( RuntimeException e ){
			throw e;
		}catch( IOException e ){
			throw e;
		}catch( Exception e ){
			throw new IOException( e );
		}
		
	}
	
	private static void dispararErro( String classe, String mensagem ) throws IOException {
		
		try{
			
			Class<?> tipo = null;
			try{
				tipo = Class.forName( classe );
			}catch( ClassNotFoundException e ){
				throw new IOException( classe + ": " + mensagem );
			}
			
			Throwable objeto = null;
			try{
				objeto = (Throwable) tipo.getConstructor( String.class ).newInstance( mensagem );
			}catch( Exception e ){
				try{
					objeto = (Throwable) tipo.newInstance();
				}catch( Exception f ){
					throw new IOException( mensagem );
				}
			}
			
			if( RuntimeException.class.isAssignableFrom( tipo ) ) throw objeto;
			throw new IOException( objeto );
		
		}catch( RuntimeException e ){
			throw e;
		}catch( IOException e ){
			throw e;
		}catch( Throwable e ){
			throw new IOException( e );
		}
		
	}
	
	/**
	 * Fecha e inutiliza esta conexão.
	 */
	public void fechar() {
		
		try{
			if( servidor != null ) servidor.close();
		}catch( Exception e ){
		}finally{
			servidor = null;
		}
		
		try{
			if( contexto != null ) contexto.term();
		}catch( Exception e ){
		}finally{
			contexto = null;
		}
		
	}
	
	@Override
	public void close() throws IOException {
		fechar();
	}
	
}
