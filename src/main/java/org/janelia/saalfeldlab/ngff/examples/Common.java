package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.ngff.axes.Axis;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;

import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.KDTree;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.algorithm.neighborhood.GeneralRectangleShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.Img;
import net.imglib2.interpolation.neighborsearch.RBFInterpolator;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.parallel.DefaultTaskExecutor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.ScaleAndTranslation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.SubsampleIntervalView;
import net.imglib2.view.SubsampleView;
import net.imglib2.view.Views;

public class Common {

	public static Space makeSpace( String name, String type, String unit, String... labels)
	{
		return new Space( name, 
				Arrays.stream(labels)
					.map( x -> new Axis(x, type, unit ))
					.toArray( Axis[]::new ));
	}
	
	public static <T extends NativeType<T> & NumericType<T>> RandomAccessibleInterval<T> open( N5Reader n5, String dataset ) throws IOException
	{
		@SuppressWarnings("unchecked")
		final CachedCellImg<T, ?> imgRaw = (CachedCellImg<T, ?>) N5Utils.open(n5, dataset);
		final RandomAccessibleInterval<T> img;
		if( imgRaw.numDimensions() == 2)
			img = Views.addDimension(imgRaw, 0, 0);
		else
			img = imgRaw;
		
		return img;
	}
	
	public static <T extends NumericType<T> & NativeType<T>> RandomAccessibleIntervalSource<T> openSource( N5Reader n5, String dataset, TransformGraph graph, String spaceIn ) throws IOException 
	{
		String space = spaceIn == null ? "" : spaceIn;
		final RandomAccessibleInterval<T> img = open( n5 , dataset);
		final AffineTransform3D xfm = graph.path("", space).get().totalAffine3D();
		return new RandomAccessibleIntervalSource<T>(img, Util.getTypeFromInterval(img), xfm, "img2d");	
	}

	public static TransformGraph buildGraph( N5Reader n5 ) throws IOException 
	{
		return buildGraph( n5, "/" );
	}

	public static TransformGraph buildGraph( N5Reader n5, String dataset ) throws IOException 
	{
		Space[] spaces = n5.getAttribute(dataset, "spaces", Space[].class);
		CoordinateTransform[] transforms = n5.getAttribute(dataset, "transformations", CoordinateTransform[].class);
		return new TransformGraph( Arrays.asList( transforms ), Arrays.asList(spaces));	
	}
	
	
	/**
	 * Returns an infinite stream of random points on a circle of the given radius.
	 * 
	 * @param radius the radius
	 * @return random points
	 */
	public static Stream<RealPoint> circleSamples( RealPoint center, double radius, double radiusJitter )
	{
		return Stream.generate( () -> {
			double tht = 2 * Math.PI * Math.random();
			double r = radius + radiusJitter * Math.random();
			return new RealPoint( 
					center.getDoublePosition(0) + r * Math.cos(tht), 
					center.getDoublePosition(1) + r * Math.sin(tht));
		});
	}
	
	/**
	 * Returns an infinite stream of random points on a circle of the given radius.
	 * 
	 * @param radius the radius
	 * @return random points
	 */
	public static Stream<RealPoint> coneSamples( RealPoint center, double radius, double radiusJitter, double height )
	{
		return Stream.generate( () -> {
			double tht = 2 * Math.PI * Math.random();
			double r = radius + radiusJitter * Math.random();
			double z = height * ( 1 - quadSampling());
			double z0 = center.getDoublePosition(2) - (height / 2);
			return new RealPoint( 
					center.getDoublePosition(0) + (z/radius) * r * Math.cos(tht), 
					center.getDoublePosition(1) + (z/radius) * r * Math.sin(tht),
					z0 + z );
		});
	}
	
	public static double quadSampling()
	{
		final double x = Math.random();
		return x * x;
	}
	
	public static <T extends RealType<T>> RealRandomAccessible<T> ptsImage( 
			T type,
			Stream<RealPoint> pts,
			double searchDist )
	{
		final List<RealPoint> ptList = pts.collect(Collectors.toList());

		final List<T> valList = Stream.generate( () ->  {
			T out = type.copy();
			out.setReal( 1.0 );
			return out;
			}).limit(ptList.size()).collect(Collectors.toList());

		final DoubleUnaryOperator rbf = rbf( searchDist );
		final KDTree<T> tree = new KDTree< T >( valList, ptList );
		final RBFInterpolator.RBFInterpolatorFactory< T > interp = 
				new RBFInterpolator.RBFInterpolatorFactory< T >( 
						rbf, searchDist, false,
						tree.firstElement().copy() );

		return Views.interpolate( tree, interp );
	}

	public static <T extends RealType<T>> void render( final RandomAccessibleInterval<T> img, 
			Stream<RealPoint> pts,
			double searchDist )
	{
		RealRandomAccessible<T> rimg = ptsImage( Util.getTypeFromInterval(img),  pts, searchDist );
		RealRandomAccess<T> rra = rimg.realRandomAccess();

		Cursor<T> c = Views.flatIterable(img).cursor();
		while( c.hasNext())
		{
			c.fwd();
			rra.setPosition(c);
			c.get().add(rra.get());
		}

	}
	
