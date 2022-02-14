package org.janelia.saalfeldlab.ngff.transforms;

public interface CoordinateTransform {

	public Object getTransform();

	public String getName();
	
	public String getInputSpace();

	public String getOutputSpace();
}
