package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.IdentityCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.PositionFieldCoordinateTransform;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.cell.CellRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.PositionFieldTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.StackedRealTransform;
import net.imglib2.realtransform.inverse.InverseRealTransformGradientDescent;
import net.imglib2.realtransform.inverse.RealTransformFiniteDerivatives;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class CoordinateListSimplerExample {
	
	public static void main( String[] args ) throws IOException
	{
		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/coordinatesSimple";	

		FinalInterval itvl = new FinalInterval( 8, 8 );

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		generateData( zarr, baseDataset, itvl );
//		show(zarr, baseDataset );

		System.out.println( "done" );
	}
	
	public static <T extends RealType<T> & NativeType<T>> RealTransform readPositionField1d( final N5Reader n5, final String dataset ) throws IOException
	{
		final CachedCellImg<T, ?> pfimg = N5Utils.open(n5, dataset);
		
		System.out.println("");
		CellRandomAccess<T, ?> ra = pfimg.randomAccess();
		for( int i = 0; i < pfimg.dimension(0); i++ )
		{
			ra.setPosition(i, 0);
			System.out.println( ra.get() );
		}
		System.out.println("");
		
//		return new PositionFieldTransform<>(
//				Views.interpolate( Views.extendValue(pfimg, -1),
//					new NLinearInterpolatorFactory<T>()));	

		return new PositionFieldTransform<>( pfimg );
	}

	public static void debugPrintTransform( RealTransform transform )
	{
		System.out.println( "" );
		RealPoint p = new RealPoint( 1 );
		RealPoint q = new RealPoint( 1 );
		for( int i = 0; i < 30; i++ )
		{
			p.setPosition(i, 0);
			transform.apply( p, q );
			System.out.println( q );
		}
		System.out.println( "" );
	}

	public static <T extends NativeType<T> & RealType<T>> void debug( 
			final N5Reader zarr, 
			final String baseDataset) throws IOException
	{

		 CachedCellImg<T, ?> img = N5Utils.open(zarr, baseDataset + "/data");
		 RealTransform px = readPositionField1d( zarr, baseDataset + "/xcoordinates");
		 RealTransform identity = new Scale( 1.0 );
		 
		 debugPrintTransform(px);


		 StackedRealTransform transform = new StackedRealTransform(px, identity);
		 
//		 NLinearInterpolatorFactory<T> interp = new NLinearInterpolatorFactory<T>();
//		 NearestNeighborInterpolatorFactory<T> interp = new NearestNeighborInterpolatorFactory<>();
//		 RealRandomAccessible<T> imgInterp = Views.interpolate( Views.extendZero(img), interp );
//		 RealRandomAccessible<T> imgXfm = new RealTransformRandomAccessible< >( imgInterp, transform );
		 
//		 RealRandomAccess<T> rra = imgXfm.realRandomAccess();
//
//		 rra.setPosition(1, 1);
//		 float[] vals = new float[]{0, 1, 6, 7, 12, 13, 18, 19};
////		 for( double x = -0.2; x < 25; x+= 0.1)
//		 for( int i = 0; i < vals.length; i++ )
//		 {
////			 rra.setPosition(x, 0);
//			 
//			 double x = vals[i];
////			 rra.setPosition(x, 0);
//			 System.out.println( String.format( "i( %f ) = %f", x, rra.get().getRealDouble() ));
//		 }
	}

	public static <T extends NativeType<T> & RealType<T>> void show( 
			final N5Reader zarr, 
			final String baseDataset) throws IOException
	{

		 CachedCellImg<T, ?> img = N5Utils.open(zarr, baseDataset + "/data");
		 RealTransform px = readPositionField1d( zarr, baseDataset + "/xcoordinates");
		 RealTransform identity = new Scale( 1.0 );
		 
		 debugPrintTransform(px);

		 StackedRealTransform transform = new StackedRealTransform(px, identity);
		 
//		 NLinearInterpolatorFactory<T> interp = new NLinearInterpolatorFactory<T>();
		 NearestNeighborInterpolatorFactory<T> interp = new NearestNeighborInterpolatorFactory<>();
		 RealRandomAccessible<T> imgInterp = Views.interpolate( Views.extendZero(img), interp );
		 RealRandomAccessible<T> imgXfm = new RealTransformRandomAccessible< >( imgInterp, transform );
		 
		 FinalInterval itvl = Intervals.createMinMax(0,0,64,64);
		 BdvFunctions.show(imgXfm, itvl, "coord demo");

		 RealRandomAccess<T> rra = imgXfm.realRandomAccess();

		 rra.setPosition(1, 1);
		 float[] vals = new float[]{0, 1, 6, 7, 12, 13, 18, 19};
//		 for( double x = -0.2; x < 25; x+= 0.1)
		 for( int i = 0; i < vals.length; i++ )
		 {
//			 rra.setPosition(x, 0);
			 
			 double x = vals[i];
			 rra.setPosition(x, 0);
			 System.out.println( String.format( "i( %f ) = %f", x, rra.get().getRealDouble() ));
		 }
	}

	public static void generateDataOld( N5ZarrWriter zarr, String baseDataset, Interval itvl ) throws IOException
	{
		final int[] blkSz = new int[]{64, 64};

		if (!zarr.exists(baseDataset))
			zarr.createGroup(baseDataset);

		FunctionRandomAccessible<DoubleType> fimg = new FunctionRandomAccessible<>( 2,
				(p,v) -> { v.set( p.getDoublePosition( 0 )); },
				DoubleType::new );

		IntervalView<DoubleType> img = Views.interval( fimg, itvl );
		
		if( !zarr.datasetExists("/data"))
			N5Utils.save(img, zarr, baseDataset + "/data", blkSz, new GzipCompression());

		if( !zarr.datasetExists("/xcoordinates")) 
		{
			RandomAccessibleInterval<FloatType> xcoords = coordinates();
			N5Utils.save(xcoords, zarr, baseDataset + "/xcoordinates", new int[]{8}, new GzipCompression());
		}
		
		Space[] spaces = new Space[]{
			Common.makeSpace("transformed-space", "space", "um", "x", "y")
		};

		if( spaces != null )
			zarr.setAttribute(baseDataset, "spaces", spaces);

//		if( transforms != null )
//			zarr.setAttribute(dataset, "transforms", transforms);	
	}
	
	public static void generateData( N5ZarrWriter zarr, String baseDataset, Interval itvl ) throws IOException
	{
		final int[] blkSz = new int[]{64, 64};

		if (!zarr.exists(baseDataset))
			zarr.createGroup(baseDataset);

		FunctionRandomAccessible<DoubleType> fimg = new FunctionRandomAccessible<>( 2,
				(p,v) -> { v.set( p.getDoublePosition( 0 )); },
				DoubleType::new );

		IntervalView<DoubleType> img = Views.interval( fimg, itvl );
		
		if( !zarr.datasetExists("/data"))
			N5Utils.save(img, zarr, baseDataset + "/data", blkSz, new GzipCompression());


		Space xy = Common.makeSpace("xyspace", "space", "um", "x", "y");
		Space x = Common.makeSpace("xspace", "space", "um", "x");
		Space[] spaces = new Space[]{ xy, x };

		IdentityCoordinateTransform id = new IdentityCoordinateTransform("id", "", "xspace");

		if( !zarr.datasetExists("/xcoordinates")) 
		{
			RandomAccessibleInterval<FloatType> xcoords = coordinates();
			PositionFieldCoordinateTransform.writePositionFieldTransform(
					zarr, baseDataset + "/xcoordinates", xcoords,
					blkSz, new GzipCompression(), new Space[]{ x }, 
					new CoordinateTransform[]{ id } );
		}

		if( spaces != null )
			zarr.setAttribute(baseDataset, "spaces", spaces);

//		if( transforms != null )
//			zarr.setAttribute(dataset, "transforms", transforms);	
	}
	
	public static RandomAccessibleInterval<FloatType> coordinates()
	{
		float[] vals = new float[]{0, 1, 6, 7, 12, 13, 18, 19};
		ArrayImg<FloatType, FloatArray> coordsRaw = ArrayImgs.floats(vals, 8);
		
		RandomAccessibleInterval<FloatType> pfImgInv = invertPositionField1d( 
				coordsRaw, new ArrayImgFactory<FloatType>( new FloatType() ) );
		
		IterableInterval<FloatType> it = Views.flatIterable( pfImgInv );
		System.out.println( " " );
		it.forEach( System.out::println );
		System.out.println( " " );
		
		return pfImgInv;
	}
	
	public static <T extends RealType<T>, S extends RealType<S>> RandomAccessibleInterval<S> invertPositionField1d( 
			final RandomAccessibleInterval<T> pfieldImg, ImgFactory<S> factory ) {

		Pair<T, T> minMax = minMax(pfieldImg);
		T min = minMax.getA();
		T max = minMax.getB();
		double initPos = (max.getRealDouble() - min.getRealDouble())/2;
		
		Interval itvl = Intervals.createMinMax(
				(long)Math.floor(min.getRealDouble()),
				(long)Math.ceil(max.getRealDouble()));
		System.out.println( "itvl sz: " + Intervals.toString(itvl));


		Img<S> outRaw = factory.create( itvl );
		System.out.println( "outRaw sz: " + Intervals.toString(outRaw));

		IntervalView<S> out = Views.interval( outRaw, itvl );
		System.out.println( "out sz: " + Intervals.toString(out));

		RealRandomAccessible<T> pf = Views.interpolate( Views.extendValue(pfieldImg, 30), 
				new NLinearInterpolatorFactory<>());

		PositionFieldTransform<T> pfx = new PositionFieldTransform<>(pf);
		RealTransformFiniteDerivatives dt = new RealTransformFiniteDerivatives( pfx );
		dt.setStep(0.05);
		InverseRealTransformGradientDescent inverseTransform = new InverseRealTransformGradientDescent( 1, dt );
		inverseTransform.setBeta(0.6);
		inverseTransform.setStepSizeMaxTries(10);
		inverseTransform.setStepSize(0.05);

		inverseTransform.setTolerance(0.01);
		inverseTransform.setMaxIters(2000);
		
		System.out.println(" ");

//		RealPoint init = new RealPoint(1);
		RealPoint qpt = new RealPoint(1);
		
		double[] init = new double[1];
		double[] q = new double[1];
		double[] x = new double[1];

		final Cursor<S> c = out.cursor();
		while( c.hasNext())
		{
			c.fwd();

//			inverseTransform.apply(c, qpt);
//			c.get().setReal( qpt.getDoublePosition(0) );

			c.localize(x);
			init[0] = 0;
			inverseTransform.inverseTol(x, init, 0.01, 1000);
			c.get().setReal( inverseTransform.getEstimate()[0] );

		}

		return out;
	}
	
	public static <T extends RealType<T>> Pair<T,T> minMax(
			final RandomAccessibleInterval<T> pfieldImg ) {

		T min = null;
		T max = null;
		
		Cursor<T> c = Views.flatIterable(pfieldImg).cursor();
		while( c.hasNext())
		{
			c.fwd();
			if( min == null )
			{
				min = c.get().copy();
				max = c.get().copy();
			}
			else
			{
				T v = c.get();
				if( v.compareTo(min) < 0)
					min.set(v);

				if( v.compareTo(max) > 0)
					max.set(v);
			}
		}

		System.out.println( "min: " + min );
		System.out.println( "max: " + max );

		return new ValuePair<>( min, max );
	}

}
