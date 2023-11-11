package app;

import ng.appserver.NGActionResults;
import ng.appserver.NGApplication;
import ng.appserver.NGRequest;
import ng.appserver.NGResponse;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		routeTable().map( "/favicon.ico", ( request ) -> {
			return new NGResponse();
		} );
		routeTable().mapComponent( "/lights", LMLightsPage.class );
	}

	@Override
	public NGActionResults defaultResponse( NGRequest request ) {
		return redirectTemporary( "/lights" );
	}

	public static NGResponse redirectTemporary( final String targetURL ) {
		final NGResponse response = new NGResponse();

		response.setHeader( "location", targetURL );
		response.setStatus( 302 );
		response.setHeader( "content-type", "text/html" );
		response.setHeader( "content-length", "0" );

		return response;
	}
}