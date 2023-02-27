package builderb0y.autocodec.logging;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;
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
import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.TypeFormatter;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;

/**
AutoCodec ties logging into its task system.
this helps keep track of tasks more easily.
in particular, {@link StackContextLogger}
keeps track of which tasks invoked which other tasks,
and when an error occurs, it prints out all the tasks
that led up to the error, and only those tasks.
I cannot overstate how immensely useful this is for debugging.

additionally, other TaskLogger implementations are also available which
print more or less information than {@link StackContextLogger} does.

{@link IndentedTaskLogger} prints out all tasks when they are started,
and their results when they are done.
and it indents them by depth, so it is easy
to see which tasks started which other tasks.

{@link BasicTaskLogger} does the same, but without any indentation.
I don't know why you'd want that, but it is available.

{@link DisabledTaskLogger} prints nothing ever,
and is an easy way to disable logging entirely.
it also overrides some of the more expensive logic in
the base TaskLogger class for performance improvements.
as such, {@link DisabledTaskLogger} caries basically no
logging overhead no matter what tasks are being performed.
*/
public abstract class TaskLogger {

	/**
	prints the message, if this logger wants to.
	subclasses may choose to not log certain messages under certain conditions.
	subclasses are encouraged to NOT call {@link Object#toString()} on
	the message unless this logger actually intends to print the message.

	note for callers: if you are trying to print a concatenated message,
	for example, logMessage("x: " + x + ", y: " + y),
	consider replacing this with {@link #logMessageLazy(Supplier)} instead:
	logMessageLazy(() -> "x: " + x + ", y: " + y);
	doing this will ensure that the concatenation is not
	performed if the logger doesn't want to print the message.
	*/
	public abstract void logMessage(@NotNull Object message);


	public static @NotNull Object lazyMessage(@NotNull Supplier<@NotNull String> message) {
		return new Object() {

			@Override
			public String toString() {
				return message.get();
			}
		};
	}

	public void logMessageLazy(@NotNull Supplier<@NotNull String> message) {
		this.logMessage(lazyMessage(message));
	}

	/**
	prints the message as an error, if this logger wants to.
	subclasses may choose to not log certain messages under certain conditions.
	subclasses are encouraged to NOT call {@link Object#toString()} on
	the message unless this logger actually intends to print the message.

	subclasses may also print errors differently than normal messages.

	note: the message may be a Throwable,
	in which case its stack trace should be printed.
	subclasses are expected to explicitly check for this.

	note for callers: if you are trying to print a concatenated message,
	for example, logError("x: " + x + ", y: " + y),
	consider replacing this with {@link #logMessageLazy(Supplier)} instead:
	logErrorLazy(() -> "x: " + x + ", y: " + y);
	doing this will ensure that the concatenation is not
	performed if the logger doesn't want to print the message.
	*/
	public abstract void logError(@NotNull Object message);

	public void logErrorLazy(@NotNull Supplier<@NotNull String> message) {
		this.logError(lazyMessage(message));
	}

	/**
	runs the task and returns its result.
	subclasses may also print additional messages
	when tasks are run, or after they complete.
	*/
	public abstract <R, X extends Throwable> R runTask(@NotNull LoggableTask<R, X> task) throws X;

	public <R, X extends Throwable> R unwrap(@NotNull DataResult<R> dataResult, boolean allowPartial, Function<? super String, ? extends X> onError) throws X {
		return dataResult.get().map(
			Function.identity(),
			(PartialResult<R> partialResult) -> {
				R result = AutoCodecUtil.getPartialResult(partialResult);
				if (result != null && allowPartial) {
					this.logError(partialResult.message());
					return result;
				}
				throw AutoCodecUtil.rethrow(onError.apply(partialResult.message()));
			}
		);
	}

	//////////////////////////////// built-in tasks ////////////////////////////////
	/**
	these methods are provided for the sole reason of being
	able to be overridden in {@link DisabledTaskLogger}
	to not allocate a new {@link LoggableTask} object,
	just to squeeze every last drop of performance out of the JVM.
	*/

	//////////////// handlers ////////////////



