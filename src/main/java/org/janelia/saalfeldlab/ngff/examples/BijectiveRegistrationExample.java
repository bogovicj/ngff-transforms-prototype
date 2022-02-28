package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.ArrayList;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.ij.N5Factory;
import org.janelia.saalfeldlab.n5.imglib2.N5DisplacementField;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Interpolation;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class BijectiveRegistrationExample {
	
//	private String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
//	private String root = "/groups/saalfeld/home/bogovicj/projects/ngff/transformsExamples/data.zarr";
	private String root = "/home/john/projects/ngff/transformsExamples/data.zarr";

//	private String transformH5Path = "/groups/saalfeld/public/jrc2018/small_sample_data/JRC2018F_JFRC2010_small.h5";
//	private String transformH5Path = "/groups/saalfeld/public/jrc2018/small_sample_data/JRC2018F_FCWB_small.h5";
	private String transformH5Path = "/home/john/projects/jrc2018/small_test_data/JRC2018F_FCWB_small.h5";

//	private String targetPath = "/groups/saalfeld/public/jrc2018/small_sample_data/JRC2018_FEMALE_small.tif";
	private String targetPath = "/home/john/projects/jrc2018/small_test_data/JRC2018_FEMALE_small.tif";

//	private String movingPath = "/groups/saalfeld/public/jrc2018/small_sample_data/JFRC2010_small.tif";

//	private String fcwbPath = "/groups/saalfeld/public/jrc2018/small_sample_data/FCWB_small.tif";
	private String fcwbPath = "/home/john/projects/jrc2018/small_test_data/FCWB_small.tif";

	private String baseDataset = "/registration";
	private N5ZarrWriter zarr;
	private Spaces spaces;
	private ArrayList<CoordinateTransform<?>> transforms;

	public static void main(String[] args) throws Exception {
		new BijectiveRegistrationExample().run();
	}
	
	public BijectiveRegistrationExample()
	{
		spaces = new Spaces();
		transforms = new ArrayList<>();
	}

	public void run() throws Exception
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		zarr = new N5ZarrWriter(root, gsonBuilder );
		
//		makeData();
		makeTransform();
		
//		String mvgDataset = baseDataset + "/jrc2010";
		String tgtDataset = baseDataset + "/jrc2018F";
		String mvgDataset = baseDataset + "/fcwb";
//
//		TransformGraph mgraph = Common.buildGraph(zarr, mvgDataset );
//		TransformGraph tgraph = Common.buildGraph(zarr, tgtDataset );
//		
//////		System.out.println( graph.allPaths("").size());
////
//		N5Reader h5 = new N5Factory().openReader(transformH5Path);
//		RealTransform fwdxfm = N5DisplacementField.open(h5, "dfield", false);
//		RealTransform invxfm = N5DisplacementField.open(h5, "invdfield", false);
//
////		RandomAccessibleIntervalSource mvgSrc = Common.openSource(zarr, mvgDataset, mgraph, "jrc2010");
//		RandomAccessibleIntervalSource mvgSrc = Common.openSource(zarr, mvgDataset, mgraph, "fcwb");
////		RealRandomAccessible mvgInterp = mvgSrc.getInterpolatedSource(0, 0, Interpolation.NLINEAR);
////		RealRandomAccessible mvgInterpReg = new RealTransformRandomAccessible(mvgInterp, fwdxfm );
////		RealRandomAccessible mvgInterpReg = new RealTransformRandomAccessible(mvgInterp, invxfm );
//
//		RandomAccessibleIntervalSource tgtSrc = Common.openSource(zarr, tgtDataset, tgraph, "jrc2018F");
////		RealRandomAccessible tgtInterp = tgtSrc.getInterpolatedSource(0, 0, Interpolation.NLINEAR);
//
////		RealRandomAccessible mvgInterpReg = new RealTransformRandomAccessible<>( Common.rra(mvgSrc), fwdxfm );
//
//		AffineTransform3D pixToPhys = new AffineTransform3D();
//		mvgSrc.getSourceTransform(0, 0, pixToPhys);
//
//		RealTransformSequence seq = new RealTransformSequence();
//		seq.add( fwdxfm );
//		seq.add( pixToPhys.inverse() );
//
//		RealRandomAccessible rra = mvgSrc.getInterpolatedSource(0, 0, Interpolation.NLINEAR);
//		RealRandomAccessible mvgInterpReg = new RealTransformRandomAccessible( rra, seq );
//
////		BdvFunctions.show(mvgSrc);
//		BdvStackSource bdv = BdvFunctions.show( mvgSrc );
//		BdvOptions opts = BdvOptions.options().addTo( bdv );
//		BdvFunctions.show(tgtSrc , opts);
//		BdvFunctions.show(mvgInterpReg, tgtSrc.getSource(0, 0), "fcwb-reg", opts);
	}

	public void makeTransform() throws Exception
	{
		FloatType type = new FloatType();
		N5Reader h5 = new N5Factory().openReader(transformH5Path);

		int[] dfieldBlk = new int[]{ 3, 64, 64, 64 };
		final GzipCompression compression = new GzipCompression();

		Space space = Common.makeDfieldSpace("forwardDfield", "space", "um", "fwd-x", "fwd-y", "fwd-z");
		RandomAccessibleInterval<FloatType> dfield = N5DisplacementField.openField(h5, "dfield", type );


		RandomAccessibleInterval<FloatType> dfieldP = N5DisplacementField.vectorAxisFirst(dfield);
		System.out.println( "dfield sz: " + Intervals.toString( dfield ));
		System.out.println( "dfield P sz: " + Intervals.toString( dfieldP ));

		String fwdName = "fwdDfield";
		double[] fwdScale = h5.getAttribute("dfield", "spacing", double[].class);

		final String fwdDfieldDataset = baseDataset + "/fwdDfield";
		N5Utils.save(dfieldP, zarr, fwdDfieldDataset, dfieldBlk, compression);
		zarr.setAttribute(fwdDfieldDataset, "spaces", new Space[]{ space });
		zarr.setAttribute(fwdDfieldDataset, "transformations", new CoordinateTransform[]{ 
				new ScaleCoordinateTransform("fwdDfieldScale", "", fwdName, fwdScale ) });
	}

	public void makeData() throws IOException
	{
		if( ! zarr.groupExists(baseDataset) ) {
			zarr.createGroup(baseDataset);
		}

//		ImagePlus mvg = IJ.openImage(movingPath);
//		toN5( "jrc2010", mvg );
//
		ImagePlus tgt = IJ.openImage(targetPath);
		toN5( "jrc2018F", tgt);
		
		ImagePlus fcwb = IJ.openImage(fcwbPath);
		toN5( "fcwb", fcwb);
	}
	
	public <T extends RealType<T> & NativeType<T>> void toN5(final String name, final ImagePlus imp  ) throws IOException
	{
		final int[] blkSize = new int[] {64,64,64};
		final GzipCompression compression = new GzipCompression();

		final Img<T> img = ImageJFunctions.wrap(imp);
		final String dataset = String.format("%s/%s", baseDataset, name );
		N5Utils.save(img, zarr, dataset, blkSize, compression);
		
		Space space = Common.makeSpace(name, "space", "um", name+"-x", name+"-y", name+"-z");
		spaces.add(space);

		double[] s = new double[]{ 
				imp.getCalibration().pixelWidth,
				imp.getCalibration().pixelHeight,
				imp.getCalibration().pixelDepth };

		ScaleCoordinateTransform scale = new ScaleCoordinateTransform("to-" + name, "", name, s );
		transforms.add( scale );
		
		zarr.setAttribute(dataset, "spaces", new Space[]{ space });
		zarr.setAttribute(dataset, "transformations", new CoordinateTransform[]{ scale });
	}
}
