package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.AffineCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.Views;

public class BasicExample {

	private static final String dataset = "/basic/mri";
	
	public static void main( String[] args ) throws IOException
	{
		final String root = args[ 0 ];

		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		makeData( zarr );

		// Try changing one or both of these to the empty string and see what happens
		BdvOptions opts = BdvOptions.options();
		opts = opts.addTo(show( zarr, dataset, "", opts));
		show( zarr, dataset, "scanner", opts);
		show( zarr, dataset, "LPS", opts);

		zarr.close();
		System.out.println("done");
	}

	@SuppressWarnings("unchecked")
	public static <T extends NativeType<T> & RealType<T>> void makeData( final N5ZarrWriter zarr ) throws IOException
	{
		final String mriDataset = dataset;
		if( zarr.exists(mriDataset))
			return;

		final ImagePlus mriImg = IJ.openImage("https://imagej.nih.gov/ij/images/mri-stack.zip");
		
		AffineTransform3D toPhysical = new AffineTransform3D();
		toPhysical.scale(0.8, 0.8, 2.2);

		AffineTransform3D xfm = new AffineTransform3D();
		xfm.rotate(0, Math.PI / 65);
		xfm.rotate(2, Math.PI / 58);
		xfm.rotate(1, Math.PI / 70);
		Interval interval = Intervals.createMinMax( -5, -5, -5, mriImg.getWidth()+5, mriImg.getHeight() +5, mriImg.getNSlices() + 5);
		double[] toAnatomicalParams = xfm.inverse().getRowPackedCopy();

		AffineTransform3D total = new AffineTransform3D();
		total.preConcatenate(toPhysical);
		total.preConcatenate(xfm);
		total.preConcatenate(toPhysical.inverse());

		final Img<T> img = (Img<T>) ImageJFunctions.wrap(mriImg);
		RandomAccessibleInterval<T> imgXfm = 
				Views.interval( Views.raster( 
					RealViews.affine( 
						Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory<T>()),
						total )),
				interval);

		N5Utils.save(imgXfm, zarr, mriDataset, new int[] {64,64,64}, new GzipCompression());

		final Space[] spaces = new Space[] {
				Common.makeSpace("scanner", "space", "millimeter", "x", "y", "z"),
				Common.makeSpace("LPS", "space", "millimeter", "LR", "AP", "IP")
		};

		final CoordinateTransform[] transforms = new CoordinateTransform[] {
				new ScaleCoordinateTransform( "to-mm", "", "scanner", new double[]{0.8, 0.8, 2.2}),
				new AffineCoordinateTransform( "scanner-to-anatomical", "scanner", "LPS", toAnatomicalParams )
		};

		zarr.setAttribute(mriDataset, "spaces", spaces);
		zarr.setAttribute(mriDataset, "transformations", transforms);
	}

	public static BdvStackSource<?> show( N5ZarrWriter zarr, String dataset, String space, BdvOptions opts ) throws IOException 
	{
		final TransformGraph graph = Common.buildGraph( zarr, dataset );
		final RandomAccessibleIntervalSource<?> src = Common.openSource(zarr, dataset, graph, space );
		final BdvStackSource<?> bdv = BdvFunctions.show(src, opts);
		bdv.setDisplayRangeBounds(0, 255);
		return bdv;
	}
	
}
