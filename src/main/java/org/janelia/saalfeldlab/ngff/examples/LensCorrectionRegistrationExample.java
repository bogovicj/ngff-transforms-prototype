package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.DeformationFieldTransform;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.PolynomialTransform2D;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.StackedRealTransform;
import net.imglib2.realtransform.Translation;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.GenericComposite;

public class LensCorrectionRegistrationExample {

	public static void main(String[] args) throws IOException {
		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/lensStitchPipeline";

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		inverseTest();

//		makeEverything( zarr, baseDataset );
//		makeEverythingOld( zarr, baseDataset );

//		buildPolynomialTranformDfield();

		System.out.println( "done done");
	}
	
	public static void inverseTest()
	{
		final Interval interval = new FinalInterval( 256, 256 );

		DeformationFieldTransform<DoubleType> xfm2d = buildLensCorrectionTransform( interval, new double[]{127, 127});
		WrappedIterativeInvertibleRealTransform ixfm = new WrappedIterativeInvertibleRealTransform<>(xfm2d);
		ixfm.getOptimzer().setTolerance(0.1);
		ixfm.getOptimzer().setMaxIters(2000);
		
		final RealTransformSequence seq = new RealTransformSequence();
		seq.add(xfm2d);
		seq.add(ixfm.inverse());
		
		IntervalIterator it = new IntervalIterator(interval);
		RealPoint p = new RealPoint( 0, 0 );
		while( it.hasNext() )
		{
			it.fwd();
			seq.apply(it, p);
			
			double dx = it.getDoublePosition(0) - p.getDoublePosition(0);
			double dy = it.getDoublePosition(1) - p.getDoublePosition(1);
			double dist = Math.sqrt( dx*dx + dy*dy );
			if( dist > 0.2 )
				System.out.println( dist );
		}
	}

	public static void makeEverything( final N5Writer n5, final String baseDataset ) throws IOException
	{
		final UnsignedByteType type = new UnsignedByteType();
//		final double[] resolution = new double[] { 0.8, 0.8, 2.5 };
//		final double[] resolution = new double[] { 1, 1, 1 };
		final double[] resolution = new double[] { 0.8, 0.8, 1.2 };

		RealRandomAccessible<UnsignedByteType> raw = makeRawData();
		int[] blkSize = new int[] {64,64,64};
		GzipCompression compression = new GzipCompression();
		
		// make tiles
		int w = 256;
		Interval interval = new FinalInterval( w, w, w );

		double o = 15;
		double loC = (0.25 * w) + o;
		double hiC = (0.75 * w) - o;
		ArrayList<double[]> centers = new ArrayList<>();
		centers.add(new double[] {loC, loC, w / 2});
		centers.add(new double[] {loC, hiC, w / 2});
		centers.add(new double[] {hiC, loC, w / 2});
		centers.add(new double[] {hiC, hiC, w / 2});

//		tileIntervals.add( Intervals.createMinMax(lomin, lomin, 0, lomax, lomax, 255 ));
//		tileIntervals.add( Intervals.createMinMax(himin, lomin, 0, himax, lomax, 255 ));
//		tileIntervals.add( Intervals.createMinMax(lomin, himin, 0, lomax, himax, 255 ));
//		tileIntervals.add( Intervals.createMinMax(himin, himin, 0, himax, himax, 255 ));
//		final List<RandomAccessibleInterval<UnsignedByteType>> tiles = tile( raw, tileIntervals );

		System.out.println( "write raw tiles");
		int i = 0;
		for( double[] center : centers ) {
			System.out.println( "writing: " + i );
			RandomAccessibleIntervalSource<UnsignedByteType> src = Common.image(type, raw, resolution, center, interval, null, 2);
			RandomAccessibleInterval<UnsignedByteType> img = src .getSource(0, 0);
			N5Utils.save(img, n5, String.format("%s/lenscorrected/tile-%d", baseDataset, i++ ), blkSize, compression);
		}

		DeformationFieldTransform<DoubleType> xfm2d = buildLensCorrectionTransform( interval, new double[]{127, 127});
		StackedRealTransform distortion = new StackedRealTransform( xfm2d, new Scale(1.0) );

		System.out.println( "write distorted tiles");
		i = 0;
		for( double[] center : centers ) {
			System.out.println( "writing distorted: " + i );
			RandomAccessibleIntervalSource<UnsignedByteType> src = Common.image(type, raw, resolution, center, interval, distortion, 2);
			RandomAccessibleInterval<UnsignedByteType> img = src .getSource(0, 0);
			N5Utils.save(img, n5, String.format("%s/raw/tile-%d", baseDataset, i ), blkSize, compression);
			i++;
		}
		System.out.println( "make done");
	}

