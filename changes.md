# Changes compared to V4:

## Re-structured how coders and codecs are created.

In older versions, several parts of logic required dealing with encoders and decoders separately.
* If you already have an AutoCoder for some type, and you wanted to add it to your AutoCodec instance, you would need to register it twice in the LookupEncoderFactory and the LookupDecoderFactory.
* If you implemented AutoCoder, you would quite often need 2 separate factories to supply it as an AutoEncoder, and as an AutoDecoder.

In this new version, coders now have first-class support for their creation.
* There are new classes like CoderFactory, CoderFactoryList, LookupCoderFactory, etc.
* Coders can be created and registered in one action instead of two.
* The old system is still supported too, but only as a last resort. If the direct creation of a coder fails, the next step is to attempt to create an encoder/decoder pair, and combine them.
	* This is also controlled by a factory, and therefore this behavior can be customized.

### Verification

Only AutoCoder's support verification now. Or at least, this is now the default behavior. Creating an AutoDecoder no longer guarantees that it'll have an AutoVerifier attached to it.

## Additions

## Removals and deprecations

* Methods on AutoCodec to create AutoEncoder's and AutoDecoder's are now annotated as TestOnly. AutoCoder extends both AutoEncoder and AutoDecoder, and can therefore be used as a substitute for both.
* Removed the ability to create DFU Encoder's and Decoder's directly. Codec extends both Encoder and Decoder, and can therefore be used as a substitute for both.

## Other changes