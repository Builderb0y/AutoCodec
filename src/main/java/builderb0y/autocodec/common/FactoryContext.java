package builderb0y.autocodec.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.coders.Coder;
import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.AutoConstructor.ConstructorFactory;
import builderb0y.autocodec.constructors.ConstructorFactoryList;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.decoders.DecoderFactoryList;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.AutoEncoder.EncoderFactory;
import builderb0y.autocodec.encoders.EncoderFactoryList;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.AutoImprinter.ImprinterFactory;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.AutoVerifier.VerifierFactory;
import builderb0y.autocodec.verifiers.NoopVerifier;
import builderb0y.autocodec.verifiers.VerifierFactoryList;

/** context used by {@link AutoFactory} to create handlers. */
public class FactoryContext<T_HandledType> extends TaskContext implements ReflectContextProvider {

	public static final @NotNull ObjectArrayFactory<FactoryContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FactoryContext.class).generic();

	public final @NotNull ReifiedType<T_HandledType> type;

	public FactoryContext(
		@NotNull AutoCodec autoCodec,
		@NotNull ReifiedType<T_HandledType> type
	) {
		super(autoCodec);
		this.type = type;
	}

	@Override
	public @NotNull TaskLogger logger() {
		return this.autoCodec.factoryLogger;
	}

	/** provides reflective access to our own {@link #type}. */
	public @NotNull ReflectContext<T_HandledType> reflect() {
		return this.reflect(this.type);
	}

	@Override
	public <T_Owner> @NotNull ReflectContext<T_Owner> reflect(@NotNull ReifiedType<T_Owner> owner) {
		return new ReflectContext<>(this.autoCodec, owner);
	}

	/**
	returns a new FactoryContext for the given type.
	this method is intended to be chained. for example: {@code
		context.type(ReifiedType.from(String.class)).tryCreateEncoder()
	}
	an overload for {@link Class} instead of {@link ReifiedType}
	is not provided because in almost all cases,
	the type required is derived from another {@link ReifiedType}.
	*/
	@SuppressWarnings("unchecked")
	public <T_NewHandledType> @NotNull FactoryContext<T_NewHandledType> type(@NotNull ReifiedType<T_NewHandledType> newType) {
		return this.type == newType ? (FactoryContext<T_NewHandledType>)(this) : new FactoryContext<>(this.autoCodec, newType);
	}

	//////////////////////////////// creating handlers ////////////////////////////////

	//////////////// encoders ////////////////

	/**
	attempts to create an {@link AutoEncoder} from all factories
	in our {@link #autoCodec}'s {@link AutoCodec#encoders} list.
	if no factories on that list are able to create the requested
	{@link AutoEncoder}, returns null.
	if more than one factory on the list is able to create
	an {@link AutoEncoder}, only the first is returned.
	throws {@link FactoryException} if any factories encountered
	an error while trying to create the requested {@link AutoEncoder}.
	*/
	public @Nullable AutoEncoder<T_HandledType> tryCreateEncoder() throws FactoryException {
		return this.tryCreateEncoder(this.autoCodec.encoders);
	}

	/**
	same as {@link #tryCreateEncoder()}, but will throw a
	{@link FactoryException} on failure instead of returning null.
	*/
	public @NotNull AutoEncoder<T_HandledType> forceCreateEncoder() throws FactoryException {
		return this.forceCreateEncoder(this.autoCodec.encoders);
	}

	/**
	attempts to create an {@link AutoEncoder} with the provided factory.
	if the factory is unable to create the requested {@link AutoEncoder}, returns null.
	throws {@link FactoryException} if the factory encountered
	an error while trying to create the requested {@link AutoEncoder}.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoEncoder<T_HandledType> tryCreateEncoder(@NotNull EncoderFactory factory) throws FactoryException {
		return (AutoEncoder<T_HandledType>)(this.logger().tryCreateHandler(factory, this));
	}

	/**
	same as {@link #tryCreateEncoder(EncoderFactory)}, but will throw
	a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoEncoder<T_HandledType> forceCreateEncoder(@NotNull EncoderFactory factory) throws FactoryException {
		return (AutoEncoder<T_HandledType>)(this.logger().forceCreateHandler(factory, this));
	}

	/**
	returns the {@link AutoEncoder} which *would* be returned
	by {@link #tryCreateEncoder()} IF the provided factory
	(caller) and all factories that come before it in the
	{@link EncoderFactoryList} were not present at all.
	this can be used by factories which want to wrap
	an existing handler in a different handler.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoEncoder<T_HandledType> tryCreateFallbackEncoder(@NotNull EncoderFactory caller) throws FactoryException {
		return (AutoEncoder<T_HandledType>)(this.logger().tryCreateFallbackHandler(this.autoCodec.encoders, this, caller));
	}

	/**
	same as {@link #tryCreateFallbackEncoder(EncoderFactory)}, but will
	throw a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoEncoder<T_HandledType> forceCreateFallbackEncoder(@NotNull EncoderFactory caller) throws FactoryException {
		return (AutoEncoder<T_HandledType>)(this.logger().forceCreateFallbackHandler(this.autoCodec.encoders, this, caller));
	}

	//////////////// constructors ////////////////

	/**
	attempts to create an {@link AutoConstructor} from all factories
	in our {@link #autoCodec}'s {@link AutoCodec#constructors} list.
	if no factories on that list are able to create the requested
	{@link AutoConstructor}, returns null.
	if more than one factory on the list is able to create
	an {@link AutoConstructor}, only the first is returned.
	throws {@link FactoryException} if any factories encountered
	an error while trying to create the requested {@link AutoConstructor}.
	*/
	public @Nullable AutoConstructor<T_HandledType> tryCreateConstructor() throws FactoryException {
		return this.tryCreateConstructor(this.autoCodec.constructors);
	}

	/**
	attempts to create an {@link AutoConstructor} from all factories
	in our {@link #autoCodec}'s {@link AutoCodec#constructors} list.
	if no factories on that list are able to create the requested
	{@link AutoConstructor}, throws {@link FactoryException}.
	if more than one factory on the list is able to create
	an {@link AutoConstructor}, only the first is returned.
	also throws {@link FactoryException} if any factories encountered
	an error while trying to create the requested {@link AutoConstructor}.
	*/
	public @NotNull AutoConstructor<T_HandledType> forceCreateConstructor() throws FactoryException {
		return this.forceCreateConstructor(this.autoCodec.constructors);
	}

	/**
	attempts to create an {@link AutoConstructor} with the provided factory.
	if the factory is unable to create the requested {@link AutoConstructor}, returns null.
	throws {@link FactoryException} if the factory encountered
	an error while trying to create the requested {@link AutoConstructor}.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoConstructor<T_HandledType> tryCreateConstructor(@NotNull ConstructorFactory factory) throws FactoryException {
		return (AutoConstructor<T_HandledType>)(this.logger().tryCreateHandler(factory, this));
	}

	/**
	attempts to create an {@link AutoConstructor} with the provided factory.
	if the factory is unable to create the requested
	{@link AutoConstructor}, throws {@link FactoryException}.
	also throws {@link FactoryException} if the factory encountered
	an error while trying to create the requested {@link AutoConstructor}.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoConstructor<T_HandledType> forceCreateConstructor(@NotNull ConstructorFactory factory) throws FactoryException {
		return (AutoConstructor<T_HandledType>)(this.logger().forceCreateHandler(factory, this));
	}

	/**
	returns the {@link AutoConstructor} which *would* be returned
	by {@link #tryCreateConstructor()} IF the provided factory
	(caller) and all factories that come before it in the
	{@link ConstructorFactoryList} were not present at all.
	this can be used by factories which want to wrap
	an existing handler in a different handler.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoConstructor<T_HandledType> tryCreateFallbackConstructor(@NotNull ConstructorFactory caller) throws FactoryException {
		return (AutoConstructor<T_HandledType>)(this.logger().tryCreateFallbackHandler(this.autoCodec.constructors, this, caller));
	}

	/**
	same as {@link #tryCreateFallbackConstructor(ConstructorFactory)}, but will
	throw a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoConstructor<T_HandledType> forceCreateFallbackConstructor(@NotNull ConstructorFactory caller) throws FactoryException {
		return (AutoConstructor<T_HandledType>)(this.logger().forceCreateFallbackHandler(this.autoCodec.constructors, this, caller));
	}

	//////////////// imprinters ////////////////

	/**
	attempts to create an {@link AutoImprinter} which does NOT
	perform any verification after imprinting from all factories
	in our {@link #autoCodec}'s {@link AutoCodec#imprinters} list.
	if no factories on that list are able to create
	the requested {@link AutoImprinter}, returns null.
	if more than one factory on the list is able to create
	an {@link AutoImprinter}, only the first is returned.
	throws {@link FactoryException} if any factories encountered
	an error while trying to create the requested {@link AutoImprinter}.
	*/
	public @Nullable AutoImprinter<T_HandledType> tryCreateImprinter() throws FactoryException {
		return this.tryCreateImprinter(this.autoCodec.imprinters);
	}

	/**
	same as {@link #tryCreateImprinter()}, but will throw a
	{@link FactoryException} on failure instead of returning null.
	*/
	public @NotNull AutoImprinter<T_HandledType> forceCreateImprinter() throws FactoryException {
		return this.forceCreateImprinter(this.autoCodec.imprinters);
	}

	/**
	attempts to create an {@link AutoImprinter} which does NOT perform
	any verification after imprinting with the provided factory.
	if the factory is unable to create the requested {@link AutoImprinter}, returns null.
	throws {@link FactoryException} if the factory encountered
	an error while trying to create the requested {@link AutoImprinter}.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoImprinter<T_HandledType> tryCreateImprinter(@NotNull ImprinterFactory factory) throws FactoryException {
		return (AutoImprinter<T_HandledType>)(this.logger().tryCreateHandler(factory, this));
	}

	/**
	same as {@link #tryCreateImprinter(ImprinterFactory)}, but will throw
	a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoImprinter<T_HandledType> forceCreateImprinter(@NotNull ImprinterFactory factory) throws FactoryException {
		return (AutoImprinter<T_HandledType>)(this.logger().forceCreateHandler(factory, this));
	}

	/**
	returns the {@link AutoImprinter} which *would* be returned
	by {@link #tryCreateImprinter()} IF the provided factory
	(caller) and all factories that come before it in the
	{@link VerifierFactoryList} were not present at all.
	this can be used by factories which want to wrap
	an existing handler in a different handler.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoImprinter<T_HandledType> tryCreateFallbackImprinter(@NotNull ImprinterFactory caller) throws FactoryException {
		return (AutoImprinter<T_HandledType>)(this.logger().tryCreateFallbackHandler(this.autoCodec.imprinters, this, caller));
	}

	/**
	same as {@link #tryCreateFallbackImprinter(ImprinterFactory)}, but will
	throw a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoImprinter<T_HandledType> forceCreateFallbackImprinter(@NotNull ImprinterFactory caller) throws FactoryException {
		return (AutoImprinter<T_HandledType>)(this.logger().forceCreateFallbackHandler(this.autoCodec.imprinters, this, caller));
	}

	//////////////// decoders ////////////////

	/**
	attempts to create an {@link AutoDecoder} from all factories
	in our {@link #autoCodec}'s {@link AutoCodec#decoders} list.
	if no factories on that list are able to create the requested
	{@link AutoDecoder}, returns null.
	if more than one factory on the list is able to create
	an {@link AutoDecoder}, only the first is returned.
	throws {@link FactoryException} if any factories encountered
	an error while trying to create the requested {@link AutoDecoder}.
	*/
	public @Nullable AutoDecoder<T_HandledType> tryCreateDecoder() throws FactoryException {
		return this.tryCreateDecoder(this.autoCodec.decoders);
	}

	/**
	same as {@link #tryCreateDecoder()}, but will throw a
	{@link FactoryException} on failure instead of returning null.
	*/
	public @NotNull AutoDecoder<T_HandledType> forceCreateDecoder() throws FactoryException {
		return this.forceCreateDecoder(this.autoCodec.decoders);
	}

	/**
	attempts to create an {@link AutoDecoder} with the provided factory.
	if the factory is unable to create the requested {@link AutoDecoder}, returns null.
	throws {@link FactoryException} if the factory encountered
	an error while trying to create the requested {@link AutoDecoder}.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoDecoder<T_HandledType> tryCreateDecoder(@NotNull DecoderFactory factory) throws FactoryException {
		return (AutoDecoder<T_HandledType>)(this.logger().tryCreateHandler(factory, this));
	}

	/**
	same as {@link #tryCreateDecoder(DecoderFactory)}, but will throw
	a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoDecoder<T_HandledType> forceCreateDecoder(@NotNull DecoderFactory factory) throws FactoryException {
		return (AutoDecoder<T_HandledType>)(this.logger().forceCreateHandler(factory, this));
	}

	/**
	returns the {@link AutoDecoder} which *would* be returned
	by {@link #tryCreateDecoder()} IF the provided factory
	(caller) and all factories that come before it in the
	{@link DecoderFactoryList} were not present at all.
	this can be used by factories which want to wrap
	an existing handler in a different handler.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoDecoder<T_HandledType> tryCreateFallbackDecoder(@NotNull DecoderFactory caller) throws FactoryException {
		return (AutoDecoder<T_HandledType>)(this.logger().tryCreateFallbackHandler(this.autoCodec.decoders, this, caller));
	}

	/**
	same as {@link #tryCreateFallbackDecoder(DecoderFactory)}, but will
	throw a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoDecoder<T_HandledType> forceCreateFallbackDecoder(@NotNull DecoderFactory caller) throws FactoryException {
		return (AutoDecoder<T_HandledType>)(this.logger().forceCreateFallbackHandler(this.autoCodec.decoders, this, caller));
	}

	//////////////// verifiers ////////////////

	/**
	attempts to create an {@link AutoVerifier} from all factories
	in our {@link #autoCodec}'s {@link AutoCodec#verifiers} list.
	if no factories on that list are able to create the requested
	{@link AutoVerifier}, returns {@link NoopVerifier#INSTANCE}.
	this method is marked as {@link Nullable} for consistency with
	the other "tryCreate" methods, but it will never return null.
	if more than one factory on the list is able to create
	an {@link AutoVerifier}, the returned {@link AutoVerifier}
	will delegate to all of them.
	throws {@link FactoryException} if any factories encountered
	an error while trying to create the requested {@link AutoVerifier}.
	*/
	public @Nullable AutoVerifier<T_HandledType> tryCreateVerifier() throws FactoryException {
		return this.tryCreateVerifier(this.autoCodec.verifiers);
	}

	/**
	same as {@link #tryCreateVerifier()}, but will throw a
	{@link FactoryException} on failure instead of returning null.
	*/
	public @NotNull AutoVerifier<T_HandledType> forceCreateVerifier() throws FactoryException {
		return this.forceCreateVerifier(this.autoCodec.verifiers);
	}

	/**
	attempts to create an {@link AutoVerifier} with the provided factory.
	if the factory is unable to create the requested {@link AutoVerifier}, returns null.
	throws {@link FactoryException} if the factory encountered
	an error while trying to create the requested {@link AutoVerifier}.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoVerifier<T_HandledType> tryCreateVerifier(@NotNull VerifierFactory factory) throws FactoryException {
		return (AutoVerifier<T_HandledType>)(this.logger().tryCreateHandler(factory, this));
	}

	/**
	same as {@link #tryCreateVerifier(VerifierFactory)}, but will throw
	a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoVerifier<T_HandledType> forceCreateVerifier(@NotNull VerifierFactory factory) throws FactoryException {
		return (AutoVerifier<T_HandledType>)(this.logger().forceCreateHandler(factory, this));
	}

	/**
	returns the {@link AutoVerifier} which *would* be returned
	by {@link #tryCreateVerifier()} IF the provided factory
	(caller) and all factories that come before it in the
	{@link VerifierFactoryList} were not present at all.
	this can be used by factories which want to wrap
	an existing handler in a different handler.
	*/
	@SuppressWarnings("unchecked")
	public @Nullable AutoVerifier<T_HandledType> tryCreateFallbackVerifier(@NotNull VerifierFactory caller) throws FactoryException {
		return (AutoVerifier<T_HandledType>)(this.logger().tryCreateFallbackHandler(this.autoCodec.verifiers, this, caller));
	}

	/**
	same as {@link #tryCreateFallbackVerifier(VerifierFactory)}, but will
	throw a {@link FactoryException} on failure instead of returning null.
	*/
	@SuppressWarnings("unchecked")
	public @NotNull AutoVerifier<T_HandledType> forceCreateFallbackVerifier(@NotNull VerifierFactory caller) throws FactoryException {
		return (AutoVerifier<T_HandledType>)(this.logger().forceCreateFallbackHandler(this.autoCodec.verifiers, this, caller));
	}

	//////////////// combo ////////////////

	public @Nullable AutoCoder<T_HandledType> tryCreateCoder() throws FactoryException {
		AutoDecoder<T_HandledType> decoder = this.tryCreateDecoder();
		if (decoder == null) return null;
		if (decoder instanceof AutoCoder<T_HandledType> coder) return coder;

		AutoEncoder<T_HandledType> encoder = this.tryCreateEncoder();
		if (encoder == null) return null;
		if (encoder instanceof AutoCoder<T_HandledType> coder) return coder;

		return new Coder<>(encoder, decoder);
	}

	public @NotNull AutoCoder<T_HandledType> forceCreateCoder() throws FactoryException {
		AutoDecoder<T_HandledType> decoder = this.forceCreateDecoder();
		if (decoder instanceof AutoCoder<T_HandledType> coder) return coder;

		AutoEncoder<T_HandledType> encoder = this.forceCreateEncoder();
		if (encoder instanceof AutoCoder<T_HandledType> coder) return coder;

		return new Coder<>(encoder, decoder);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { type: " + this.type + " }";
	}
}