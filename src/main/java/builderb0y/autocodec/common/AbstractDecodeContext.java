package builderb0y.autocodec.common;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.data.*;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeContext.ArrayDecodePath;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.decoders.DecodeContext.ObjectDecodePath;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.fixers.AutoFixer;
import builderb0y.autocodec.fixers.DataFixContext;
import builderb0y.autocodec.fixers.DataFixException;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintContext;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.StreamableIterable;
import builderb0y.autocodec.util.StreamableIterable.SingletonStreamableIterable;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;

public abstract class AbstractDecodeContext<
	T_Encoded,
	T_Exception extends Exception,
	T_Context extends AbstractDecodeContext<T_Encoded, T_Exception, T_Context>
>
extends DynamicOpsContext<T_Encoded>
implements DataReader<T_Exception> {

	public static final @NotNull ObjectArrayFactory<AbstractDecodeContext<?, ?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AbstractDecodeContext.class).generic();

	public final @Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent;
	public final @NotNull DecodePath path;
	public final @NotNull Data data;

	public AbstractDecodeContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data data,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, ops);
		this.parent = parent;
		this.path = path;
		this.data = data;
	}

	public AbstractDecodeContext(@NotNull AbstractDecodeContext<T_Encoded, ?, ?> from) {
		this(from.autoCodec, from.parent, from.path, from.data, from.ops);
	}

	@Override
	public @NotNull TaskLogger logger() {
		return this.autoCodec.decodeLogger;
	}

	@Override
	public @NotNull Data data() {
		return this.data;
	}

	@Internal
	public abstract @NotNull T_Context newContext(
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data input
	);

	@SuppressWarnings("unchecked")
	public @NotNull T_Context withData(@NotNull Data data) {
		return this.data == data ? (T_Context)(this) : this.newContext(this.parent, this.path, data);
	}

	public @NotNull T_Context fork(@NotNull DecodeContext.DecodePath nextPath, @NotNull Data data) {
		return this.newContext(this, nextPath, data);
	}

	public @NotNull T_Context fork(@NotNull String memberName, @NotNull Data member) {
		return this.fork(new ObjectDecodePath(memberName), member);
	}

	public @NotNull T_Context fork(int index, @NotNull Data element) {
		return this.fork(new ArrayDecodePath(index), element);
	}

	public abstract @NotNull T_Exception newException(@NotNull Supplier<@NotNull String> messageSupplier);

	@Override
	public @NotNull T_Exception notA(@NotNull String type) {
		return this.newException(() -> this.pathToStringBuilder().append(" is not a ").append(type).append(": ").append(this.data).toString());
	}

	@Override
	public @NotNull T_Context tryGetElement(int index) {
		ListData list = this.tryAsList();
		return this.fork(index, list != null ? list.get(index) : EmptyData.INSTANCE);
	}

	@Override
	public @NotNull T_Context tryGetMember(@NotNull String key) {
		MapData map = this.tryAsMap();
		return this.fork(key, map != null ? map.get(key) : EmptyData.INSTANCE);
	}

	@Override
	public @NotNull T_Context forceGetElement(int index) throws T_Exception {
		return this.fork(index, this.forceAsList().value.get(index));
	}

	@Override
	public @NotNull T_Context forceGetMember(@NotNull String key) throws T_Exception {
		return this.fork(key, this.forceAsMap().get(key));
	}

	@Internal
	public @NotNull StreamableIterable<@NotNull T_Context> createListIterable(@NotNull List<@NotNull Data> list) {
		return () -> {
			ListIterator<Data> iterator = list.listIterator();
			return new Iterator<>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public T_Context next() {
					return AbstractDecodeContext.this.fork(iterator.nextIndex(), iterator.next());
				}
			};
		};
	}

	@Override
	public @NotNull StreamableIterable<@NotNull T_Context> listIterable() throws T_Exception {
		return this.createListIterable(this.forceAsList().value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public @NotNull StreamableIterable<@NotNull T_Context> listIterableOrSingleton() throws T_Exception {
		ListData list = this.tryAsList();
		if (list != null) {
			return this.createListIterable(list.value);
		}
		else {
			return new SingletonStreamableIterable<>((T_Context)(this));
		}
	}

	@Override
	public @NotNull StreamableIterable<T_Context> listIterableMaybeSingleton(boolean singleton) throws T_Exception {
		return singleton ? this.listIterableOrSingleton() : this.listIterable();
	}

	@Override
	public @NotNull StreamableIterable<Map.@NotNull Entry<@NotNull T_Context, @NotNull T_Context>> mapIterable() throws T_Exception {
		Set<Map.Entry<Data, Data>> entrySet = this.forceAsMap().value.entrySet();
		return new StreamableIterable<>() {

			@Override
			public @NotNull Iterator<Map.Entry<T_Context, T_Context>> iterator() {
				Iterator<Map.Entry<Data, Data>> iterator = entrySet.iterator();
				return new Iterator<>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Map.Entry<T_Context, T_Context> next() {
						Map.Entry<Data, Data> next = iterator.next();
						return Map.entry(
							AbstractDecodeContext.this.fork("<key>", next.getKey()),
							AbstractDecodeContext.this.fork(next.getKey().toString(), next.getValue())
						);
					}
				};
			}

			@Override
			public @NotNull Stream<Map.Entry<T_Context, T_Context>> stream() {
				return entrySet.stream().map((Map.Entry<Data, Data> entry) -> Map.entry(
					AbstractDecodeContext.this.fork("<key>", entry.getKey()),
					AbstractDecodeContext.this.fork(entry.getKey().toString(), entry.getValue())
				));
			}
		};
	}

	//////////////////////////////// handlers ////////////////////////////////

	public <T_Decoded> @NotNull DataFixContext<T_Encoded> fixDataWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataFixException {
		return this.logger().fixData(fixer, new DataFixContext<>(this));
	}

	public <T_Decoded> T_Decoded decodeWith(@NotNull AutoDecoder<T_Decoded> decoder) throws DecodeException {
		return this.logger().decode(decoder, new DecodeContext<>(this));
	}

	public <T_Decoded> @NotNull T_Decoded constructWith(@NotNull AutoConstructor<T_Decoded> constructor) throws ConstructException {
		return this.logger().construct(constructor, new ConstructContext<>(this));
	}

	public <T_Decoded> void imprintWith(@NotNull AutoImprinter<T_Decoded> imprinter, @NotNull T_Decoded object) throws ImprintException {
		this.logger().imprint(imprinter, new ImprintContext<>(this, object));
	}

	public <T_Decoded> void verifyWith(@NotNull AutoVerifier<T_Decoded> verifier, @Nullable T_Decoded object) throws VerifyException {
		this.logger().verify(verifier, new VerifyContext<>(this, object));
	}

	//////////////////////////////// toString ////////////////////////////////

	public void appendPathTo(@NotNull StringBuilder builder) {
		if (this.parent != null) this.parent.appendPathTo(builder);
		this.path.appendTo(builder);
	}

	public @NotNull StringBuilder pathToStringBuilder() {
		StringBuilder builder = new StringBuilder(64);
		this.appendPathTo(builder);
		return builder;
	}

	public @NotNull String pathToString() {
		return this.pathToStringBuilder().toString();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.data + ", ops: " + this.ops + " }";
	}
}