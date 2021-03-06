package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.RealTransform;

public class InverseCoordinateTransform<T extends RealTransform,C extends CoordinateTransform<T>> extends AbstractCoordinateTransform<T> {

	protected C transform;

	public InverseCoordinateTransform( final String name, final C ct ) {
		// input and output spaces / axes must be swapped
		super( "inverse_of", name, ct.getOutputSpace(), ct.getInputSpace());
		super.input_axes = ct.getOutputAxes();
		super.output_axes = ct.getInputAxes();
		this.transform = ct;
	}
	
	public InverseCoordinateTransform( final C ct ) {
		// input and output spaces must be swapped
		this( "inverse_of-" + ct.getName(), ct );
	}

	public T getTransform() {
		return transform.getTransform();
	}
}
