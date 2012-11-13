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
 
	public SimpleSSLSocketFactory (KeyStore truststore)  
	throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
	{
		super(null);
 
		try
		{
			SSLContext context = SSLContext.getInstance ("TLS");
 
			// Create a trust manager that does not validate certificate chains and simply
			// accept all type of certificates
	                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                        return new java.security.cert.X509Certificate[] {};
	                }
 
	                public void checkClientTrusted(X509Certificate[] chain, String authType) 
					throws CertificateException {
	                }
 
	                public void checkServerTrusted(X509Certificate[] chain, String authType) 
					throws CertificateException {
	                }
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
 
	 @Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) 
                                  throws IOException, UnknownHostException 
        {
	    return sslFactory.createSocket(socket, host, port, autoClose);
	}
 
	@Override
	public Socket createSocket() throws IOException {
	    return sslFactory.createSocket();
	}
}
