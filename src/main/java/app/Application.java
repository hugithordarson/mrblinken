package app;

import ng.appserver.NGActionResults;
import ng.appserver.NGApplication;
import ng.appserver.http.NGRequest;
import ng.appserver.http.NGRespBuilder;
import ng.appserver.http.NGResponse;
import ng.plugins.Routes;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	@Override
	public Routes routes() {
		return Routes
				.create()
				.map( "/favicon.ico", ( request ) -> {
					return NGRespBuilder.of();
				} )
				.map( "/lights", LMLightsPage.class );
	}

	@Override
	public NGActionResults defaultResponse( NGRequest request ) {
		return redirectTemporary( "/lights" );
	}

	public static NGResponse redirectTemporary( final String targetURL ) {
		final NGResponse response = NGRespBuilder.of();

		response.setHeader( "location", targetURL );
		response.setStatus( 302 );
		response.setHeader( "content-type", "text/html" );
		response.setHeader( "content-length", "0" );

		return response;
	}
}