	public <T_Encoded, T_Decoded> @NotNull T_Encoded encode(
		@NotNull AutoEncoder<T_Decoded> encoder,
		@NotNull EncodeContext<T_Encoded, T_Decoded> context
	)
	throws EncodeException {
		return this.runTask(new LoggableTask<T_Encoded, EncodeException>() {

			@Override
			public @NotNull T_Encoded run() throws EncodeException {
				return encoder.encode(context);
			}

			@Override
			public String toString() {
				return "Encoding " + context + " with " + encoder;
			}
		});
	}

	public <T_Encoded, T_Decoded> @Nullable T_Decoded decode(
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull DecodeContext<T_Encoded> context
	)
	throws DecodeException {
		return this.runTask(new LoggableTask<T_Decoded, DecodeException>() {

			@Override
			public @Nullable T_Decoded run() throws DecodeException {
				return decoder.decode(context);
			}

			@Override
			public String toString() {
				return "Decoding " + context + " with " + decoder;
			}
		});
	}

	public <T_Encoded, T_Decoded> @NotNull T_Decoded construct(
		@NotNull AutoConstructor<T_Decoded> constructor,
		@NotNull ConstructContext<T_Encoded> context
	)
	throws ConstructException {
		return this.runTask(new LoggableTask<T_Decoded, ConstructException>() {

			@Override
			public @NotNull T_Decoded run() throws ConstructException {
				return constructor.construct(context);
			}

			@Override
			public String toString() {
				return "Constructing " + context + " with " + constructor;
			}
		});
	}

	public <T_Encoded, T_Decoded> void imprint(
		@NotNull AutoImprinter<T_Decoded> imprinter,
		@NotNull ImprintContext<T_Encoded, T_Decoded> context
	)
	throws ImprintException {
		this.runTask(new LoggableTask<T_Decoded, ImprintException>() {

			@Override
			public T_Decoded run() throws ImprintException {
				imprinter.imprint(context);
				return context.object;
			}

			@Override
			public String toString() {
				return "Imprinting " + context + " with " + imprinter;
			}
		});
	}

	public <T_Encoded, T_Decoded> void verify(
		@NotNull AutoVerifier<T_Decoded> verifier,
		@NotNull VerifyContext<T_Encoded, T_Decoded> context
	)
	throws VerifyException {
		this.runTask(new LoggableTask<String, VerifyException>() {

			@Override
			public String run() throws VerifyException {
				verifier.verify(context);
				return "OK";
			}

			@Override
			public String toString() {
				return "Verifying " + context + " with " + verifier;
			}
		});
	}



	//////////////// factories ////////////////



	public <T_Handler extends AutoHandler> @Nullable T_Handler tryCreateHandler(
		@NotNull AutoFactory<T_Handler> factory,
		@NotNull FactoryContext<?> context
	)
	throws FactoryException {
		return this.runTask(new LoggableTask<T_Handler, FactoryException>() {

			@Override
			public T_Handler run() throws FactoryException {
				return factory.tryCreate(context);
			}

			@Override
			public String toString() {
				return factory + " trying to create handler for " + context;
			}
		});
	}

	public <T_Handler extends AutoHandler> @NotNull T_Handler forceCreateHandler(
		@NotNull AutoFactory<T_Handler> factory,
		@NotNull FactoryContext<?> context
	)
	throws FactoryException {
		return this.runTask(new LoggableTask<T_Handler, FactoryException>() {

			@Override
			public T_Handler run() throws FactoryException {
				return factory.forceCreate(context);
			}

			@Override
			public String toString() {
				return factory + " forcibly creating handler for " + context;
			}
		});
	}

	public <
		T_Handler extends AutoHandler,
		T_Factory extends AutoFactory<T_Handler>
	>
	@Nullable T_Handler tryCreateFallbackHandler(
		@NotNull FactoryList<T_Handler, T_Factory> factoryList,
		@NotNull FactoryContext<?> context,
		@NotNull T_Factory caller
	)
	throws FactoryException {
		return this.runTask(new LoggableTask<T_Handler, FactoryException>() {

			@Override
			public T_Handler run() throws FactoryException {
				return factoryList.tryCreateFallback(context, caller);
			}

			@Override
			public String toString() {
				return factoryList + " trying to create fallback handler for " + context + "; caller: " + caller;
			}
		});
	}

