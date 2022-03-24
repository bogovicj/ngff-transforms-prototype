package org.janelia.saalfeldlab.ngff.transforms;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public abstract class AbstractParametrizedRealFieldTransform<T extends RealTransform,P,V extends NumericType<V>> extends AbstractParametrizedTransform<T,RealRandomAccessible<V>[]> {
	
	protected transient RealRandomAccessible<V>[] fields;

//	protected double[] parameters;

	protected int vectorAxisIndex;

	public AbstractParametrizedRealFieldTransform( String type, String name, String inputSpace, String outputSpace ) {
		this( type, name, null, inputSpace, outputSpace );
	}

	public AbstractParametrizedRealFieldTransform( String type, String name, String parameterPath, 
			String inputSpace, String outputSpace ) {
		super( type, name, inputSpace, outputSpace );
	}
	
	public int getVectorAxisIndex() {
		return vectorAxisIndex;
	}

	public abstract int parseVectorAxisIndex( N5Reader n5 );

	public RealRandomAccessible<V>[] getFields() {
		return fields;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RealRandomAccessible<V>[] getParameters(N5Reader n5) {

		vectorAxisIndex = parseVectorAxisIndex( n5 );
		try {
			final RandomAccessibleInterval<V> fieldRaw = (RandomAccessibleInterval<V>) N5Utils.open(n5, getParameterPath() );

			final int nv = (int)fieldRaw.dimension(getVectorAxisIndex());
			fields = new RealRandomAccessible[nv];
			for( int i = 0; i < nv; i++ )
				fields[i] = Views.interpolate( Views.extendBorder(fieldRaw), new NLinearInterpolatorFactory<V>());

			return fields;
		} catch (IOException e) { }
		return null;
	}

}
