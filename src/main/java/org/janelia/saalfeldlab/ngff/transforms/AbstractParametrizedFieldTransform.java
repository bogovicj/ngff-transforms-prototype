package org.janelia.saalfeldlab.ngff.transforms;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public abstract class AbstractParametrizedFieldTransform<T,V extends NumericType<V>> extends AbstractParametrizedTransform<T,RandomAccessible<V>[]> {
	
	protected transient RandomAccessible<V>[] fields;

	// TODO allow explicit 1d parameters ?
//	protected double[] parameters;

	protected transient int vectorAxisIndex;

	public AbstractParametrizedFieldTransform( String type, String name, String inputSpace, String outputSpace ) {
		this( type, name, null, inputSpace, outputSpace );
	}

	public AbstractParametrizedFieldTransform( String type, String name, String parameterPath, 
			String inputSpace, String outputSpace ) {
		super( type, name, parameterPath, inputSpace, outputSpace );
	}
	
	public int getVectorAxisIndex() {
		return vectorAxisIndex;
	}

	public abstract int parseVectorAxisIndex( N5Reader n5 );

	public RandomAccessible<V>[] getFields() {
		return fields;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessible<V>[] getParameters(N5Reader n5) {

		vectorAxisIndex = parseVectorAxisIndex( n5 );
		try {
			final RandomAccessibleInterval<V> fieldRaw = (RandomAccessibleInterval<V>) N5Utils.open(n5, getParameterPath() );

			if( vectorAxisIndex < 0 ) {
				fields = new RandomAccessible[]{ fieldRaw };
			}
			else {
				final int nv = (int)fieldRaw.dimension(getVectorAxisIndex());
				fields = new RandomAccessible[nv];
				for( int i = 0; i < nv; i++ )
					fields[i] = Views.hyperSlice(fieldRaw, vectorAxisIndex, i);
			}

			return fields;
		} catch (IOException e) { }
		return null;
	}

}
