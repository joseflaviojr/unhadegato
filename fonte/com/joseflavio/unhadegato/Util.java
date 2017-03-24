
/*
 *  Copyright (C) 2016 Jos� Fl�vio de Souza Dias J�nior
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
 *  Direitos Autorais Reservados (C) 2016 Jos� Fl�vio de Souza Dias J�nior
 *
 *  Este arquivo � parte de Unha-de-gato - <http://www.joseflavio.com/unhadegato/>.
 *
 *  Unha-de-gato � software livre: voc� pode redistribu�-lo e/ou modific�-lo
 *  sob os termos da Licen�a P�blica Menos Geral GNU conforme publicada pela
 *  Free Software Foundation, tanto a vers�o 3 da Licen�a, como
 *  (a seu crit�rio) qualquer vers�o posterior.
 *
 *  Unha-de-gato � distribu�do na expectativa de que seja �til,
 *  por�m, SEM NENHUMA GARANTIA; nem mesmo a garantia impl�cita de
 *  COMERCIABILIDADE ou ADEQUA��O A UMA FINALIDADE ESPEC�FICA. Consulte a
 *  Licen�a P�blica Menos Geral do GNU para mais detalhes.
 *
 *  Voc� deve ter recebido uma c�pia da Licen�a P�blica Menos Geral do GNU
 *  junto com Unha-de-gato. Se n�o, veja <http://www.gnu.org/licenses/>.
 */

package com.joseflavio.unhadegato;

import com.joseflavio.copaiba.Copaiba;
import com.joseflavio.copaiba.CopaibaConexao;
import com.joseflavio.urucum.arquivo.ResourceBundleCharsetControl;
import com.joseflavio.urucum.comunicacao.Consumidor;
import com.joseflavio.urucum.texto.StringUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;

/**
 * @author Jos� Fl�vio de Souza Dias J�nior
 */
class Util {
    
    private static ResourceBundle resourceBundle;
    
    static {
        resourceBundle = ResourceBundle.getBundle( "UnhaDeGato", new ResourceBundleCharsetControl( "UTF-8" ) );
    }
    
    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    /**
     * @see StringUtil#formatarMensagem(ResourceBundle, String, Object...)
     */
    public static String getMensagem( String mensagem, Object... parametros ) {
        return StringUtil.formatarMensagem( getResourceBundle(), mensagem, parametros );
    }
    
    /**
     * {@link InputStream Recep��o} de bytes conforme padr�o de comunica��o da {@link Copaiba}.
     */
    public static void receberBytes( InputStream is, byte[] bytes, int inicio, int total ) throws IOException {
        int b;
        while( total-- > 0 ){
            b = is.read();
            if( b == -1 ) throw new EOFException();
            bytes[inicio++] = (byte) b;
        }
    }
    
    /**
     * {@link InputStream Recep��o} de {@link Integer} conforme padr�o de comunica��o da {@link Copaiba}.
     */
    public static int receberInt( InputStream is ) throws IOException {
        int ch1 = is.read();
        int ch2 = is.read();
        int ch3 = is.read();
        int ch4 = is.read();
        if( ( ch1 | ch2 | ch3 | ch4 ) < 0 ) throw new EOFException();
        return ( ( ch1 << 24 ) + ( ch2 << 16 ) + ( ch3 << 8 ) + ( ch4 << 0 ) );
    }
    
    /**
     * {@link InputStream Recep��o} de {@link String} conforme padr�o de comunica��o da {@link Copaiba}.
     */
    public static String receberString( InputStream is ) throws IOException {
        int total = receberInt( is );
        if( total < 0 ) return null;
        if( total == 0 ) return "";
        byte[] bytes = new byte[total];
        receberBytes( is, bytes, 0, total );
        return new String( bytes, "UTF-8" );
    }
    
    /**
     * {@link OutputStream Envio} de bytes conforme padr�o de comunica��o da {@link Copaiba}.
     */
    public static void enviarBytes( OutputStream os, byte[] bytes, int inicio, int total ) throws IOException {
        while( total-- > 0 ) os.write( bytes[inicio++] );
        os.flush();
    }
    
    /**
     * {@link OutputStream Envio} de {@link Integer} conforme padr�o de comunica��o da {@link Copaiba}.
     */
    public static void enviarInt( OutputStream os, int valor ) throws IOException {
        os.write( ( valor >>> 24 ) & 0xFF );
        os.write( ( valor >>> 16 ) & 0xFF );
        os.write( ( valor >>> 8  ) & 0xFF );
        os.write( ( valor >>> 0  ) & 0xFF );
        os.flush();
    }
    
    /**
     * {@link OutputStream Envio} de {@link String} conforme padr�o de comunica��o da {@link Copaiba}.
     */
    public static void enviarTexto( OutputStream os, String texto ) throws IOException {
        if( texto != null ){
            if( texto.length() > 0 ){
                byte[] bytes = texto.getBytes( "UTF-8" );
                enviarInt( os, bytes.length );
                enviarBytes( os, bytes, 0, bytes.length );
            }else{
                enviarInt( os, 0 );
            }
        }else{
            enviarInt( os, -1 );
        }
        os.flush();
    }
    
    public static boolean isIOException( Throwable e ) {
        if( e == null ) return false;
        return e instanceof IOException ? true : isIOException( e.getCause() );
    }
    
    public static void fechar( CopaibaConexao copaiba ) {
        try{
            if( copaiba != null ){
                copaiba.fechar( true );
            }
        }catch( Exception e ){
        }
    }
    
    public static void fechar( Consumidor consumidor ) {
        try{
            if( consumidor != null ){
                consumidor.fechar();
            }
        }catch( Exception e ){
        }
    }
    
}
