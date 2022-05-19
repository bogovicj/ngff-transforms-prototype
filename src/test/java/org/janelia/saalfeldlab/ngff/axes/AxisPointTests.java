package org.janelia.saalfeldlab.ngff.axes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class AxisPointTests
{

	@Test
	public void testSimple()
	{

		AxisPoint p = new AxisPoint( "x", "y" );
		p.setPosition( 1, "x" );
		p.setPosition( 2, "y" );

		assertEquals( 1, p.getDoublePosition( 0 ), 1e-9 );
		assertEquals( 2, p.getDoublePosition( 1 ), 1e-9 );

		assertEquals( 1, p.getDoublePosition( "x" ), 1e-9 );
		assertEquals( 2, p.getDoublePosition( "y" ), 1e-9 );
		
		
		// treat y as the zero index and x as the first index
		p.setAxisOrder( "y", "x" );
		
		// values at indexes change
		assertEquals( 2, p.getDoublePosition( 0 ), 1e-9 );
		assertEquals( 1, p.getDoublePosition( 1 ), 1e-9 );

		// getting position by name doesn't change
		assertEquals( 1, p.getDoublePosition( "x" ), 1e-9 );
		assertEquals( 2, p.getDoublePosition( "y" ), 1e-9 );

		// for some fun
		p.setAxisOrder( "x", "x", "y", "y" );
		assertEquals( 1, p.getDoublePosition( 0 ), 1e-9 );
		assertEquals( 1, p.getDoublePosition( 1 ), 1e-9 );
		assertEquals( 2, p.getDoublePosition( 2 ), 1e-9 );
		assertEquals( 2, p.getDoublePosition( 3 ), 1e-9 );
	}
	
	@Test
	public void testAddAndTrim()
	{
		AxisPoint p = new AxisPoint( );
		p.setPosition( 0, "a" );
		p.setPosition( 1, "b" );
		p.setPosition( 2, "c" );
		p.setPosition( 3, "d" );
		p.setPosition( 4, "e" );
		p.expand();

		assertEquals( 5, p.numDimensions() );

		p.setAxisOrder( "d", "c" );
		assertEquals( 2, p.numDimensions() );
		
		// but can still get other axes
		assertEquals( 4, p.getDoublePosition( "e" ), 1e-9 );
		
		p.trim();
		assertEquals( 2, p.numDimensions() );
		
		// can't get other axes after trim
		assertThrows( NullPointerException.class, () -> p.getDoublePosition( "e" ) );

	}

}
