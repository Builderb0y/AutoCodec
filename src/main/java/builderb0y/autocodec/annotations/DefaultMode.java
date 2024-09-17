package builderb0y.autocodec.annotations;

/**
used by various @Default<Type> annotations
to specify whether the default value should
be treated as an encoded input to be decoded, or
a decoded output which needs no further processing.
*/
public enum DefaultMode {
	/**
	the specified value should be treated as an input to be decoded.
	*/
	ENCODED,
	/**
	the specified value should be treated as an
	output which needs no further processing.
	*/
	DECODED;
}