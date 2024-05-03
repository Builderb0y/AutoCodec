package builderb0y.autocodec;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.locks.ReentrantLock;

import com.mojang.serialization.*;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.constructors.ConstructorFactoryList;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeContext.RootDecodePath;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.decoders.DecoderFactoryList;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.encoders.EncoderFactoryList;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.imprinters.ImprinterFactoryList;
import builderb0y.autocodec.integration.*;
import builderb0y.autocodec.logging.Printer;
import builderb0y.autocodec.logging.StackContextLogger;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.ReflectionManager;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifierFactoryList;
import builderb0y.autocodec.verifiers.VerifyException;

/**
the main class responsible for creating Codec's.
simply construct an instance of this class
(and preferably store it in a static final field),
then call {@link #createDFUCodec(Class)}
or {@link #createDFUCodec(ReifiedType)}
on it to create a Codec for that type.
*/
public class AutoCodec implements ReflectContextProvider {

	public final @NotNull TaskLogger factoryLogger, encodeLogger, decodeLogger;

	public final @NotNull ReflectionManager reflectionManager;

	public final @NotNull     EncoderFactoryList encoders;
	public final @NotNull ConstructorFactoryList constructors;
	public final @NotNull   ImprinterFactoryList imprinters;
	public final @NotNull     DecoderFactoryList decoders;
	public final @NotNull    VerifierFactoryList verifiers;

	public AutoCodec() {
		ReentrantLock lock     = new ReentrantLock();
		this.factoryLogger     = this.createFactoryLogger(lock);
		this.encodeLogger      = this.createEncodeLogger(lock);
		this.decodeLogger      = this.createDecodeLogger(lock);

		this.reflectionManager = this.createReflectionManager();

		this.encoders          = this.createEncoders();
		this.constructors      = this.createConstructors();
		this.imprinters        = this.createImprinters();
		this.decoders          = this.createDecoders();
		this.verifiers         = this.createVerifiers();
	}



	//////////////////////////////// DFU integration ////////////////////////////////



	//////////////// codecs ////////////////

	/** creates a DFU {@link Codec} which can encode and decode instances of the given class. */
	public <T_Decoded> @NotNull Codec<T_Decoded> createDFUCodec(@NotNull Class<T_Decoded> clazz) {
		return Auto2DFUCodec.of(this, this.createCoder(clazz));
	}

	/** creates a DFU {@link Codec} which can encode and decode instances of the given type. */
	public <T_Decoded> @NotNull Codec<T_Decoded> createDFUCodec(@NotNull ReifiedType<T_Decoded> type) {
		return Auto2DFUCodec.of(this, this.createCoder(type));
	}

	/** creates a DFU {@link Codec} which delegates to the provided {@link AutoEncoder} and {@link AutoDecoder}. */
	public <T_Decoded> @NotNull Codec<T_Decoded> createDFUCodec(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		return Auto2DFUCodec.of(this, encoder, decoder);
	}

	/** creates a DFU {@link Codec} which delegates to the provided {@link AutoCoder}. */
	public <T_Decoded> @NotNull Codec<T_Decoded> createDFUCodec(@NotNull AutoCoder<T_Decoded> both) {
		return Auto2DFUCodec.of(this, both);
	}

	public <T_Decoded> @NotNull AutoCoder<T_Decoded> wrapDFUCodec(@NotNull Encoder<T_Decoded> encoder, @NotNull Decoder<T_Decoded> decoder, boolean allowPartial) {
		return DFU2AutoCodec.of(encoder, decoder, allowPartial);
	}

	public <T_Decoded> @NotNull AutoCoder<T_Decoded> wrapDFUCodec(@NotNull Codec<T_Decoded> codec, boolean allowPartial) {
		return DFU2AutoCodec.of(codec, allowPartial);
	}

	//////////////// map codecs ////////////////

	/** creates a DFU {@link MapCodec} which can encode and decode instances of the given class. */
	public <T_Decoded> @NotNull MapCodec<T_Decoded> createDFUMapCodec(@NotNull Class<T_Decoded> clazz) {
		return Auto2DFUMapCodec.of(this, this.createCoder(clazz));
	}

	/** creates a DFU {@link MapCodec} which can encode and decode instances of the given type. */
	public <T_Decoded> @NotNull MapCodec<T_Decoded> createDFUMapCodec(@NotNull ReifiedType<T_Decoded> type) {
		return Auto2DFUMapCodec.of(this, this.createCoder(type));
	}