	public static void makeEverythingOld( final N5Writer n5, final String baseDataset ) throws IOException
	{
		Img<UnsignedByteType> raw = makeRawDataImg();
		int[] blkSize = new int[] {64,64,64};
		GzipCompression compression = new GzipCompression();
		
		// make tiles
		ArrayList<Interval> tileIntervals = new ArrayList<>();
		int w = 128;
		int o = 15;
		int lomin = o;
		int lomax = o + w - 1;
		int himin = lomax - o;
		int himax = himin + w - 1;
		tileIntervals.add( Intervals.createMinMax(lomin, lomin, 0, lomax, lomax, 255 ));
		tileIntervals.add( Intervals.createMinMax(himin, lomin, 0, himax, lomax, 255 ));
		tileIntervals.add( Intervals.createMinMax(lomin, himin, 0, lomax, himax, 255 ));
		tileIntervals.add( Intervals.createMinMax(himin, himin, 0, himax, himax, 255 ));
		final List<RandomAccessibleInterval<UnsignedByteType>> tiles = tile( raw, tileIntervals );

		System.out.println( "write raw tiles");
		int i = 0;
//		for( RandomAccessibleInterval<UnsignedByteType> t : tiles ) {
//			N5Utils.save(t, n5, String.format("%s/tileRaw-%d", baseDataset, i++ ), blkSize, compression);
//		}

		DeformationFieldTransform<DoubleType> xfm2d = buildLensCorrectionTransform( raw, new double[]{127, 127});
		StackedRealTransform xfm = new StackedRealTransform( xfm2d, new Scale(1.0) );
		WrappedIterativeInvertibleRealTransform ixfm = new WrappedIterativeInvertibleRealTransform<>(xfm);
		List<RealRandomAccessible<UnsignedByteType>> distortedTiles = lensDistort( tiles, ixfm );

//		System.out.println( "write distorted tiles");
//		i = 0;
//		for( RealRandomAccessible<UnsignedByteType> treal : distortedTiles ) {
//			IntervalView<UnsignedByteType> t = Views.interval( Views.raster(treal), tileIntervals.get(i) );
//			N5Utils.save(t, n5, String.format("%s/tile-%d", baseDataset, i ), blkSize, compression);
//			i++;
//		}

		List<RealRandomAccessible<UnsignedByteType>> correctedTiles = lensDistort( tiles, ixfm.inverse() );
		System.out.println( "write corrected tiles");
		i = 0;
		for( RealRandomAccessible<UnsignedByteType> treal : distortedTiles ) {
			IntervalView<UnsignedByteType> t = Views.interval( Views.raster(treal), tileIntervals.get(i) );
			N5Utils.save(t, n5, String.format("%s/tile-lenscorrect-%d", baseDataset, i ), blkSize, compression);
			i++;
		}
	}

	public static <T extends RealType<T> & NativeType<T>> List<RandomAccessibleInterval<T>> tile( 
			final RandomAccessibleInterval<T> img, List<Interval> intervals ) {
		return intervals.stream().map( i -> Views.interval(img, i)).collect(Collectors.toList());
	}
	
