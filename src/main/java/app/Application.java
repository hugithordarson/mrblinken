package app;

import java.util.concurrent.TimeUnit;

import ng.appserver.NGActionResults;
import ng.appserver.NGApplication;
import ng.appserver.http.NGRequest;
import ng.appserver.http.NGResponses;
import ng.appserver.http.NGResponse;
import ng.plugins.Routes;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		// Stop a running storm on shutdown, so the strip and its fade setting are restored
		Runtime.getRuntime().addShutdownHook( new Thread( () -> {
			try {
				Lightning.defaultLightning().stopStorm().get( 5, TimeUnit.SECONDS );
			}
			catch( Exception e ) {
				// Nothing to do, we're going down anyway
			}
		} ) );

		NGApplication.run( args, Application.class );
	}

	@Override
	public Routes routes() {
		return Routes
				.create()
				.map( "/favicon.ico", ( request ) -> {
					return NGResponses.of();
				} )
				.map( "/lights", LMLightsPage.class );
	}

	@Override
	public NGActionResults defaultResponse( NGRequest request ) {
		return redirectTemporary( "/lights" );
	}

	public static NGResponse redirectTemporary( final String targetURL ) {
		final NGResponse response = NGResponses.of();

		response.setHeader( "location", targetURL );
		response.setStatus( 302 );
		response.setHeader( "content-type", "text/html" );
		response.setHeader( "content-length", "0" );

		return response;
	}
}