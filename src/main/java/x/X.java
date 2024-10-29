package x;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
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

	public static void main2( String[] args ) {}

	public static void main( String[] args ) {

		final TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[0];
			}

			@Override
			public void checkServerTrusted( java.security.cert.X509Certificate[] chain, String authType ) throws CertificateException {}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException {}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1, Socket arg2 ) throws CertificateException {}

			@Override
			public void checkClientTrusted( X509Certificate[] arg0, String arg1, SSLEngine arg2 ) throws CertificateException {}

			@Override
			public void checkServerTrusted( X509Certificate[] arg0, String arg1, Socket arg2 ) throws CertificateException {}

			@Override
			public void checkServerTrusted( X509Certificate[] arg0, String arg1, SSLEngine arg2 ) throws CertificateException {}
		};

		//		String authString = "Basic " + encodeUserPassword();
		//		authString = authString.replaceAll( "\n", "" );
		//		connection.addRequestProperty( "Authorization", authString );

		try {
			final SSLContext sslContext = SSLContext.getInstance( "SSL" ); // OR TLS
			sslContext.init( null, new TrustManager[] { MOCK_TRUST_MANAGER }, new SecureRandom() );

			final HttpClient client = HttpClient
					.newBuilder()
					.sslContext( sslContext )
					.build();

			final String url = "https://172.16.1.78:8443/v1/devices/cb3ffb1b-1973-46f5-8a6f-21aa8ec1329c_1";
			final String jsonStringOnOff = """
					[
						{
							"attributes": {
								"isOn": true
							}
						}
					]
					""";

			final String jsonString = """
					[
						{
							"transitionTime":"0",
							"attributes": {
								"lightLevel": 1
							}
						}
					]
					""";

			System.out.println( jsonString );

			final HttpResponse<String> response = client
					.send(
							HttpRequest
									.newBuilder( URI.create( url ) )
									.setHeader( "content-type", "application/json" )
									.setHeader( "Authorization", "Bearer " + "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRhYjZmNTIzZTNiOTE5OTNhMDdmNWFkMjRiYWYyZWQ2MjYxOTZkNWZjNzgyN2YyNzcyZWQ2ZWIyMDczNTgzMGYifQ.eyJpc3MiOiIzYTIwOTkxMi0yN2QwLTQwYzgtYTQyYi03OTU0MTE5ZGUzOTciLCJ0eXBlIjoiYWNjZXNzIiwiYXVkIjoiaG9tZXNtYXJ0LmxvY2FsIiwic3ViIjoiMGE2MzE0YTctYTgzZi00MTgwLTk5MDUtNzliMWNjZmM5YjJhIiwiaWF0IjoxNzMwMTg4NTM1LCJleHAiOjIwNDU3NjQ1MzV9.Cg0jTUX_o7f0nbbJ8bMD4aRF1SaXPrExhc87a-6puO62jP4pg79ny0GFvpH5CM-GO0rgWgW-am0pvElDNRo8Dw" )
									.method( "PATCH", BodyPublishers.ofString( jsonString ) )
									.build(),
							BodyHandlers.ofString() );

			System.out.println( response.body() );
		}
		catch( IOException | InterruptedException | KeyManagementException | NoSuchAlgorithmException e ) {
			e.printStackTrace();
		}
	}
}