package org.janelia.saalfeldlab.ngff.transforms;

import java.lang.reflect.Type;

import org.janelia.saalfeldlab.n5.N5Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CoordinateTransformAdapter
	implements JsonDeserializer<CoordinateTransform<?>>, JsonSerializer<CoordinateTransform<?>> {
//	implements JsonDeserializer<LinearSpatialTransform> {

	final N5Reader n5;

	public CoordinateTransformAdapter( final N5Reader n5 ) {
		this.n5 = n5;
	}

	@Override
	public CoordinateTransform<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if( !json.isJsonObject() )
			return null;
		
		final JsonObject jobj = json.getAsJsonObject();
		if( !jobj.has("type") )
			return null;

		CoordinateTransform<?> out = null;
		switch( jobj.get("type").getAsString() )
		{
		case("identity"):
			out = context.deserialize( jobj, IdentityCoordinateTransform.class );
			break;
		case("scale"):
			out = context.deserialize( jobj, ScaleCoordinateTransform.class );
			break;
		case("translation"):
			out = context.deserialize( jobj, TranslationCoordinateTransform.class );
			break;
		case("scale_offset"):
			out = context.deserialize( jobj, ScaleOffsetCoordinateTransform.class );
			break;
		case("affine"):
			out = context.deserialize( jobj, AffineCoordinateTransform.class );
			break;
		case("sequence"):
			out = context.deserialize( jobj, SequenceCoordinateTransform.class );
			break;
		case("stacked"):
			out = context.deserialize( jobj, StackedCoordinateTransform.class );
			break;
		}
		/*
		 * Not necessary, since parsers or consuming code are responsible for calling
		 * getParameters and buildTransform
		 */
		//readTransformParameters(out);

		return out;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final CoordinateTransform readTransformParameters( final CoordinateTransform transform ) {

		if( transform instanceof ParametrizedTransform ) {
			ParametrizedTransform pt = (ParametrizedTransform)transform;
			if( pt.getParameterPath() != null ) {
				pt.buildTransform( pt.getParameters(n5));
			}
		}
		return transform;
	}

	@Override
	public JsonElement serialize(CoordinateTransform src, Type typeOfSrc, JsonSerializationContext context) {
		return context.serialize(src);
	}
	
	public static void main( String[] args )
	{
		
		final String affineString = "{"
				+ "\"type\": \"affine\","
				+ "\"affine\" : [ 11.0, 0.0, 0.1, 0.0, 12.0, 0.2 ]"
				+ "}";

		final String scaleString = "{"
				+ "\"type\": \"scale\","
				+ "\"scale\" : [ 11.0, -8.0 ]"
				+ "}";

		final String translationString = "{"
				+ "\"type\": \"translation\","
				+ "\"translation\" : [ -0.9, 2.1 ]"
				+ "}";

		final String idString = "{"
				+ "\"type\": \"identity\""
				+ "}";

		final String seqString = "{"
				+ "\"type\": \"sequence\","
				+ "\"transformations\": [" 
				+ scaleString + "," + translationString 
				+ "]}";
		

		
		final CoordinateTransformAdapter adapter = new CoordinateTransformAdapter( null );

		final GsonBuilder gsonBuilder = new GsonBuilder();
//		gsonBuilder.registerTypeHierarchyAdapter(SpatialTransform.class, adapter );
		gsonBuilder.registerTypeAdapter( CoordinateTransform.class, adapter );
		gsonBuilder.disableHtmlEscaping();

		final Gson gson = gsonBuilder.create();

		CoordinateTransform parsedAffine = gson.fromJson(affineString, CoordinateTransform.class);
		System.out.println( affineString );
		System.out.println( parsedAffine );
		System.out.println( gson.toJson( parsedAffine ));
		System.out.println( " " );

		CoordinateTransform parsedScale = gson.fromJson(scaleString, CoordinateTransform.class);
		System.out.println( scaleString );
		System.out.println( parsedScale );
		System.out.println( gson.toJson( parsedScale));
		System.out.println( " " );

		CoordinateTransform parsedTranslation = gson.fromJson(translationString, CoordinateTransform.class);
		System.out.println( translationString );
		System.out.println( parsedTranslation );
		System.out.println( gson.toJson( parsedTranslation));
		System.out.println( " " );
		
		CoordinateTransform parsedId = gson.fromJson(idString, CoordinateTransform.class);
		System.out.println( idString );
		System.out.println( parsedId );
		System.out.println( gson.toJson( parsedId ));
		System.out.println( " " );

		CoordinateTransform parsedSeq = gson.fromJson(seqString, CoordinateTransform.class);
		System.out.println( seqString );
		System.out.println( parsedSeq );
		System.out.println( gson.toJson( parsedSeq ));
		System.out.println( " " );
		
//		ScaleCoordinateTransform s = new ScaleCoordinateTransform( new double[] {1, 2 });
//		TranslationCoordinateTransform t = new TranslationCoordinateTransform( new double[] {3, 4 });
//		SequenceCoordinateTransform seq = new SequenceCoordinateTransform( new RealCoordinateTransform[] { s, t }, "", "" );
//		
//		System.out.println( gson.toJson(seq) );

	}
}
