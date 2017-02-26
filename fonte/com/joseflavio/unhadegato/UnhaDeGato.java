
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

import com.joseflavio.copaiba.Copaiba;
import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.copaiba.CopaibaException;
import com.joseflavio.urucum.comunicacao.Consumidor;
import com.joseflavio.urucum.comunicacao.SocketConsumidor;

import java.io.*;

/**
 * Conexão ao {@link Concentrador}.
 * @author José Flávio de Souza Dias Júnior
 */
public class UnhaDeGato implements Closeable {
	
	/**
	 * Versão da {@link UnhaDeGato}.
	 */
	public static final String VERSAO = "1";
	
	private String endereco;
	
	private int porta;
	
	private boolean segura;
	
	private boolean ignorarCertificado;
	
	/**
	 * Argumentos para conexão a um {@link Concentrador}.<br>
	 * A {@link SocketConsumidor#SocketConsumidor(String, int, boolean, boolean) conexão} será realizada por demanda,
	 * fechando após cada chamada.
	 * @see SocketConsumidor#SocketConsumidor(String, int, boolean, boolean)
	 */
	public UnhaDeGato( String endereco, int porta, boolean segura, boolean ignorarCertificado ) {
		this.endereco = endereco;
		this.porta = porta;
		this.segura = segura;
		this.ignorarCertificado = ignorarCertificado;
	}
	
	/**
	 * Conecta-se a um {@link Concentrador}, de forma não segura (sem SSL).
	 * @see UnhaDeGato#UnhaDeGato(String, int, boolean, boolean)
	 */
	public UnhaDeGato( String endereco, int porta ) {
		this( endereco, porta, false, true );
	}
	
	/**
	 * {@link CopaibaConexao#executar(String, String, java.io.Writer, boolean)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @return objeto resultante da rotina, em formato JSON.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String executar( String copaiba, String linguagem, String rotina ) throws RuntimeException, IOException {
		return enviar( copaiba, 1, linguagem, rotina );
	}
	
	/**
	 * {@link CopaibaConexao#atribuir(String, String, String)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public void atribuir( String copaiba, String variavel, String classe, String json ) throws RuntimeException, IOException {
		enviar( copaiba, 2, variavel, classe, json );
	}
	
	/**
	 * {@link CopaibaConexao#obter(String, boolean)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String obter( String copaiba, String variavel ) throws RuntimeException, IOException {
		return enviar( copaiba, 3, variavel );
	}
	
	/**
	 * {@link CopaibaConexao#obter(String, String, boolean, java.io.Serializable...)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String obter( String copaiba, String objeto, String metodo ) throws RuntimeException, IOException {
		return enviar( copaiba, 4, objeto, metodo );
	}
	
	/**
	 * {@link CopaibaConexao#remover(String)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public void remover( String copaiba, String variavel ) throws RuntimeException, IOException {
		enviar( copaiba, 5, variavel );
	}
	
	/**
	 * {@link CopaibaConexao#solicitar(String, String, String)}
	 * @param copaiba Nome que identifica a {@link Copaiba} desejada.
	 * @throws RuntimeException devido a problemas locais ou remotos.
	 * @throws IOException devido a problemas de rede ou por {@link CopaibaException}.
	 */
	public String solicitar( String copaiba, String classe, String estado, String metodo ) throws RuntimeException, IOException {
		return enviar( copaiba, 6, classe, estado, metodo );
	}
	
	/**
	 * Consulta a {@link UnhaDeGato#VERSAO} implementada pelo {@link Concentrador}.
	 */
	public String versao() throws RuntimeException, IOException {
		return enviar( "##Unha-de-gato.VERSAO", 0 );
	}
	
	private String enviar( String copaiba, int requisicao, String... parametros ) throws RuntimeException, IOException {
		
		Consumidor consumidor = null;
		
		try{
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream( 128 );
			
			Util.enviarTexto( baos, copaiba );
			
			baos.write( requisicao );
			
			Util.enviarInt( baos, parametros.length );
			
			for( String parametro : parametros ){
				Util.enviarTexto( baos, parametro );
			}
			
			consumidor = new SocketConsumidor( endereco, porta, segura, ignorarCertificado );
			
			InputStream  is = consumidor.getInputStream();
			OutputStream os = consumidor.getOutputStream();
			
			os.write( baos.toByteArray() );
			os.flush();
			
			String retorno = Util.receberString( is );
			
			try{
				os.write( 0 );
				os.flush();
			}catch( Exception e ){
			}
			
			// ##Unha-de-gato.ERRO@Classe@Mensagem
			if( retorno != null && retorno.startsWith( "##Unha-de-gato.ERRO" ) ){
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
		}finally{
			
			Util.fechar( consumidor );
			consumidor = null;
			
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
	 * Fecha e inutiliza este {@link UnhaDeGato}.
	 */
	public void fechar() {
		endereco = null;
	}
	
	@Override
	public void close() throws IOException {
		fechar();
	}
	
}
