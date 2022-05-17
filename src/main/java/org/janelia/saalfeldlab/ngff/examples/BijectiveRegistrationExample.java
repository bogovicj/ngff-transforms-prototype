package org.janelia.saalfeldlab.ngff.examples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.ij.N5Factory;
import org.janelia.saalfeldlab.n5.imglib2.N5DisplacementField;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.AffineCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.BijectionCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.DisplacementFieldCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.ParametrizedTransform;
import org.janelia.saalfeldlab.ngff.transforms.RealCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.SequenceCoordinateTransform;
import org.janelia.saalfeldlab.ngff.vis.Vis;

import com.google.gson.GsonBuilder;

import bdv.img.WarpedSource;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Interpolation;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.DeformationFieldTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class BijectiveRegistrationExample {
	
//	private String srcDir = "/home/john/projects/jrc2018/small_test_data/";
//	private String srcDir = "/groups/saalfeld/public/jrc2018/small_sample_data/";

//	private String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
//	private String root = "/groups/saalfeld/home/bogovicj/projects/ngff/transformsExamples/data.zarr";

	private String root;
	private String srcDir;

	private String transformH5Path;
	private String targetPath;
	private String fcwbPath;
	private String ptsPath;

	private String baseDataset = "/registration";
	private N5ZarrWriter zarr;
	private Spaces spaces;
	private ArrayList<CoordinateTransform<?>> transforms;

	public static void main(String[] args) throws Exception {
		new BijectiveRegistrationExample( args[0], args[1]).run();
	}

	public BijectiveRegistrationExample( String root, String srcDir ) {
		spaces = new Spaces();
		transforms = new ArrayList<>();

		this.root = root + File.separator;
		this.srcDir = srcDir;

		transformH5Path = srcDir + File.separator + "JRC2018F_FCWB_small.h5";
		targetPath = srcDir + File.separator + "JRC2018_FEMALE_small.tif";
		fcwbPath = srcDir + File.separator + "FCWB_small.tif";
		ptsPath = srcDir + File.separator + "GadMARCM-F000122_seg001_03.swc";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() throws Exception
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		zarr = new N5ZarrWriter(root, gsonBuilder );

		makeData();
		makeTransform();
//		makePoints();

		final String tgtDataset = baseDataset + "/jrc2018F";
		final String mvgDataset = baseDataset + "/fcwb";
//		final String space = "jrc2018F"; // try changing to "fcwb", say
		final String space = "fcwb"; // try chaning to "fcwb", say

		Vis vis = new Vis( zarr ).addTransforms(baseDataset);
		BdvStackSource<?> bdv = vis.dataset(tgtDataset).space(space).show();
		vis.bdvOptions(BdvOptions.options().addTo(bdv)).dataset(mvgDataset).show();

		System.out.println("run complete");
	}

	public void makePoints() throws IOException
	{
		final double[] vals = Files.lines(Paths.get(ptsPath)).filter( x -> { return !x.startsWith("#"); })
			.flatMapToDouble( x -> {
				return Arrays.stream( x.split(" ") ).mapToDouble(Double::parseDouble);
			}).toArray();

		final ArrayImg<DoubleType, DoubleArray> data = ArrayImgs.doubles(vals, 7, vals.length / 7);
		final IntervalView<DoubleType> pts = Views.interval(data, Intervals.createMinMax( 2, 0, 4, data.max(1)));
		N5Utils.save(pts, zarr, baseDataset + "/pts", new int[]{3,1028}, new GzipCompression());

	}

	/**
	 * Writes the displacement fields, returns the forward affine
	 * @return 
	 * 
	 * @throws Exception
	 */
	public AffineTransform3D makeTransform() throws Exception
	{
		final FloatType type = new FloatType();
		final N5Reader h5 = new N5Factory().openReader(transformH5Path);

		final RandomAccessibleInterval<FloatType> dfield = N5DisplacementField.openField(h5, "dfield", type );
		final RandomAccessibleInterval<FloatType> dfieldP = N5DisplacementField.vectorAxisFirst(dfield);
		final double[] fwdScale = h5.getAttribute("dfield", "spacing", double[].class);
		writeDfield( dfieldP, fwdScale, "fwdDfield");

		final RandomAccessibleInterval<FloatType> invdfield = N5DisplacementField.openField(h5, "invdfield", type );
		final RandomAccessibleInterval<FloatType> invdfieldP = N5DisplacementField.vectorAxisFirst(invdfield);
		final double[] invScale = h5.getAttribute("invdfield", "spacing", double[].class);
		writeDfield( invdfieldP, invScale, "invDfield");

		final double[] params = h5.getAttribute("dfield", "affine", double[].class );
		final AffineTransform3D affine = new AffineTransform3D();
		affine.set(params);

		final SequenceCoordinateTransform fwdTransform = new SequenceCoordinateTransform("jrc2018F-to-fcwb", "jrc2018F", "fcwb", 
				new RealCoordinateTransform[] {
					new DisplacementFieldCoordinateTransform(null, baseDataset + "/fwdDfield" ),
					new AffineCoordinateTransform( params )
				});

		final SequenceCoordinateTransform invTransform = new SequenceCoordinateTransform("fcwb-to-jrc2018F", "fcwb", "jrc2018F",
				new RealCoordinateTransform[] {
					new AffineCoordinateTransform( affine.inverse().getRowPackedCopy() ),
					new DisplacementFieldCoordinateTransform(null, baseDataset + "/invDfield" )
				});

		transforms.add( fwdTransform );
		transforms.add( invTransform );

		zarr.setAttribute(baseDataset, "coordinateSystems", spaces.spaces().toArray( Space[]::new ));

		BijectionCoordinateTransform bct = new BijectionCoordinateTransform("jrc2018F<>fcwb", "jrc2018F", "fcwb", fwdTransform, invTransform);
		zarr.setAttribute(baseDataset, "coordinateTransformations", new CoordinateTransform[]{ bct } );

		return affine;
	}
	
	public <T extends RealType<T> & NativeType<T>> void writeDfield( RandomAccessibleInterval<T> dfield, double[] scale, String name ) throws IOException
	{
		int[] dfieldBlk = new int[]{ 3, 64, 64, 64 };
		final GzipCompression compression = new GzipCompression();

		Space space = Common.makeDfieldSpace("forwardDfield", "space", "um", "fwd-x", "fwd-y", "fwd-z");

		final String fwdDfieldDataset = baseDataset + "/" + name;
		N5Utils.save(dfield, zarr, fwdDfieldDataset, dfieldBlk, compression);
		zarr.setAttribute(fwdDfieldDataset, "spaces", new Space[]{ space });
		zarr.setAttribute(fwdDfieldDataset, "transformations", new CoordinateTransform[]{ 
				new ScaleCoordinateTransform("fwdDfieldScale", "", name, scale ) });
	}

	public void makeData() throws IOException
	{
		if( ! zarr.groupExists(baseDataset) ) {
			zarr.createGroup(baseDataset);
		}

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

		ScaleCoordinateTransform scale = new ScaleCoordinateTransform("to-" + name, dataset, name, s );
		transforms.add( scale );
		
		zarr.setAttribute(dataset, "coordinateSystems", new Space[]{ space });
		zarr.setAttribute(dataset, "coordinateTransformations", new CoordinateTransform[]{ scale });
	}
	
	
	public static void old() {
//		final CoordinateTransform[] transforms = zarr.getAttribute(baseDataset, "transformations", CoordinateTransform[].class);
//		System.out.println( transforms );
//		SequenceCoordinateTransform seq = (SequenceCoordinateTransform)transforms[0];
//		DisplacementFieldCoordinateTransform df = (DisplacementFieldCoordinateTransform) seq.getTransformations()[0];
//		System.out.println( df );
////		DeformationFieldTransform xfm = df.getTransform();
////		System.out.println( xfm );
//		
//		System.out.println( "is pt? " + ( df instanceof ParametrizedTransform ));
//		
//		DeformationFieldTransform xfm = (DeformationFieldTransform) df.getTransform(zarr);
//		System.out.println( xfm );

//		TransformGraph reggraph = Common.buildGraph( zarr, baseDataset );
//		reggraph.getTransforms().stream().forEach(System.out::println);
//		System.out.println( reggraph.path("fcwb", "jrc2018F").get());
		
//		System.out.println( "" );
//		final TransformGraph graph = Common.buildGraph(zarr, baseDataset, mvgDataset );	
//		System.out.println( graph.getSpaces().getSpace(""));
//		graph.getTransforms().stream().forEach(System.out::println);
//		System.out.println( graph.path("", "jrc2018F").get());
//		System.out.println( "" );
//		System.out.println( graph.path("jrc2018F","").get());
//
//
//		TransformPath p = graph.path("jrc2018F","").get();
//		System.out.println( p );
//
//		System.out.println( "" );
//		List<CoordinateTransform<?>> ts = p.flatTransforms();
//		ts.stream().forEach( System.out::println );
//		
//		System.out.println( "" );
//		for( CoordinateTransform<?> t : ts )
//		{
//			System.out.println( t.getName());
//			System.out.println( t.getTransform(zarr));
//		}
		
		
//		RandomAccessibleIntervalSource mvgSrc = Common.openSource(zarr, mvgDataset, "fcwb");
//		RandomAccessibleIntervalSource tgtSrc = Common.openSource(zarr, tgtDataset, "jrc2018F");
//
////		RealRandomAccessible mvgInterp = mvgSrc.getInterpolatedSource(0, 0, Interpolation.NLINEAR);
////		RealRandomAccessible mvgInterpReg = new RealTransformRandomAccessible(mvgInterp, fwdxfm );
////		RealRandomAccessible mvgInterpReg = new RealTransformRandomAccessible(mvgInterp, invxfm );
//
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
}