	/** creates a DFU {@link MapCodec} which delegates to the provided {@link AutoEncoder} and {@link AutoDecoder}. */
	public <T_Decoded> @NotNull MapCodec<T_Decoded> createDFUMapCodec(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		return Auto2DFUMapCodec.of(this, encoder, decoder);
	}

	/** creates a DFU {@link MapCodec} which delegates to the provided {@link AutoCoder}. */
	public <T_Decoded> @NotNull MapCodec<T_Decoded> createDFUMapCodec(@NotNull AutoCoder<T_Decoded> both) {
		return Auto2DFUMapCodec.of(this, both);
	}

	//////////////// encoders ////////////////

	/** creates a DFU {@link Encoder} which can encode instances of the given class. */
	public <T_Decoded> @NotNull Encoder<T_Decoded> createDFUEncoder(@NotNull Class<T_Decoded> clazz) {
		return Auto2DFUEncoder.of(this, this.createEncoder(clazz));
	}

	/** creates a DFU {@link Encoder} which can encode instances of the given type. */
	public <T_Decoded> @NotNull Encoder<T_Decoded> createDFUEncoder(@NotNull ReifiedType<T_Decoded> type) {
		return Auto2DFUEncoder.of(this, this.createEncoder(type));
	}

	/** creates a DFU {@link Encoder} which delegates to the provided {@link AutoEncoder}. */
	public <T_Decoded> @NotNull Encoder<T_Decoded> createDFUEncoder(@NotNull AutoEncoder<T_Decoded> encoder) {
		return Auto2DFUEncoder.of(this, encoder);
	}

	/**
	creates an {@link AutoEncoder} which delegates to the provided DFU {@link Encoder}.
	@param allowPartial if true, partial results will be returned,
	and errors will be logged. if false, partial results will be ignored,
	and errors will be thrown. note that in all cases, complete successes
	will always be returned, and complete failures will always be thrown.
	*/
	public <T_Decoded> @NotNull AutoEncoder<T_Decoded> wrapDFUEncoder(@NotNull Encoder<T_Decoded> encoder, boolean allowPartial) {
		return DFU2AutoEncoder.of(encoder, allowPartial);
	}

	//////////////// decoders ////////////////

	/** creates a DFU {@link Decoder} which can decode instances of the given class. */
	public <T_Decoded> @NotNull Decoder<T_Decoded> createDFUDecoder(@NotNull Class<T_Decoded> clazz) {
		return Auto2DFUDecoder.of(this, this.createDecoder(clazz));
	}

	/** creates a DFU {@link Decoder} which can decode instances of the given type. */
	public <T_Decoded> @NotNull Decoder<T_Decoded> createDFUDecoder(@NotNull ReifiedType<T_Decoded> type) {
		return Auto2DFUDecoder.of(this, this.createDecoder(type));
	}

	/** creates a DFU {@link Decoder} which delegates to the provided {@link AutoDecoder} */
	public <T_Decoded> @NotNull Decoder<T_Decoded> createDFUDecoder(@NotNull AutoDecoder<T_Decoded> decoder) {
		return Auto2DFUDecoder.of(this, decoder);
	}

	/**
	creates an {@link AutoDecoder} which delegates to the provided DFU {@link Decoder}
	@param allowPartial if true, partial results will be returned,
	and errors will be logged. if false, partial results will be ignored,
	and errors will be thrown. note that in all cases, complete successes
	will always be returned, and complete failures will always be thrown.
	*/
	public <T_Decoded> @NotNull AutoDecoder<T_Decoded> wrapDFUDecoder(@NotNull Decoder<T_Decoded> decoder, boolean allowPartial) {
		return DFU2AutoDecoder.of(decoder, allowPartial);
	}



	//////////////////////////////// factory methods ////////////////////////////////



	/**
	creates a {@link FactoryContext} for the given class.

	this method is marked as {@link Internal} because it is
	preferable to call the "create<handler type>" methods instead.
	*/
	@Internal
	public <T_Decoded> @NotNull FactoryContext<T_Decoded> newFactoryContext(@NotNull Class<T_Decoded> clazz) {
		return new FactoryContext<>(this, ReifiedType.from(clazz));
	}

