package builderb0y.autocodec.logging;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintContext;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.ReflectException;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;

/**
TaskLogger which prints nothing ever, and has no internal state.
this logger is most useful in performance-sensitive
code which cannot afford any logging overhead.
this logger is also useful in highly concurrent code,
which cannot afford the contention of {@link AbstractTaskLogger#lock}.
*/
public class DisabledTaskLogger extends TaskLogger {

	@Override
	public void logMessage(@NotNull Object message) {}

	@Override
	public void logMessageLazy(@NotNull Supplier<@NotNull String> message) {}

	@Override
	public void logError(@NotNull Object message) {}

	@Override
	public void logErrorLazy(@NotNull Supplier<@NotNull String> message) {}

	@Override
	public <R, X extends Throwable> R runTask(@NotNull LoggableTask<R, X> task) throws X {
		return task.run();
	}

	//////////////////////////////// built-in tasks ////////////////////////////////

	//////////////// handlers ////////////////

	@Override
	public <T_Encoded, T_Decoded> T_Encoded encode(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return encoder.encode(context);
	}

	@Override
	public <T_Encoded, T_Decoded> T_Decoded decode(@NotNull AutoDecoder<T_Decoded> decoder, @NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return decoder.decode(context);
	}

	@Override
	public <T_Encoded, T_Decoded> T_Decoded construct(@NotNull AutoConstructor<T_Decoded> constructor, @NotNull ConstructContext<T_Encoded> context) throws ConstructException {
		return constructor.construct(context);
	}

	@Override
	public <T_Encoded, T_Decoded> void imprint(@NotNull AutoImprinter<T_Decoded> imprinter, @NotNull ImprintContext<T_Encoded, T_Decoded> context) throws ImprintException {
		imprinter.imprint(context);
	}

	@Override
	public <T_Encoded, T_Decoded> void verify(@NotNull AutoVerifier<T_Decoded> verifier, @NotNull VerifyContext<T_Encoded, T_Decoded> context) throws VerifyException {
		verifier.verify(context);
	}

	//////////////// factories ////////////////

	@Override
	public <T_Handler extends AutoHandler> @Nullable T_Handler tryCreateHandler(@NotNull AutoHandler.AutoFactory<T_Handler> factory, @NotNull FactoryContext<?> context) {
		return factory.tryCreate(context);
	}

	@Override
	public <T_Handler extends AutoHandler> @NotNull T_Handler forceCreateHandler(@NotNull AutoHandler.AutoFactory<T_Handler> factory, @NotNull FactoryContext<?> context) {
		return factory.forceCreate(context);
	}

	@Override
	public <T_Handler extends AutoHandler, T_Factory extends AutoFactory<T_Handler>> @Nullable T_Handler tryCreateFallbackHandler(@NotNull FactoryList<T_Handler, T_Factory> factoryList, @NotNull FactoryContext<?> context, @NotNull T_Factory caller) throws FactoryException {
		return factoryList.tryCreateFallback(context, caller);
	}

	@Override
	public <T_Handler extends AutoHandler, T_Factory extends AutoFactory<T_Handler>> @NotNull T_Handler forceCreateFallbackHandler(@NotNull FactoryList<T_Handler, T_Factory> factoryList, @NotNull FactoryContext<?> context, @NotNull T_Factory caller) throws FactoryException {
		return factoryList.forceCreateFallback(context, caller);
	}

	//////////////// reflection ////////////////

	@Override
	public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] getFields(@NotNull ReflectContext<T_Owner> context, boolean inherited) throws ReflectException {
		return context.reflectionManager().getFields(context, inherited);
	}

	@Override
	public @Nullable <T_Owner, T_Collect> T_Collect searchFields(@NotNull ReflectContext<T_Owner> context, boolean inherited, @NotNull Predicate<? super FieldLikeMemberView<T_Owner, ?>> predicate, @NotNull MemberCollector<FieldLikeMemberView<T_Owner, ?>, T_Collect> collector) throws ReflectException {
		return context.reflectionManager().searchFields(context, inherited, predicate, collector);
	}

	@Override
	public <T_Owner> @NotNull MethodLikeMemberView<T_Owner, ?> @NotNull [] getMethods(@NotNull ReflectContext<T_Owner> context, boolean inherited) throws ReflectException {
		return context.reflectionManager().getMethods(context, inherited);
	}

	@Override
	public <T_Owner, T_Collect> @Nullable T_Collect searchMethods(@NotNull ReflectContext<T_Owner> context, boolean inherited, @NotNull Predicate<? super MethodLikeMemberView<T_Owner, ?>> predicate, @NotNull MemberCollector<MethodLikeMemberView<T_Owner, ?>, T_Collect> collector) throws ReflectException {
		return context.reflectionManager().searchMethods(context, inherited, predicate, collector);
	}
}