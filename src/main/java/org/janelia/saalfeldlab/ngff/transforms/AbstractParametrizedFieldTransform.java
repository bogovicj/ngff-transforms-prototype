package org.janelia.saalfeldlab.ngff.transforms;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public abstract class AbstractParametrizedFieldTransform<T,V extends NumericType<V>> extends AbstractParametrizedTransform<T,RealRandomAccessible<V>[]> {
	
	protected transient RealRandomAccessible<V>[] fields;

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

	public RealRandomAccessible<V>[] getFields() {
		return fields;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RealRandomAccessible<V>[] getParameters(N5Reader n5) {

		final String path = getParameterPath();
		vectorAxisIndex = parseVectorAxisIndex( n5 );

		InvertibleRealTransform ixfm = null;
		CoordinateTransform<?>[] transforms = null;
		try {
			transforms = n5.getAttribute(path, "transformations", CoordinateTransform[].class);
		} catch (IOException e1) { }

		System.out.println( transforms[0] instanceof InvertibleCoordinateTransform );
		if( transforms != null && transforms.length >= 1  &&
			 transforms[0] instanceof InvertibleCoordinateTransform ) {
				
			ixfm = ((InvertibleCoordinateTransform<?>)transforms[0]).getInverseTransform();
		}

		final RandomAccessibleInterval<V> fieldRaw;
		try {
			fieldRaw = (RandomAccessibleInterval<V>) N5Utils.open(n5, path );
		} catch (IOException e) { 
			return null;
		}

		RandomAccessibleInterval<V>[] rawfields;
		int nv = 1;
		if( vectorAxisIndex < 0 ) {
			rawfields = new RandomAccessibleInterval[]{ fieldRaw };
		}
		else {
			nv = (int)fieldRaw.dimension(getVectorAxisIndex());
			rawfields = new RandomAccessibleInterval[nv];
			for( int i = 0; i < nv; i++ )
			{
				rawfields[i] = Views.hyperSlice(fieldRaw, vectorAxisIndex, i);
			}
		}

		fields = new RealRandomAccessible[ rawfields.length ];
		for( int i = 0; i < nv; i++ ) {
			if( ixfm == null )
			{
				fields[i] = Views.interpolate( rawfields[i], new NLinearInterpolatorFactory<>());
			}
			else {
				fields[i] = new RealTransformRealRandomAccessible( 
						Views.interpolate( rawfields[i], new NLinearInterpolatorFactory<>()),
						ixfm );
			}
		}

		return fields;
	}

}