	public static <T extends RealType<T> & NativeType<T>> List<RealRandomAccessible<T>> lensDistort( 
			final List<RandomAccessibleInterval<T>> tiles,
			final RealTransform distortion){

		return tiles.stream().map( x -> {	 
			return new RealTransformRandomAccessible< >( 
					Views.interpolate(Views.extendZero(x), new NLinearInterpolatorFactory<>()),
					distortion ); })
				.collect(Collectors.toList());
	}
	
//	public void makeDataAndLensCorrected( final N5Writer n5, final String baseDataset )

	public static RealRandomAccessible<UnsignedByteType> makeRawData()
	{
		Stream<RealPoint> pts = Common.coneSamples( new RealPoint( 128, 128, 128 ), 48, 10, 64 );
		return Common.ptsImage( new UnsignedByteType(), pts.limit(240), 16 );
	}

	public static Img<UnsignedByteType> makeRawDataImg()
	{
		ArrayImg<UnsignedByteType, ByteArray> img = ArrayImgs.unsignedBytes(256, 256, 256);
		Stream<RealPoint> pts = Common.coneSamples( new RealPoint( 128, 128, 128 ), 48, 10, 64 );
		Common.render(img, pts.limit(240), 16 );
//		BdvStackSource<UnsignedByteType> bdv = BdvFunctions.show(img,"cone");
		return img;


//		DeformationFieldTransform<DoubleType> xfm = buildLensCorrectionTransform( img, new double[]{127, 127});
//		WrappedIterativeInvertibleRealTransform ixfm = new WrappedIterativeInvertibleRealTransform<>(xfm);
//
//		RealRandomAccessible<DoubleType> imgXfm = new RealTransformRandomAccessible< >( 
//				Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory<>() ),
//				ixfm.inverse() );	
	}

	public static DeformationFieldTransform<DoubleType> buildLensCorrectionTransform( Interval dims,
			final double[] center )
	{
		FunctionRealRandomAccessible<DoubleType> dx = new FunctionRealRandomAccessible<DoubleType>( 2, 
				(p,v) -> {
					v.setReal( 0.12 * (center[0] - p.getDoublePosition(0)) +
							   0.04 * (center[1] - p.getDoublePosition(1))
							);
//					if( p.getDoublePosition( 0 ) > center[ 0 ]) { v.setReal( 1 ); }
//					else { v.setReal( -1 ); }
				},
				DoubleType::new );
		
		FunctionRealRandomAccessible<DoubleType> dy = new FunctionRealRandomAccessible<DoubleType>( 2, 
				(p,v) -> {
					v.setReal( 0.16 * (center[1] - p.getDoublePosition(1)) + 
							   0.05 * (center[0] - p.getDoublePosition(0)));

//					if( p.getDoublePosition( 1 ) > center[ 1 ]) { v.setReal( 1 ); }
//					else { v.setReal( -1 ); }
				},
				DoubleType::new );
		
		return new DeformationFieldTransform<>(dx, dy);
	}
	
	public static void buildPolynomialTranformDfield()
	{
		PolynomialTransform2D pxfm = new PolynomialTransform2D();
//		double[] coefs = new double[]{ 3.0, 2.0 };

		double[] coefs = new double[]{ 3.0, 2.0,  0.1, 0.2, 0.3, 0.4 };

//		double[] coefs = new double[]{
//				0.5, 			// 0th order term
//				0.8, 1.2, 		// 1st order terms 
//				0.2, 0.3, 0.4 	// second order terms
//		};

		pxfm.set(coefs);
		System.out.println( pxfm );
		System.out.println( pxfm );
		
		RealPoint p = new RealPoint( 1, 1 );
		RealPoint q = new RealPoint( 0, 0 );
		pxfm.apply(p, q);
		System.out.println( "" );
		System.out.println( p + " > " + q);

	}


}
