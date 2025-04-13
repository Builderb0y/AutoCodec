# Major changes compared to V5:
* Added a hook for data fixers.
	* There are no built-in data fixers, you have to write your own, but the hook is there.
* Added a new Data system to reduce the reliance on DynamicOps for everything, and to make handling of data easier.
	* DynamicOps is still used internally, but implementors of handlers no longer need to think about that.
	* AutoEncoder's now return Data<T_Encoded> instead of T_Encoded directly.
	* AutoDecoder's now use Data<T_Encoded> as their input. Still wrapped in a DecodeContext though.
		* The methods you're used to from DecodeContext like tryAsList(), forceAsBoolean(), etc... are now on a superinterface of DecodeContext.

# Minor changes compared to V5:
* FactoryList is now thread-safe, so handlers can now be requested from the AutoCodec from multiple threads at once.