	/**
	creates a {@link FactoryContext} for the given type.

	this method is marked as {@link Internal} because it is
	preferable to call the "create<handler type>" methods instead.
	*/
	@Internal
	public <T_Decoded> @NotNull FactoryContext<T_Decoded> newFactoryContext(@NotNull ReifiedType<T_Decoded> type) {
		return new FactoryContext<>(this, type);
	}

	/**
	creates a {@link ReflectContext} for the given class.

	this method is marked as {@link Internal} because reflection in
	general is only intended to be used by {@link AutoFactory}'s.
	if you are writing your own factory and need to reflect into things,
	consider using {@link FactoryContext#reflect(ReifiedType)} instead.
	*/
	@Internal
	public <T_Owner> @NotNull ReflectContext<T_Owner> reflect(@NotNull Class<T_Owner> owner) {
		return new ReflectContext<>(this, ReifiedType.from(owner));
	}

	/**
	creates a {@link ReflectContext} for the given type.

	this method is marked as {@link Internal} because reflection in
	general is only intended to be used by {@link AutoFactory}'s.
	if you are writing your own factory and need to reflect into things,
	consider using {@link FactoryContext#reflect(ReifiedType)} instead.
	*/
	@Internal
	@Override
	public <T_Owner> @NotNull ReflectContext<T_Owner> reflect(@NotNull ReifiedType<T_Owner> owner) {
		return new ReflectContext<>(this, owner);
	}

	/**
	creates an {@link AutoEncoder} which can encode instances of the provided class.
	if such an encoder could not be created for any reason, a {@link FactoryException} is thrown.
	*/
	public <T_Decoded> @NotNull AutoEncoder<T_Decoded> createEncoder(@NotNull Class<T_Decoded> clazz) throws FactoryException  {
		return this.newFactoryContext(clazz).forceCreateEncoder();
	}

	/**
	creates an {@link AutoEncoder} which can encode instances of the provided type.
	if such an encoder could not be created for any reason, a {@link FactoryException} is thrown.
	*/
	public <T_Decoded> @NotNull AutoEncoder<T_Decoded> createEncoder(@NotNull ReifiedType<T_Decoded> type) throws FactoryException {
		return this.newFactoryContext(type).forceCreateEncoder();
	}

	/**
	creates an {@link AutoConstructor} which can construct instances of the provided class.
	if such a constructor could not be created for any reason, a {@link FactoryException} is thrown.

	this method is marked as {@link TestOnly} because from a serialization perspective,
	an {@link AutoConstructor} has no use without an associated {@link AutoImprinter}.
	if both are desired and you are trying to write your own {@link AutoDecoder},
	consider using the methods on {@link FactoryContext} instead.
	*/
	@TestOnly
	public <T_Decoded> @NotNull AutoConstructor<T_Decoded> createConstructor(@NotNull Class<T_Decoded> clazz) throws FactoryException {
		return this.newFactoryContext(clazz).forceCreateConstructor();
	}

	/**
	creates an {@link AutoConstructor} which can construct instances of the provided type.
	if such a constructor could not be created for any reason, a {@link FactoryException} is thrown.

	this method is marked as {@link TestOnly} because from a serialization perspective,
	an {@link AutoConstructor} has no use without an associated {@link AutoImprinter}.
	if both are desired and you are trying to write your own {@link AutoDecoder},
	consider using the methods on {@link FactoryContext} instead.
	*/
	@TestOnly
	public <T_Decoded> @NotNull AutoConstructor<T_Decoded> createConstructor(@NotNull ReifiedType<T_Decoded> type) throws FactoryException {
		return this.newFactoryContext(type).forceCreateConstructor();
	}

	/**
	creates an {@link AutoImprinter} which can imprint instances of the provided class.
	if such an imprinter could not be created for any reason, a {@link FactoryException} is thrown.

	this method is marked as {@link TestOnly} because from a serialization perspective,
	an {@link AutoImprinter} has no use without an associated {@link AutoConstructor}.
	if both are desired and you are trying to write your own {@link AutoDecoder},
	consider using the methods on {@link FactoryContext} instead.
	*/
	@TestOnly
	public <T_Decoded> @NotNull AutoImprinter<T_Decoded> createImprinter(@NotNull Class<T_Decoded> clazz) throws FactoryException {
		return this.newFactoryContext(clazz).forceCreateImprinter();
	}

