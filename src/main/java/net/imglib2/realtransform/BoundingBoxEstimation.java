package net.imglib2.realtransform;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.iterator.RealIntervalIterator;

/**
 * Temporarily copied from:
 * https://github.com/saalfeldlab/bigwarp/tree/bboxEst/src/main/java/net/imglib2/realtransform  
 * 	
 * @author John Bogovic
 *
 */
public class BoundingBoxEstimation {

	public static enum Method { CORNERS, FACES, VOLUME };

	private Method method;

	private int samplesPerDim = 5;

	private double[] steps;

	public BoundingBoxEstimation()
	{
		this( Method.FACES );
	}

	public BoundingBoxEstimation( final Method method )
	{
		setMethod( method );
	}

	public BoundingBoxEstimation( final Method method, int samplesPerDim )
	{
		setMethod( method );
		setSamplesPerDim( samplesPerDim );
	}

	public void setMethod( final String method )
	{
		setMethod( Method.valueOf(method) );
	}

	public void setMethod( final Method method )
	{
		this.method = method;
	}

	public void setSamplesPerDim( int samplesPerDim )
	{
		this.samplesPerDim = samplesPerDim;
	}

	public double[] setSamplesPerDim( final RealInterval itvl, int maxSamples )
	{
		steps = samplesPerDim( itvl, maxSamples );
		return steps;
	}

	public static double[] samplesPerDim( final RealInterval itvl, final int maxSamples )
	{
		final int nd = itvl.numDimensions();
		double step = Double.MIN_VALUE;
		for( int i = 0; i < nd; i++ )
		{
			double ns = (itvl.realMax(i) - itvl.realMin(i)) / maxSamples;
			if( ns > step )
				step = ns;
		}

		double[] steps = new double[ nd ];
		Arrays.fill( steps, step );
		return steps;
	}

	public Interval estimatePixelInterval( RealTransform xfm, Interval interval )
	{
		steps = samplesPerDim( interval, samplesPerDim );

		switch( method )
		{
		case CORNERS:
			return corners(xfm, interval);
		case VOLUME:
			return volume(xfm, interval, steps );
		default:
			return faces( xfm, interval, steps );
		}
	}

	public BoundingBoxEstimation copy()
	{
		return new BoundingBoxEstimation(method, samplesPerDim);
	}

	/**
	 * The smallest discrete {@link Interval} containing the given {@link RealInterval}.}
	 * 
	 * @param realInterval the real interval 
	 * @return the interval
	 */
	public static FinalInterval containingInterval( RealInterval realInterval )
	{
		final int nd = realInterval.numDimensions();
		final long[] min = new long[ nd ];
		final long[] max = new long[ nd ];
		for( int i = 0; i < nd; i++ )
		{
			min[i] = (long)Math.floor( realInterval.realMin(i) );
			max[i] = (long)Math.ceil( realInterval.realMax( i ));
		}

		return new FinalInterval( min, max );
	}

	public static FinalInterval faces( RealTransform xfm, Interval interval, double[] steps )
	{
		return containingInterval(facesReal( xfm, interval, steps ));
	}

	public static FinalRealInterval facesReal( RealTransform xfm, Interval interval, double[] stepsIn )
	{
		if( xfm == null )
			return new FinalRealInterval( interval );

		double[] steps;
		if( stepsIn == null )
		{
			steps = samplesPerDim( interval, 5 );
		}
		else 
			steps = stepsIn;

		final int nd = interval.numDimensions();

		final double[] min = new double[ nd ];
		final double[] max = new double[ nd ];
		Arrays.fill( min, Long.MAX_VALUE );
		Arrays.fill( max, Long.MIN_VALUE );

		final double[] itvlMin = new double[ nd ];
		final double[] itvlMax = new double[ nd ];
		for( int i = 0; i < nd; i++ )
		{
			subInterval( interval, interval.realMin( i ), i, itvlMin, itvlMax );
			minMaxInterval( xfm, new FinalRealInterval(itvlMin, itvlMax), steps, min, max );

			subInterval( interval, interval.realMax( i ) , i, itvlMin, itvlMax );
			minMaxInterval( xfm, new FinalRealInterval(itvlMin, itvlMax), steps, min, max );
		}

		return new FinalRealInterval( min, max );
	}

	public static void minMaxInterval( final RealTransform xfm, final RealInterval interval, final double[] steps,
			final double[] min, final double[] max )
	{
		final int nd = interval.numDimensions();
		final RealIntervalIterator it = new RealIntervalIterator( interval, steps );	
		final RealPoint ptxfm = new RealPoint( nd );

		while( it.hasNext() )
		{
			it.fwd();
			xfm.apply( it, ptxfm );

			for( int d = 0; d < nd; d++ )
			{
				long lo = (long)Math.floor( ptxfm.getDoublePosition(d) );
				long hi = (long)Math.ceil( ptxfm.getDoublePosition(d) );

				if( lo < min[ d ])
					min[ d ] = lo;

				if( hi > max[ d ])
					max[ d ] = hi;
			}
		}
	}

	public static void subInterval( final RealInterval interval, final double pos, final int dim,
			final double[] min, final double[] max )
	{
		interval.realMin(min);
		min[dim] = pos;

		interval.realMax(max);
		max[dim] = pos;
	}

	public static FinalRealInterval volumeReal( RealTransform xfm, Interval interval, double[] steps )
	{
		if( xfm == null )
			return new FinalRealInterval( interval );

		int nd = interval.numDimensions();
		double[] min = new double[ nd ];
		double[] max = new double[ nd ];
		Arrays.fill( min, Long.MAX_VALUE );
		Arrays.fill( max, Long.MIN_VALUE );

		minMaxInterval( xfm, interval, steps, min, max );
		return new FinalRealInterval( min, max );
	}

	public static FinalInterval volume( RealTransform xfm, Interval interval, double[] steps )
	{
		return containingInterval( volumeReal( xfm, interval, steps ));
	}

	public static FinalInterval corners( RealTransform xfm, Interval interval )
	{
		if( xfm == null )
			return new FinalInterval( interval );

		int nd = interval.numDimensions();
		double[] pt = new double[ nd ];
		double[] ptxfm = new double[ nd ];

		long[] min = new long[ nd ];
		long[] max = new long[ nd ];
		Arrays.fill( min, Long.MAX_VALUE );
		Arrays.fill( max, Long.MIN_VALUE );

		long[] unitInterval = new long[ nd ];
		Arrays.fill( unitInterval, 2 );

		IntervalIterator it = new IntervalIterator( unitInterval );
		while( it.hasNext() )
		{
			it.fwd();
			for( int d = 0; d < nd; d++ )
			{
				if( it.getLongPosition( d ) == 0 )
					pt[ d ] = interval.min( d );
				else
					pt[ d ] = interval.max( d );
			}

			xfm.apply( pt, ptxfm );

			for( int d = 0; d < nd; d++ )
			{
				long lo = (long)Math.floor( ptxfm[d] );
				long hi = (long)Math.ceil( ptxfm[d] );

				if( lo < min[ d ])
					min[ d ] = lo;

				if( hi > max[ d ])
					max[ d ] = hi;
			}
		}
		return new FinalInterval( min, max );
	}

}
