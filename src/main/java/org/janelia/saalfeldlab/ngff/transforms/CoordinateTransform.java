package org.janelia.saalfeldlab.ngff.transforms;

public interface CoordinateTransform<T> {

	public T getTransform();

	public String getName();
	
	public String getInputSpace();

	public String getOutputSpace();
}
