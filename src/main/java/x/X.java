package x;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Python client, fínn til að skoða API
 *
 * https://github.com/mattias73andersson/dirigera-client-poc
 */

public class X {

	public static void main( String[] args ) {

		final TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[0];
			}

			@Override
			public void checkServerTrusted( java.security.cert.X509Certificate[] chain, String authType ) throws CertificateException {
				// empty method
			}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1, Socket arg2 ) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1, SSLEngine arg2 ) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted( X509Certificate[] arg0, String arg1, Socket arg2 ) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted( X509Certificate[] arg0, String arg1, SSLEngine arg2 ) throws CertificateException {
				// TODO Auto-generated method stub

			}
		};

		//		String authString = "Basic " + encodeUserPassword();
		//		authString = authString.replaceAll( "\n", "" );
		//		connection.addRequestProperty( "Authorization", authString );

		try {
			SSLContext sslContext = SSLContext.getInstance( "SSL" ); // OR TLS
			sslContext.init( null, new TrustManager[] { MOCK_TRUST_MANAGER }, new SecureRandom() );
			final HttpClient client = HttpClient.newBuilder().sslContext( sslContext ).build();

			final HttpResponse<String> response = client
					.send(
							HttpRequest
									.newBuilder(
											URI.create( "https://172.16.1.78:8443/devices" ) )
									//									.header( "Authorization", "Bearer" )
									.build(),
							BodyHandlers.ofString() );

			System.out.println( response );
		}
		catch( IOException | InterruptedException | KeyManagementException | NoSuchAlgorithmException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}