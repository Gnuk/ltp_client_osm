/*
	Copyright 2012 OpenTeamMap
	
	This software is a part of LocalizeTeaPot whose purpose is to Localize your friends.
	
	This software is governed by the CeCILL license under French law and
	abiding by the rules of distribution of free software.  You can  use, 
	modify and/ or redistribute the software under the terms of the CeCILL
	license as circulated by CEA, CNRS and INRIA at the following URL
	"http://www.cecill.info". 
	
	As a counterpart to the access to the source code and  rights to copy,
	modify and redistribute granted by the license, users are provided only
	with a limited warranty  and the software's author,  the holder of the
	economic rights,  and the successive licensors  have only  limited
	liability. 
	
	In this respect, the user's attention is drawn to the risks associated
	with loading,  using,  modifying and/or developing or reproducing the
	software by the user in light of its specific status of free software,
	that may mean  that it is complicated to manipulate,  and  that  also
	therefore means  that it is reserved for developers  and  experienced
	professionals having in-depth computer knowledge. Users are therefore
	encouraged to load and test the software's suitability as regards their
	requirements in conditions enabling the security of their systems and/or 
	data to be ensured and,  more generally, to use and operate it in the 
	same conditions as regards security. 
	
	The fact that you are presently reading this means that you have had
	knowledge of the CeCILL license and that you accept its terms.
 */

package fr.univsavoie.ltp.client.tools;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
 
/**
 * Simple SSLFactory implementation which is designed to ignore all the SSL certificate.
 * Note: Please only use this for development testing (for self signed in certificate). Adding this to the 
 * public application is a serious blunder.
 * @author Syed Ghulam Akbar
 */
public class SimpleSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory
{
	private SSLSocketFactory sslFactory = HttpsURLConnection.getDefaultSSLSocketFactory ();
	
	public SimpleSSLSocketFactory (KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
	{
		super(null);
 
		try
		{
			SSLContext context = SSLContext.getInstance ("TLS");
 
			// Create a trust manager that does not validate certificate chains and simply
			// accept all type of certificates
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return new java.security.cert.X509Certificate[] {};
				}
				
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			}};
			
			// Initialize the socket factory
		context.init (null, trustAllCerts, new SecureRandom ());
		sslFactory = context.getSocketFactory ();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException 
	{
		return sslFactory.createSocket(socket, host, port, autoClose);
	}
	
	public Socket createSocket() throws IOException
	{
		return sslFactory.createSocket();
	}
}
