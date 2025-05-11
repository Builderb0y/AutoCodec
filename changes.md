# Major changes compared to V5:
* Added a hook for data fixers.
	* There are no built-in data fixers, you have to write your own, but the hook is there.
* Added a new Data system to reduce the reliance on DynamicOps for everything, and to make handling of data easier.
	* AutoEncoder's now return Data instead of T_Encoded.
	* AutoDecoder's now use Data as their input. Still wrapped in a DecodeContext though.
		* The methods you're used to from DecodeContext like tryAsList(), createString(), etc... are now on a superinterface of DecodeContext.
	* DecodeContext.input has been renamed to DecodeContext.data, and is now located on the super class.
		* Likewise, DecodeContext.input(T_Encoded) has been moved to AbstractDecodeContext.withData(Data).
		* Also DecodeContext.input(T_Encoded, DecodePath) became AbstractDecodeContext.fork(DecodePath, Data).

# Minor changes compared to V5:
* FactoryList is now thread-safe, so handlers can now be requested from the AutoCodec from multiple threads at once.
* Added a new ForceOrdinal annotation to tweak the ordinal vs. name policy for enum coding.