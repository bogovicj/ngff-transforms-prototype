package org.janelia.saalfeldlab.ngff.multiscales;

import org.janelia.saalfeldlab.ngff.spaces.Space;

public class Multiscale {
	
	public String version;
	public String name;
	public String type;
	public Object metadata;
	public DatasetTransform[] datasets;
	public Space[] spaces;
}