	public <
		T_Handler extends AutoHandler,
		T_Factory extends AutoFactory<T_Handler>
	>
	@NotNull T_Handler forceCreateFallbackHandler(
		@NotNull FactoryList<T_Handler, T_Factory> factoryList,
		@NotNull FactoryContext<?> context,
		@NotNull T_Factory caller
	)
	throws FactoryException {
		return this.runTask(new LoggableTask<T_Handler, FactoryException>() {

			@Override
			public T_Handler run() throws FactoryException {
				return factoryList.forceCreateFallback(context, caller);
			}

			@Override
			public String toString() {
				return factoryList + " forcibly creating fallback handler for " + context + "; caller: " + caller;
			}
		});
	}



	//////////////// reflection ////////////////



	public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] getFields(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited
	)
	throws ReflectException {
		return this.runTask(new LoggableTask<FieldLikeMemberView<T_Owner, ?>[], ReflectException>() {

			@Override
			public FieldLikeMemberView<T_Owner, ?>[] run() throws ReflectException {
				return context.reflectionManager().getFields(context, inherited);
			}

			@Override
			public String toString() {
				return (
					new TypeFormatter(128)
					.annotations(false)
					.simplify(false)
					.append("Getting all ")
					.append(inherited ? "inherited" : "declared")
					.append(" fields in ")
					.append(context.owner)
					.toString()
				);
			}
		});
	}

	public <T_Owner, T_Collect> @Nullable T_Collect searchFields(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited,
		@NotNull Predicate<? super FieldLikeMemberView<T_Owner, ?>> predicate,
		@NotNull MemberCollector<FieldLikeMemberView<T_Owner, ?>, T_Collect> collector
	)
	throws ReflectException {
		return this.runTask(new LoggableTask<T_Collect, ReflectException>() {

			@Override
			public T_Collect run() throws ReflectException {
				return context.reflectionManager().searchFields(context, inherited, predicate, collector);
			}

			@Override
			public String toString() {
				return (
					new TypeFormatter(128)
					.annotations(false)
					.simplify(false)
					.append("Finding ")
					.append(inherited ? "inherited" : "declared")
					.append(' ')
					.append(collector.searchType("field"))
					.append(" in ")
					.append(context.owner)
					.append(" which matches the predicate ")
					.append(predicate.toString())
					.toString()
				);
			}
		});
	}

	public <T_Owner> @NotNull MethodLikeMemberView<T_Owner, ?> @NotNull [] getMethods(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited
	)
	throws ReflectException {
		return this.runTask(new LoggableTask<MethodLikeMemberView<T_Owner, ?>[], ReflectException>() {

			@Override
			public MethodLikeMemberView<T_Owner, ?>[] run() throws ReflectException {
				return context.reflectionManager().getMethods(context, inherited);
			}

			@Override
			public String toString() {
				return (
					new TypeFormatter(128)
					.annotations(false)
					.simplify(false)
					.append("Getting all ")
					.append(inherited ? "inherited" : "declared")
					.append(" methods in ")
					.append(context.owner)
					.toString()
				);
			}
		});
	}

	public <T_Owner, T_Collect> @Nullable T_Collect searchMethods(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited,
		@NotNull Predicate<? super MethodLikeMemberView<T_Owner, ?>> predicate,
		@NotNull MemberCollector<MethodLikeMemberView<T_Owner, ?>, T_Collect> collector
	)
	throws ReflectException {
		return this.runTask(new LoggableTask<T_Collect, ReflectException>() {

			@Override
			public T_Collect run() throws ReflectException {
				return context.reflectionManager().searchMethods(context, inherited, predicate, collector);
			}

			@Override
			public String toString() {
				return (
					new TypeFormatter(128)
					.annotations(false)
					.simplify(false)
					.append("Finding ")
					.append(inherited ? "inherited" : "declared")
					.append(' ')
					.append(collector.searchType("method"))
					.append(" in ")
					.append(context.owner)
					.append(" which matches the predicate ")
					.append(predicate.toString())
					.toString()
				);
			}
		});
	}
}