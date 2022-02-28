package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.DisplacementFieldCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.InverseCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.RealCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.SequenceCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.TranslationCoordinateTransform;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.Cursor;
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
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.DeformationFieldTransform;
import net.imglib2.realtransform.InvertibleRealTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.Scale2D;
import net.imglib2.realtransform.StackedRealTransform;
import net.imglib2.realtransform.Translation;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.GenericComposite;

public class LensCorrectionRegistrationExample {

	public static void main(String[] args) throws IOException {
		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
//		final String root = "/groups/saalfeld/home/bogovicj/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/lensStitchPipeline";

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

//		makeEverything( zarr, baseDataset );
		visTest(zarr, baseDataset);

		System.out.println( "done done");
	}

	public static <T extends RealType<T> & NativeType<T>> AffineTransform3D makeTform( final Interval interval, final double[] res, final double[] center ) {
		double[] t = Common.translation( interval, center);
		AffineTransform3D xfm = new AffineTransform3D();
		xfm.scale(res[0], res[1], res[2]);
		xfm.translate( t[0], t[1], t[2] );	
		return xfm;
	}
	
	public static <T extends RealType<T> & NativeType<T>> void visTest( final N5Reader n5, final String baseDataset ) throws IOException
	{

		double[] resHR = new double[]{0.4, 0.4, 0.4};
		Img<T> hr = N5Utils.open(n5, String.format("%s/highResSource", baseDataset ));
		RandomAccessibleIntervalSource<T> hrSrc = new RandomAccessibleIntervalSource<T>(hr, Util.getTypeFromInterval(hr), makeTform(hr, resHR, new double[]{256,256,256}), "HR");

		Img<T> tile0 = N5Utils.open(n5, String.format("%s/lenscorrected/tile-0", baseDataset ));
		Img<T> tile1 = N5Utils.open(n5, String.format("%s/lenscorrected/tile-1", baseDataset ));
		Img<T> tile2 = N5Utils.open(n5, String.format("%s/lenscorrected/tile-2", baseDataset ));
		Img<T> tile3 = N5Utils.open(n5, String.format("%s/lenscorrected/tile-3", baseDataset ));
		T type = Util.getTypeFromInterval(tile0);
		
		ArrayList<double[]> centers = centers( 256 );

		double[] res = new double[]{0.8, 0.8, 2.2 };
		RandomAccessibleIntervalSource<T> src0 = new RandomAccessibleIntervalSource<T>(tile0, type, makeTform(tile0, res, centers.get(0)), "tile-0");
		RandomAccessibleIntervalSource<T> src1 = new RandomAccessibleIntervalSource<T>(tile1, type, makeTform(tile1, res, centers.get(1)), "tile-1");
		RandomAccessibleIntervalSource<T> src2 = new RandomAccessibleIntervalSource<T>(tile2, type, makeTform(tile2, res, centers.get(2)), "tile-2");
		RandomAccessibleIntervalSource<T> src3 = new RandomAccessibleIntervalSource<T>(tile3, type, makeTform(tile3, res, centers.get(3)), "tile-3");

		BdvStackSource<T> bdv = BdvFunctions.show( src0 );
		BdvOptions opts = BdvOptions.options().addTo( bdv );
		BdvFunctions.show( src1, opts );
		BdvFunctions.show( src2, opts );
		BdvFunctions.show( src3, opts );
		BdvFunctions.show( hrSrc, opts );
	}

	public static Spaces makeSpaces()
	{
		final Spaces spaces = new Spaces();
		spaces.add( Common.makeSpace("lens-corrected", "space", "pixel", "corrected.x", "corrected.y", "corrected.z"));
		spaces.add( Common.makeSpace("stitched", "space", "pixel", "stitched.x", "stitched.y", "stitched.z"));

		return spaces;
	}
	
	public static ArrayList<double[]> centers( int w )
	{
		double o = 15;
		double loC = (0.25 * w) + o;
		double hiC = (0.75 * w) - o;
		ArrayList<double[]> centers = new ArrayList<>();
		centers.add(new double[] {loC, loC, w / 2});
		centers.add(new double[] {loC, hiC, w / 2});
		centers.add(new double[] {hiC, loC, w / 2});
		centers.add(new double[] {hiC, hiC, w / 2});
		return centers;
	}

	public static CoordinateTransform<?> makeScaleTranslation( String name, double[] s, double[] t )
	{
		RealCoordinateTransform<?>[] seq = new RealCoordinateTransform<?>[] {
			new ScaleCoordinateTransform("", "", "", s),
			new TranslationCoordinateTransform("", "", "", t) };

		return new SequenceCoordinateTransform(name, "lens-corrected", "stitched", seq );
	}

