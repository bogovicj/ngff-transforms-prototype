package org.janelia.saalfeldlab.ngff.transforms;

public class InverseCoordinateTransform<T,C extends CoordinateTransform<T>> extends AbstractCoordinateTransform<T> {
	
	protected String name;

	protected C ct;

	public InverseCoordinateTransform( final String name, final C ct ) {
		// input and output spaces must be swapped
		super( "inverse_of", name, ct.getOutputSpace(), ct.getInputSpace());
		this.ct = ct;
	}
	
	public InverseCoordinateTransform( final C ct ) {
		// input and output spaces must be swapped
		this( "inverse_of-" + ct.getName(), ct );
	}

	public T getTransform() {
		return ct.getTransform();
	}
}
