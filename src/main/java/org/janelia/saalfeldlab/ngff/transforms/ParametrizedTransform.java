package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;

public interface ParametrizedTransform<T,P> extends CoordinateTransform<T> {

	public String getParameterPath();
	
	@Override
	public default T getTransform( final N5Reader n5 )
	{
		T t = getTransform();
		if( t != null )
			return t;
		else
		{
			return buildTransform(getParameters(n5));
		}
	}

	public T buildTransform( P parameters );

	public P getParameters( final N5Reader n5 );
}
