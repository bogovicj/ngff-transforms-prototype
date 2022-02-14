package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.Arrays;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.bdv.N5Source;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
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
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class CoordinateListExample {
	
	public static void main( String[] args ) throws IOException
	{
		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/coordinates";	
		
		FinalInterval itvl = new FinalInterval( 128, 128 );
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

//		generateData( zarr, baseDataset, itvl );
		
		show(zarr, baseDataset );

		System.out.println( "done" );
	}
	
	public static <T extends RealType<T> & NativeType<T>> RealTransform readPositionField1d( final N5Reader n5, final String dataset ) throws IOException
	{
		final CachedCellImg<T, ?> pfimg = N5Utils.open(n5, dataset);
		return new PositionFieldTransform<>(
				Views.interpolate( Views.extendValue(pfimg, -1),
					new NLinearInterpolatorFactory<T>()));	
	}

	public static <T extends NativeType<T> & NumericType<T>> void show( 
			final N5Reader zarr, 
			final String baseDataset) throws IOException
	{

		 CachedCellImg<T, ?> img = N5Utils.open(zarr, baseDataset + "/data");
		 RealTransform px = readPositionField1d( zarr, baseDataset + "/xcoordinates");
		 RealTransform identity = new Scale( 1.0 );

		 StackedRealTransform transform = new StackedRealTransform(px, identity);
		 
		 NLinearInterpolatorFactory<T> interp = new NLinearInterpolatorFactory<T>();
//		 NearestNeighborInterpolatorFactory<T> interp = new NearestNeighborInterpolatorFactory<>();
		 RealRandomAccessible<T> imgInterp = Views.interpolate( Views.extendZero(img), interp );
		 RealRandomAccessible<T> imgXfm = new RealTransformRandomAccessible< >( imgInterp, transform );
		 
		 FinalInterval itvl = Intervals.createMinMax(0,0,256,256);
		 BdvFunctions.show(imgXfm, itvl, "coord demo");

	}
	
	public static void generateData( N5ZarrWriter zarr, String baseDataset, Interval itvl ) throws IOException
	{
		final int[] blkSz = new int[]{64, 64};

		zarr.createGroup(baseDataset);

		FunctionRandomAccessible<DoubleType> fimg = new FunctionRandomAccessible<>( 2,
				(p,v) -> {
					double r = Math.sqrt(
						p.getDoublePosition(0) * p.getDoublePosition(0) + 
						p.getDoublePosition(1) * p.getDoublePosition(1));
					v.set( Math.cos( 0.2 * r ));
				},
				DoubleType::new );

		IntervalView<DoubleType> img = Views.interval( fimg, itvl );
		N5Utils.save(img, zarr, baseDataset + "/data", blkSz, new GzipCompression());
		
		
		RandomAccessibleInterval<FloatType> xcoords = coordinates( img.dimension(0));
		N5Utils.save(xcoords, zarr, baseDataset + "/xcoordinates", new int[]{128}, new GzipCompression());
		
		Space[] spaces = new Space[]{
			Common.makeSpace("transformed-space", "space", "um", "x", "y")
		};

		if( spaces != null )
			zarr.setAttribute(baseDataset, "spaces", spaces);

//		if( transforms != null )
//			zarr.setAttribute(dataset, "transforms", transforms);	
	}
	
	public static RandomAccessibleInterval<FloatType> coordinates( long N )
	{
		ArrayImg<FloatType, FloatArray> img = ArrayImgs.floats(N);
		boolean b = true;
		float v = 0.0f;

		ArrayCursor<FloatType> c = img.cursor();
		while( c.hasNext() ) {
			c.next().set( v );
			if( b )
				v+=5;
			else
				v++;

			b = !b;
		}

		return img;
	}

}