	public static void makeEverything( final N5Writer n5, final String baseDataset ) throws IOException
	{
		final UnsignedByteType type = new UnsignedByteType();
		final double[] resolution = new double[] { 0.8, 0.8, 1.2 };

		RealRandomAccessible<UnsignedByteType> raw = makeRawData();
		int[] blkSize = new int[] {64,64,64};
		GzipCompression compression = new GzipCompression();
		
		// make tiles
		int w = 256;
		Interval interval = new FinalInterval( w, w, w );
		final ArrayList<double[]> centers = centers( w );
		
		ArrayList<CoordinateTransform<?>> transforms = new ArrayList<>();
		
//		final RandomAccessibleIntervalSource<UnsignedByteType> highResSource = Common.image(type, raw, 
//				new double[]{0.4, 0.4, 0.4}, 
//				new double[]{256,256,256}, 
//				new FinalInterval(512,512,512),
//				null, 2);
//
//		N5Utils.save(highResSource.getSource(0, 0), n5, String.format("%s/highResSource", baseDataset), blkSize, compression);

//		System.out.println( "write raw tiles");
//		int i = 0;
//		for( double[] center : centers ) {
//			System.out.println( "writing: " + i );
//			RandomAccessibleIntervalSource<UnsignedByteType> src = Common.image(type, raw, resolution, center, interval, null, 2);
//			RandomAccessibleInterval<UnsignedByteType> img = src .getSource(0, 0);
//
//			double[] ti = Common.translation( interval, center );
//			CoordinateTransform<?> st = makeScaleTranslation( String.format("tile-%d-stitch", i ), resolution, ti );
//			transforms.add( st );
//
//			N5Utils.save(img, n5, String.format("%s/lenscorrected/tile-%d", baseDataset, i ), blkSize, compression);
//			i++;
//		}
//
//		DeformationFieldTransform<DoubleType> xfm2d = buildLensCorrectionTransform( interval, new double[]{127, 127});
//		StackedRealTransform distortion = new StackedRealTransform( xfm2d, new Scale(1.0) );
//
//		System.out.println( "write distorted tiles");
//		i = 0;
//		for( double[] center : centers ) {
//			System.out.println( "writing distorted: " + i );
//			RandomAccessibleIntervalSource<UnsignedByteType> src = Common.image(type, raw, resolution, center, interval, distortion, 2);
//			RandomAccessibleInterval<UnsignedByteType> img = src.getSource(0, 0);
//			N5Utils.save(img, n5, String.format("%s/raw/tile-%d", baseDataset, i ), blkSize, compression);
//			i++;
//		}
//
//		WrappedIterativeInvertibleRealTransform<?> ixfm = new WrappedIterativeInvertibleRealTransform<>(xfm2d);
//		ixfm.getOptimzer().setTolerance(0.05);
//		ixfm.getOptimzer().setMaxIters(3000);
//
//		Img<FloatType> dfield = makeDfield2dInv( new long[]{256,256}, ixfm );
//		String lensCorrectionTransformPath = String.format("%s/lenscorrected/transform", baseDataset);
//		N5Utils.save(dfield, n5, lensCorrectionTransformPath, blkSize, compression);
//		
//		// make transforms 
//		DisplacementFieldCoordinateTransform lcXfm = new DisplacementFieldCoordinateTransform<>("lens-correction", lensCorrectionTransformPath, "", "lens-corrected");
//		InverseCoordinateTransform lcXfmInv = new InverseCoordinateTransform( "lens-correction", lcXfm );
//		transforms.add( lcXfmInv );
//
//		n5.setAttribute(baseDataset, "spaces", makeSpaces().spaces().toArray(Space[]::new));
//		n5.setAttribute(baseDataset, "transformations", transforms);
//
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

	public static void inverseTest()
	{
		final Interval interval = new FinalInterval( 256, 256 );

		DeformationFieldTransform<DoubleType> xfm2d = buildLensCorrectionTransform( interval, new double[]{127, 127});
		WrappedIterativeInvertibleRealTransform ixfm = new WrappedIterativeInvertibleRealTransform<>(xfm2d);
		ixfm.getOptimzer().setTolerance(0.05);
		ixfm.getOptimzer().setMaxIters(3000);

		Img<FloatType> dfield = makeDfield2dInv( new long[]{256,256}, ixfm );

//		final RealTransformSequence seq = new RealTransformSequence();
//		seq.add(xfm2d);
//		seq.add(ixfm.inverse());
//		
//		IntervalIterator it = new IntervalIterator(interval);
//		RealPoint p = new RealPoint( 0, 0 );
//		double maxDist = -1;
//		while( it.hasNext() )
//		{
//			it.fwd();
//			seq.apply(it, p);
//			
//			double dx = it.getDoublePosition(0) - p.getDoublePosition(0);
//			double dy = it.getDoublePosition(1) - p.getDoublePosition(1);
//			double dist = Math.sqrt( dx*dx + dy*dy );
//			
//			if( dist > maxDist )
//				maxDist = dist;
//			
//			if( dist > 0.1 )
//				System.out.println( dist );
//		}
//		
//		System.out.println( "maxDist: " + maxDist );
	}
	
	/**
	 * Only for pixel units.
	 * 
	 * @param sz  dimensions of output array (2d)
	 * @param xfm inverse transform
	 * @return
	 */
	public static Img<FloatType> makeDfield2dInv( long[] sz, WrappedIterativeInvertibleRealTransform<?> xfm )
	{
		InvertibleRealTransform ixfm = xfm.inverse();

		final ArrayImg<FloatType, FloatArray> dfield = ArrayImgs.floats( 2, sz[0], sz[1] );
		final IntervalView<FloatType> dfieldP = Views.permute( Views.permute(dfield, 1, 0), 2, 1 );
		final CompositeIntervalView<FloatType, ? extends GenericComposite<FloatType>> dfieldC = Views.collapse(dfieldP);
		
//		System.out.println( Intervals.toString( dfield ));
//		System.out.println( Intervals.toString( dfieldP ));
//		System.out.println( Intervals.toString( dfieldC ));

		Cursor<? extends GenericComposite<FloatType>> c = Views.iterable( dfieldC ).cursor();
		RealPoint p = new RealPoint( 2 );
		while( c.hasNext() )
		{
			c.fwd();
			GenericComposite<FloatType> v = c.get();

			ixfm.apply( c, p );
			for( int d = 0 ; d < 2; d++ )
			{
				v.get(d).setReal( p.getDoublePosition(0) - c.getDoublePosition(d));
			}
		}
		
		return dfield;
	}

}
