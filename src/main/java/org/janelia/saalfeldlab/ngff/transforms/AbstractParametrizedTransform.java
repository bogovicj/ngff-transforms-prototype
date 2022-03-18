package org.janelia.saalfeldlab.ngff.transforms;

public abstract class AbstractParametrizedTransform<T,P> extends AbstractCoordinateTransform<T> implements ParametrizedTransform<T,P> {

	private final String path;
	
	public AbstractParametrizedTransform( String type, String name, String inputSpace, String outputSpace ) {
		this( type, name, null, inputSpace, outputSpace );
	}

	public AbstractParametrizedTransform( String type, String name, String parameterPath,
			String inputSpace, String outputSpace ) {
		super( type, name, inputSpace, outputSpace );
		this.path = parameterPath;
	}

	public AbstractParametrizedTransform( String type, String name, String parameterPath, 
			String[] inputAxes, String[] outputAxes ) {
		super( type, name, inputAxes, outputAxes );
		this.path = parameterPath;
	}

	@Override
	public String getParameterPath() {
		return path;
	}

}