	/**
	creates an {@link AutoImprinter} which can imprint instances of the provided type.
	if such an imprinter could not be created for any reason, a {@link FactoryException} is thrown.

	this method is marked as {@link TestOnly} because from a serialization perspective,
	an {@link AutoImprinter} has no use without an associated {@link AutoConstructor}.
	if both are desired and you are trying to write your own {@link AutoDecoder},
	consider using the methods on {@link FactoryContext} instead.
	*/
	@TestOnly
	public <T_Decoded> @NotNull AutoImprinter<T_Decoded> createImprinter(@NotNull ReifiedType<T_Decoded> type) throws FactoryException {
		return this.newFactoryContext(type).forceCreateImprinter();
	}

	/**
	creates an {@link AutoDecoder} which can decode instances of the provided class.
	if such a decoder could not be created for any reason, a {@link FactoryException} is thrown.
	*/
	public <T_Decoded> @NotNull AutoDecoder<T_Decoded> createDecoder(@NotNull Class<T_Decoded> clazz) throws FactoryException {
		return this.newFactoryContext(clazz).forceCreateDecoder();
	}

	/**
	creates an {@link AutoDecoder} which can decode instances of the provided type.
	if such a decoder could not be created for any reason, a {@link FactoryException} is thrown.
	*/
	public <T_Decoded> @NotNull AutoDecoder<T_Decoded> createDecoder(@NotNull ReifiedType<T_Decoded> type) throws FactoryException {
		return this.newFactoryContext(type).forceCreateDecoder();
	}

	/**
	creates an {@link AutoDecoder} which can decode instances of the provided class.
	if such a verifier could not be created for any reason, a {@link FactoryException} is thrown.

	this method is marked as {@link TestOnly} because from a serialization perspective,
	an {@link AutoVerifier} has no use without an associated {@link AutoDecoder}.
	if you are writing your own {@link AutoDecoder} and need an {@link AutoVerifier}
	for it, consider using the methods on {@link FactoryContext} instead.
	*/
	@TestOnly
	public <T_Decoded> @NotNull AutoVerifier<T_Decoded> createVerifier(@NotNull Class<T_Decoded> clazz) throws FactoryException {
		return this.newFactoryContext(clazz).forceCreateVerifier();
	}

	/**
	creates an {@link AutoDecoder} which can decode instances of the provided type.
	if such a verifier could not be created for any reason, a {@link FactoryException} is thrown.

	this method is marked as {@link TestOnly} because from a serialization perspective,
	an {@link AutoVerifier} has no use without an associated {@link AutoDecoder}.
	if you are writing your own {@link AutoDecoder} and need an {@link AutoVerifier}
	for it, consider using the methods on {@link FactoryContext} instead.
	*/
	@TestOnly
	public <T_Decoded> @NotNull AutoVerifier<T_Decoded> createVerifier(@NotNull ReifiedType<T_Decoded> type) throws FactoryException {
		return this.newFactoryContext(type).forceCreateVerifier();
	}

	/**
	creates an {@link AutoCoder} which can encode and decode instances of the given class.
	if an encoder or decoder could not be created for any reason, a {@link FactoryException} is thrown.
	*/
	public <T_Decoded> @NotNull AutoCoder<T_Decoded> createCoder(@NotNull Class<T_Decoded> clazz) throws FactoryException {
		return this.newFactoryContext(clazz).forceCreateCoder();
	}

	/**
	creates an {@link AutoCoder} which can encode and decode instances of the given type.
	if an encoder or decoder could not be created for any reason, a {@link FactoryException} is thrown.
	*/
	public <T_Decoded> @NotNull AutoCoder<T_Decoded> createCoder(@NotNull ReifiedType<T_Decoded> type) throws FactoryException {
		return this.newFactoryContext(type).forceCreateCoder();
	}



	//////////////////////////////// util ////////////////////////////////



	/**
	encodes the provided input with the provided encoder and ops.
	any exceptions thrown by the encoder are relayed to the caller.
	*/
	public <T_Encoded, T_Decoded> @NotNull T_Encoded encode(@NotNull AutoEncoder<T_Decoded> encoder, T_Decoded input, @NotNull DynamicOps<T_Encoded> ops) throws EncodeException {
		return new EncodeContext<>(this, input, ops).encodeWith(encoder);
	}

