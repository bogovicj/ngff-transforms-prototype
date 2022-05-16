package net.imglib2.realtransform;

import java.util.Arrays;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

public class SubsetRealTransform implements RealTransform
{
	protected int numSourceDimensions;

	protected int numTargetDimensions;

	private final RealTransform transform;

	protected int[] srcComponents;

	protected int[] tgtComponents;

	protected int[] tgtDst;
	
	protected double[] srcPt;

	protected double[] tgtPt;

	public SubsetRealTransform( 
			final RealTransform transform, final int[] components ) {
		this.transform = transform;
		this.srcComponents = components;
		this.tgtComponents = components;
		this.numSourceDimensions = components.length;
		this.numTargetDimensions = components.length;
		srcPt = new double[ transform.numSourceDimensions() ];
		tgtPt = new double[ transform.numTargetDimensions() ];
		tgtPt = srcPt;
	}
	
	public SubsetRealTransform( 
			final RealTransform transform, final int[] srcComponents, final int[] tgtComponents ) {
		this.transform = transform;
		this.srcComponents = srcComponents;
		this.tgtComponents = tgtComponents;
		this.numSourceDimensions = Arrays.stream( srcComponents ).max().getAsInt() + 1;
		this.numTargetDimensions = (int)Arrays.stream( tgtComponents ).filter( x -> x >= 0 ).count();
		srcPt = new double[ transform.numSourceDimensions() ];
		tgtPt = new double[ transform.numTargetDimensions() ];
	}


	@Override
	public void apply(double[] source, double[] target) {
		assert source.length >= numTargetDimensions;
		assert target.length >= numTargetDimensions;
		
		System.arraycopy( source, 0, target, 0, target.length );
		for ( int d = 0; d < srcPt.length; ++d )
			srcPt[ d ] = source[ srcComponents[ d ] ];

		transform.apply( srcPt, tgtPt );

		for ( int d = 0; d < numTargetDimensions; ++d )
			if( tgtComponents[ d ] >= 0 )
				target[ tgtComponents[ d ] ] = tgtPt[ d ];
	}

	@Override
	public void apply(RealLocalizable source, RealPositionable target) {
		assert source.numDimensions() >= numTargetDimensions;
		assert target.numDimensions() >= numTargetDimensions;

		target.setPosition( source );
		for ( int d = 0; d < srcPt.length; ++d )
			srcPt[ d ] = source.getDoublePosition( srcComponents[ d ] );

		transform.apply( srcPt, tgtPt );

		for ( int d = 0; d < transform.numTargetDimensions(); ++d )
			if( tgtComponents[ d ] >= 0 )
				target.setPosition( tgtPt[ d ], tgtComponents[ d ]  );
	}

	@Override
	public SubsetRealTransform copy() {
		if( srcComponents == tgtComponents)
			return new SubsetRealTransform( transform, srcComponents );
		else
			return new SubsetRealTransform( transform, srcComponents, tgtComponents );
	}

	@Override
	public int numSourceDimensions() {
		return numSourceDimensions;
	}

	@Override
	public int numTargetDimensions() {
		return numTargetDimensions;
	}

}
