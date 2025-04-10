# Major changes compared to V5:
* Added a hook for data fixers.
	* There are no built-in data fixers, you have to write your own, but the hook is there.

# Minor changes compared to V5:
* FactoryList is now thread-safe, so handlers can now be requested from the AutoCodec from multiple threads at once.