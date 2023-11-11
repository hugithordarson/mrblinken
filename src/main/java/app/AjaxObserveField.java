package app;

import ng.appserver.NGComponent;
import ng.appserver.NGContext;

public class AjaxObserveField extends NGComponent {

	public AjaxObserveField( NGContext context ) {
		super( context );
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
}