	/**
	creates a new {@link DecodeContext} bound to this AutoCodec,
	with a root path (AKA no parent), and the provided input and ops.

	this method is marked with {@link Internal} because it is preferable
	to call {@link #decode(AutoDecoder, Object, DynamicOps)} or a similar method instead.
	*/
	@Internal
	public <T_Encoded> @NotNull DecodeContext<T_Encoded> newDecodeContext(@NotNull T_Encoded input, @NotNull DynamicOps<T_Encoded> ops) {
		return new DecodeContext<>(this, null, RootDecodePath.INSTANCE, input, ops);
	}

	/**
	constructs a new object using the provided constructor, input, and ops.
	any exceptions thrown by the constructor are relayed to the caller.

	this method is marked as {@link TestOnly} because construction is
	typically not useful without imprinting, and at that point you might
	as well use {@link #decode(AutoDecoder, Object, DynamicOps)} instead.
	*/
	@TestOnly
	public <T_Encoded, T_Decoded> @NotNull T_Decoded construct(@NotNull AutoConstructor<T_Decoded> constructor, @NotNull T_Encoded input, @NotNull DynamicOps<T_Encoded> ops) throws ConstructException {
		return this.newDecodeContext(input, ops).constructWith(constructor);
	}

	/**
	imprints the provided object using the provided imprinter, input, and ops.
	any exceptions thrown by the imprinter are relayed to the caller.

	this method is marked as {@link TestOnly} because imprinting is
	typically not useful without constructing, and at that point you might
	as well use {@link #decode(AutoDecoder, Object, DynamicOps)} instead.
	*/
	@TestOnly
	public <T_Encoded, T_Decoded> void imprint(@NotNull AutoImprinter<T_Decoded> imprinter, @NotNull T_Decoded object, @NotNull T_Encoded input, @NotNull DynamicOps<T_Encoded> ops) throws ImprintException {
		this.newDecodeContext(input, ops).imprintWith(imprinter, object);
	}

	/**
	decodes the provided input using the provided decoder and ops.
	any exceptions thrown by the decoder are relayed to the caller.
	*/
	public <T_Encoded, T_Decoded> T_Decoded decode(@NotNull AutoDecoder<T_Decoded> decoder, @NotNull T_Encoded input, @NotNull DynamicOps<T_Encoded> ops) throws DecodeException {
		return this.newDecodeContext(input, ops).decodeWith(decoder);
	}

	/**
	verifies the provided object using the provided verifier, input, and ops.
	any exceptions thrown by the verifier are relayed to the caller.

	this method is marked as {@link TestOnly} because verification is
	typically not useful without decoding, and {@link DecoderFactoryList}
	will automatically add a verifier at the end if any verifiers are applicable,
	so {@link #decode(AutoDecoder, Object, DynamicOps)} so will perform verification too.
	*/
	@TestOnly
	public <T_Encoded, T_Decoded> void verify(@NotNull AutoVerifier<T_Decoded> verifier, @Nullable T_Decoded object, @NotNull T_Encoded input, @NotNull DynamicOps<T_Encoded> ops) throws VerifyException {
		this.newDecodeContext(input, ops).verifyWith(verifier, object);
	}



	//////////////////////////////// setup ////////////////////////////////



	/**
	convenience method to create all 3 of our loggers,
	as {@link #createFactoryLogger(ReentrantLock)},
	{@link #createEncodeLogger(ReentrantLock)},
	and {@link #createDecodeLogger(ReentrantLock)}
	all delegate to this method by default.
	*/
	@OverrideOnly
	public @NotNull TaskLogger createDefaultLogger(@NotNull ReentrantLock lock) {
		return new StackContextLogger(lock, Printer.SYSTEM, true);
	}

	/**
	creates the {@link TaskLogger} which will be used for logging when
	a handler is requested from or created by one of our factory lists.
	the default implementation delegates to {@link #createDefaultLogger(ReentrantLock)}.
	the lock provided is the same lock that is also provided to
	{@link #createEncodeLogger(ReentrantLock)} and {@link #createDecodeLogger(ReentrantLock)}.
	this allows the 3 loggers to not log in the middle of each other if desired.
	anonymous subclasses of AutoCodec are free to ignore this lock if the
	3 loggers will print to different places. for example, different files.
	anonymous subclasses of AutoCodec are also free to return
	different types of loggers for factories, encoding, and decoding.
	for example, by making encoding and decoding more verbose than factories.
	*/
	@OverrideOnly
	public @NotNull TaskLogger createFactoryLogger(@NotNull ReentrantLock lock) {
		return this.createDefaultLogger(lock);
	}

