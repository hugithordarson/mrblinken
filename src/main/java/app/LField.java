package app;

import ng.appserver.NGComponent;
import ng.appserver.NGContext;

/**
 * This component should be stateless
 */

public class LField extends NGComponent {

	public LField( NGContext context ) {
		super( context );
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String value() {
		return valueForBinding( "value" ).toString();
	}

	public void setValue( String value ) {
		int intValue;

		try {
			intValue = Integer.parseInt( value );
		}
		catch( Exception e ) {
			intValue = 0;
		}

		if( intValue > 255 ) {
			intValue = 255;
		}

		setValueForBinding( intValue, "value" );
	}
}