package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.bdv.N5Source;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.axes.Axis;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.TranslationCoordinateTransform;

import com.adobe.xmp.impl.Utils;
import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.RandomAccessibleIntervalMipmapSource;
import io.scif.img.converters.RandomAccessConverter;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class GenerateMultiscaleExampleData {
	
	public static void main( String[] args ) throws IOException
	{
		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/multiscale";

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

//		zarr.createGroup(baseDataset);
//		makeDatasets( zarr, baseDataset, new double[]{2,2}, 3 );

		final TransformGraph graph = Common.buildGraph( zarr, baseDataset );
		showMultiscale( zarr, baseDataset, graph );

		System.out.println("done");
	}
	
	public static <T extends NativeType<T> & NumericType<T>> void showMultiscale( 
			N5ZarrWriter zarr, 
			String baseDataset,
			TransformGraph graph) throws IOException
	{
		String[] ls = zarr.list(baseDataset);
		Arrays.sort(ls);
		System.out.println( Arrays.toString(ls));
		
		int N = ls.length;

		final RandomAccessibleInterval[] imgs = new RandomAccessibleInterval[N];
		final AffineTransform3D[] xfms = new AffineTransform3D[N];

		T type = null;
		for( int i = 0; i < N; i++ ) {

			String dset = String.format("%s/%s", baseDataset, ls[i]);
			imgs[i] = Common.open( zarr, dset);

			CoordinateTransform ct = graph.getTransform(String.format("s%dtoPhysical", i)).get();
			xfms[i] = new TransformPath(ct).totalAffine3D();
//			xfms[i] = graph.path("", String.format("s%dtoPhysical", i)).get().totalAffine3D();

			if( i == 0 )
				type = (T) Util.getTypeFromInterval(imgs[i]);
		}
		
		N5Source<T> src = new N5Source<T>(type, baseDataset + "_ms", imgs, xfms);

		BdvFunctions.show(src);
		
	}
	
	public static void makeDatasets( N5ZarrWriter zarr, String baseDataset, double[] factors,
			int N ) throws IOException
	{
		
		final ArrayList<CoordinateTransform> transforms = new ArrayList<>();

		final String spaceName = "multiscale-um";
		Space[] spaces = new Space[]{
			Common.makeSpace(spaceName, "space", "um", "x", "y")
		};

		final int[] blkSz = new int[]{64, 64};
		long[] dims = new long[]{ 128, 128 };

		FunctionRealRandomAccessible<DoubleType> fimg = new FunctionRealRandomAccessible<>( 2,
				(p,v) -> {
					v.set( Math.sqrt(
						p.getDoublePosition(0) * p.getDoublePosition(0) + 
						p.getDoublePosition(1) * p.getDoublePosition(1)) );
				},
				DoubleType::new );
		
		Scale df = new Scale( factors );
		
		final double[] ones = new double[ factors.length ];
		Arrays.fill(ones, 1);

		Scale s = new Scale( ones );
		String dataset = "";
		
		for( int i = 0; i < N; i++ )
		{
			dims[0] /= factors[0];
			dims[1] /= factors[1];

			String si = String.format("s%d", i);
			dataset = String.format("%s/s%d", baseDataset, i);
			IntervalView<DoubleType> img = Views.interval(
				Views.raster( RealViews.affine(fimg, s.inverse())),
				new FinalInterval( dims ));

			transforms.add(
					new ScaleCoordinateTransform(si+"toPhysical", "", spaceName, s.getScaleCopy()));

			s.preConcatenate(df);

			N5Utils.save(img, zarr, dataset, blkSz, new GzipCompression());
		}

		if( spaces != null )
			zarr.setAttribute(baseDataset, "spaces", spaces);

		if( transforms != null )
			zarr.setAttribute(baseDataset, "transformations", transforms);
	}

}