	/**
	creates the {@link TaskLogger} which will be used for logging when an object is encoded.
	the default implementation delegates to {@link #createDefaultLogger(ReentrantLock)}.
	the lock provided is the same lock that is also provided to
	{@link #createFactoryLogger(ReentrantLock)} and {@link #createDecodeLogger(ReentrantLock)}.
	this allows the 3 loggers to not log in the middle of each other if desired.
	anonymous subclasses of AutoCodec are free to ignore this lock if the
	3 loggers will print to different places. for example, different files.
	anonymous subclasses of AutoCodec are also free to return
	different types of loggers for factories, encoding, and decoding.
	for example, by making encoding and decoding more verbose than factories.
	*/
	@OverrideOnly
	public @NotNull TaskLogger createEncodeLogger(@NotNull ReentrantLock lock) {
		return this.createDefaultLogger(lock);
	}

	/**
	creates the {@link TaskLogger} which will be used for logging when an object is decoded.
	the default implementation delegates to {@link #createDefaultLogger(ReentrantLock)}.
	the lock provided is the same lock that is also provided to
	{@link #createFactoryLogger(ReentrantLock)} and {@link #createEncodeLogger(ReentrantLock)}.
	this allows the 3 loggers to not log in the middle of each other if desired.
	anonymous subclasses of AutoCodec are free to ignore this lock if the
	3 loggers will print to different places. for example, different files.
	anonymous subclasses of AutoCodec are also free to return
	different types of loggers for factories, encoding, and decoding.
	for example, by making encoding and decoding more verbose than factories.
	*/
	@OverrideOnly
	public @NotNull TaskLogger createDecodeLogger(@NotNull ReentrantLock lock) {
		return this.createDefaultLogger(lock);
	}

	/**
	creates the {@link ReflectionManager} which this AutoCodec
	will use when factories attempt to reflect into types.
	anonymous subclasses of AutoCodec can override this method
	to provide a ReflectionManager which imposes additional
	restrictions on which fields/methods are visible,
	or provide {@link MethodHandles.Lookup}'s for additional classes.
	*/
	@OverrideOnly
	public @NotNull ReflectionManager createReflectionManager() {
		return new ReflectionManager();
	}

	/**
	creates the {@link EncoderFactoryList} which this
	AutoCodec uses to create {@link AutoEncoder}'s.
	anonymous subclasses of AutoCodec can override this method
	to provide an EncoderFactoryList with different factories built into it.
	*/
	@OverrideOnly
	public @NotNull EncoderFactoryList createEncoders() {
		return new EncoderFactoryList(this);
	}

	/**
	creates the {@link ConstructorFactoryList} which this
	AutoCodec uses to create {@link AutoConstructor}'s.
	anonymous subclasses of AutoCodec can override this method
	to provide a ConstructorFactoryList with different factories built into it.
	*/
	@OverrideOnly
	public @NotNull ConstructorFactoryList createConstructors() {
		return new ConstructorFactoryList(this);
	}

	/**
	creates the {@link ImprinterFactoryList} which this
	AutoCodec uses to create {@link AutoImprinter}'s.
	anonymous subclasses of AutoCodec can override this method
	to provide an ImprinterFactoryList with different factories built into it.
	*/
	@OverrideOnly
	public @NotNull ImprinterFactoryList createImprinters() {
		return new ImprinterFactoryList(this);
	}

	/**
	creates the {@link DecoderFactoryList} which this
	AutoCodec uses to create {@link AutoDecoder}'s.
	anonymous subclasses of AutoCodec can override this method
	to provide an DecoderFactoryList with different factories built into it.
	*/
	@OverrideOnly
	public @NotNull DecoderFactoryList createDecoders() {
		return new DecoderFactoryList(this);
	}

	/**
	creates the {@link VerifierFactoryList} which this
	AutoCodec uses to create {@link AutoVerifier}'s.
	anonymous subclasses of AutoCodec can override this method
	to provide an VerifierFactoryList with different factories built into it.
	*/
	@OverrideOnly
	public @NotNull VerifierFactoryList createVerifiers() {
		return new VerifierFactoryList(this);
	}
}