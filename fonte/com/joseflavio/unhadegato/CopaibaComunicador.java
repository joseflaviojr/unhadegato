
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
import com.joseflavio.copaiba.Modo;
import com.joseflavio.urucum.comunicacao.Consumidor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link Thread} de {@link CopaibaConexao comunicação} com uma {@link Copaiba}.
 * @author José Flávio de Souza Dias Júnior
 */
class CopaibaComunicador extends Thread {
    
    private CopaibaGerenciador gerenciador;
    
    private boolean interrompida = false;
    
    public CopaibaComunicador( CopaibaGerenciador gerenciador ) {
        this.gerenciador = gerenciador;
    }
    
    @Override
    public void run() {
    
        CopaibaConexao copaiba = null;
        
        Consumidor consumidor = null;
        
        int      requisicao = -1;
        String[] argumentos = null;
        int      tentativas = 0;
        boolean  fechar     = false;
        
        while( true ){
    
            try{
                
                // Interrupção
                
                if( interrompida ){
                    if( consumidor == null ){
                        Util.fechar( copaiba );
                        copaiba = null;
                        return;
                    }else{
                        try{
                            copaiba.verificar();
                        }catch( Exception f ){
                            Util.fechar( copaiba );
                            Util.fechar( consumidor );
                            copaiba = null;
                            consumidor = null;
                            return;
                        }
                    }
                }
                
                // Copaíba
                
                if( copaiba == null ){
                    try{
                        copaiba = new CopaibaConexao(
                            gerenciador.getEndereco(),
                            gerenciador.getPorta(),
                            gerenciador.isSegura(),
                            gerenciador.isIgnorarCertificado(),
                            Modo.JAVA,
                            gerenciador.getUsuario(),
                            gerenciador.getSenha()
                        );
                    }catch( Exception e ){
                        Thread.sleep( 2000 );
                        continue;
                    }
                }
                
                // Consumidor
                
                if( consumidor == null ){
                    consumidor = gerenciador.obterConsumidor();
                    requisicao = -1;
                    argumentos = null;
                    tentativas = 0;
                    fechar     = false;
                }
    
                if( consumidor == null ){
                    try{
                        Thread.sleep( 40 );
                    }catch( InterruptedException f ){
                    }finally{
                        continue;
                    }
                }
                
                // Requisição
    
                InputStream is  = null;
                OutputStream os = null;
                
                try{
                    
                    is = consumidor.getInputStream();
                    os = consumidor.getOutputStream();
        
                    if( requisicao == -1 ){
        
                        requisicao = is.read();
        
                        int total = Util.receberInt( is );
                        argumentos = new String[total];
                        for( int i = 0; i < total; i++ ){
                            argumentos[i] = Util.receberString( is );
                        }
                        
                    }
                    
                }catch( Exception e ){
                    fechar = true;
                    throw e;
                }
    
                // Processamento
    
                String resultado = null;
                
                try{
    
                    tentativas++;
    
                    switch( requisicao ){
                        case 1 :
                            resultado = (String) copaiba.executar(
                                argumentos[0],
                                argumentos[1],
                                null,
                                true
                            );
                            break;
                        case 2 :
                            copaiba.atribuir(
                                argumentos[0],
                                argumentos[1],
                                argumentos[2]
                            );
                            break;
                        case 3 :
                            resultado = (String) copaiba.obter(
                                argumentos[0],
                                true
                            );
                            break;
                        case 4 :
                            resultado = (String) copaiba.obter(
                                argumentos[0],
                                argumentos[1],
                                true
                            );
                            break;
                        case 5 :
                            copaiba.remover(
                                argumentos[0]
                            );
                            break;
                        case 6 :
                            resultado = copaiba.solicitar(
                                argumentos[0],
                                argumentos[1],
                                argumentos[2]
                            );
                            break;
                    }
                    
                }catch( Exception e ){
                    if( Util.isIOException( e ) ){
                        try{
                            copaiba.verificar();
                        }catch( Exception f ){
                            Util.fechar( copaiba );
                            copaiba = null;
                            if( tentativas == 1 ) continue;
                        }
                    }
                    resultado = "##Unha-de-gato.ERRO@" + e.getClass().getName() + "@" + e.getMessage();
                }
    
                // Resposta
    
                Util.enviarTexto( os, resultado );
                
                // Fechamento
                
                try{
                    is.read();
                }catch( Exception e ){
                }finally{
                    Util.fechar( consumidor );
                    consumidor = null;
                }
    
            }catch( Exception e ){
                
                if( copaiba != null ){
                    try{
                        copaiba.verificar();
                    }catch( Exception f ){
                        Util.fechar( copaiba );
                        copaiba = null;
                    }
                }
                
                if( consumidor != null ){
                    if( fechar || ! consumidor.isAberto() ){
                        Util.fechar( consumidor );
                        consumidor = null;
                    }
                }
                
            }
    
        }
        
    }
    
    @Override
    public void interrupt() {
        interrompida = true;
        super.interrupt();
    }
    
}