	public static DoubleUnaryOperator rbf( final double searchRad ) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double x) {
				if( x > searchRad )
					return 0;
				else if( x <= 0 )
					return 1.0;
				else
					return searchRad - x;
			}
		};
	}
	public static <T> SubsampleIntervalView<T> downsample( RandomAccessibleInterval<T> img, long... factors )
	{
		return Views.subsample(img, factors);
	}
	
	public static <T extends RealType<T>> Img<T> downsampleAvg( RandomAccessibleInterval<T> img, int factor )
	{
		GeneralRectangleShape shp = new GeneralRectangleShape( factor, -factor / 2 , false );
		SubsampleView<Neighborhood<T>> nbrhoods = Views.subsample( shp.neighborhoodsRandomAccessible(Views.extendZero(img)), factor );
		RandomAccess<Neighborhood<T>> nbrhoodsRa = nbrhoods.randomAccess();

		long[] factors = new long[ img.numDimensions()];
		Arrays.fill(factors, factor);

		SubsampleIntervalView<T> z = Views.subsample( img, factors );
		Img<T> out = Util.getSuitableImgFactory(z, Util.getTypeFromInterval(img)).create(z);
		Cursor<T> c = out.cursor();

		double N = 0;
		while( c.hasNext())
		{
			N = 0;
			c.fwd();
			nbrhoodsRa.setPosition(c);
			Neighborhood<T> n = nbrhoodsRa.get();
			Cursor<T> nc = n.cursor();
			while( nc.hasNext() ) {
				c.get().add(nc.next());
				N++;
			}
			c.get().mul(1 / N);
		}

		return out;
	}

	public static <T extends NumericType<T>> Img<T> downsampleAvg( RandomAccessibleInterval<T> img )
	{
		SubsampleIntervalView<T> z = Views.subsample( img, 2, 2 );
		SubsampleIntervalView<T> x = Views.subsample( Views.translate(img, 1, 0 ), 2, 2 );
		SubsampleIntervalView<T> y = Views.subsample( Views.translate(img, 1, 0 ), 2, 2 );
		SubsampleIntervalView<T> xy = Views.subsample( Views.translate(img, 1, 1 ), 2, 2 );
		RandomAccessibleInterval<T> vol = Views.stack(z,x,y,xy);
		RandomAccess<T> ra = vol.randomAccess();

		Img<T> out = Util.getSuitableImgFactory(z, Util.getTypeFromInterval(img)).create(z);
		Cursor<T> c = out.cursor();
		int stackDim = vol.numDimensions()-1;

		while( c.hasNext())
		{
			c.fwd();
			ra.setPosition(0, stackDim );
			ra.setPosition(c);
			for( int i = 0; i < stackDim; i++ ) { 
				c.get().add(ra.get());
				ra.fwd(stackDim);
			}
		}
		return out;
	}
	
	public static double[] translation( Interval itvl, double[] centerPhysical )
	{
		int nd = itvl.numDimensions();
		final double[] tParams = new double[ nd ];
		for( int i = 0; i < nd; i++ ) {
			tParams[ i ] =  centerPhysical[i] - ((itvl.realMax(i) - itvl.realMin(i)) / 2);
//			tParams[ i ] =  ((itvl.realMax(i) - itvl.realMin(i)) / 2) - centerPhysical[i];
		}	
		return tParams;
	}
	
	public static double[] translation( Interval itvl, double[] centerPhysical, double[] resolution )
	{
		int nd = itvl.numDimensions();
		final double[] tParams = new double[ nd ];
		for( int i = 0; i < nd; i++ ) {
//			tParams[ i ] =  centerPhysical[i] - ((itvl.realMax(i) - itvl.realMin(i)) / 2);
			tParams[ i ] =  (resolution[i] * (itvl.realMax(i) - itvl.realMin(i)) / 2) - centerPhysical[i];
		}	
		return tParams;
	}
	
	public static <T extends RealType<T> & NativeType<T>> RandomAccessibleIntervalSource<T> image(
			T type, RealRandomAccessible<T> data, 
			double[] resolution, double[] centerPhysical, Interval itvl, RealTransform distortion,
			int nThreads )
	{
		assert( data.numDimensions() == itvl.numDimensions() );

//		int nd = data.numDimensions();

//		double[] tParams = translation( itvl, centerPhysical, resolution );
		double[] tParams = translation( itvl, centerPhysical );
//		double[] tParams = new double[nd];

		final ScaleAndTranslation st = new ScaleAndTranslation( resolution, tParams);
		final AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale( resolution[0], resolution[1], resolution[2] );
		

		RealTransform totalTransform;
		if( distortion == null )
			totalTransform = st;
		else
		{
			RealTransformSequence seq = new RealTransformSequence();
			seq.add( distortion );
			seq.add( st );
			totalTransform = seq;
		}

		Img<T> img = Util.getSuitableImgFactory(itvl, type ).create(itvl);
		RealTransformRandomAccessible<T,?> rra = new RealTransformRandomAccessible< >( data, totalTransform );
		IntervalView<T> imgVirt = Views.interval(Views.raster(rra), itvl );

		DefaultTaskExecutor exec = new DefaultTaskExecutor( Executors.newFixedThreadPool(nThreads));
		LoopBuilder.setImages(imgVirt, img).multiThreaded(exec).forEachPixel( (x,y) -> y.set(x));
		
		RandomAccessibleIntervalSource<T> src = new RandomAccessibleIntervalSource<T>(
				img, Util.getTypeFromInterval(img), xfm, "img");	

		return src;
	}
}
