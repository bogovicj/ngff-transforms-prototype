package org.janelia.saalfeldlab.ngff.transforms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.janelia.saalfeldlab.ngff.axes.Axis;
import org.janelia.saalfeldlab.ngff.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.examples.Common;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.junit.Test;

public class SpacesTests {
	
	@Test
	public void axisSetTests()
	{
		final Space a = Common.makeSpace("in", "space", "um", "0", "1", "2");
		final Space b = Common.makeSpace("in", "space", "um", "a", "b", "1");

		Space aIb = a.intersection("AintersectB", b);
		System.out.println( aIb.numDimensions() );
		System.out.println( Arrays.toString( aIb.getAxisLabels() ));
		
		Space aDb = a.diff("AdiffB", b);
		System.out.println( aDb.numDimensions() );
		System.out.println( Arrays.toString( aDb.getAxisLabels() ));

		Space bDa = b.diff("BdiffA", a);
		System.out.println( bDa.numDimensions() );
		System.out.println( Arrays.toString( bDa.getAxisLabels() ));
		
	}

	@Test
	public void subSuperSpacesTests()
	{
		final Space in = Common.makeSpace("in", "space", "um", "0", "1");
		final Space out = Common.makeSpace("out", "space", "um", "x", "y");

		final Space x = Common.makeSpace("x", "space", "um", "x");
		final Space y = Common.makeSpace("y", "space", "um", "y");

		assertTrue( in.isSubspaceOf(in));
		assertTrue( in.isSuperspaceOf(in));
		assertTrue( in.axesEquals(in));
		assertTrue( out.isSubspaceOf(out));
		assertTrue( out.isSuperspaceOf(out));
		assertTrue( out.axesEquals(out));

		assertFalse( in.isSubspaceOf(out));
		assertFalse( in.isSuperspaceOf(out));
		assertFalse( in.axesEquals(out));
		assertFalse( out.isSubspaceOf(in));
		assertFalse( out.isSuperspaceOf(in));
		assertFalse( out.axesEquals(in));
		
		assertTrue( out.isSuperspaceOf(x));
		assertTrue( out.isSuperspaceOf(y));
		assertTrue( x.isSubspaceOf(out));
		assertTrue( y.isSubspaceOf(out));
		
		assertFalse( in.isSuperspaceOf(x));
		assertFalse( in.isSuperspaceOf(y));
		assertFalse( x.isSubspaceOf(in));
		assertFalse( y.isSubspaceOf(in));
		assertFalse( x.isSubspaceOf(y));
		assertFalse( y.isSubspaceOf(x));
		
		final Space inx = out.subSpace("", "x");
		assertTrue( inx.axesEquals(x) );

	}

}
