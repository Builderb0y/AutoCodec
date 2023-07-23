package builderb0y.autocodec.decoders;

import java.util.*;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.DynamicOpsContext;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintContext;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;

public class DecodeContext<T_Encoded> extends DynamicOpsContext<T_Encoded> {

	public static final @NotNull ObjectArrayFactory<DecodeContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DecodeContext.class).generic();

	public final @Nullable DecodeContext<T_Encoded> parent;
	public final @NotNull DecodePath path;
	public final @NotNull T_Encoded input;

	public DecodeContext(
		@NotNull AutoCodec autoCodec,
		@Nullable DecodeContext<T_Encoded> parent,
		@NotNull DecodePath path,
		@NotNull T_Encoded input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, ops);
		this.parent = parent;
		this.path   = path;
		this.input  = input;
	}

	public DecodeContext(@NotNull DecodeContext<T_Encoded> context) {
		this(context.autoCodec, context.parent, context.path, context.input, context.ops);
	}

	@Override
	public @NotNull TaskLogger logger() {
		return this.autoCodec.decodeLogger;
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull T_Encoded input) {
		return this.input == input ? this : new DecodeContext<>(this.autoCodec, this.parent, this.path, input, this.ops);
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull T_Encoded input, @NotNull DecodePath nextPath) {
		return new DecodeContext<>(this.autoCodec, this, nextPath, input, this.ops);
	}

	//////////////////////////////// ops methods ////////////////////////////////

	public boolean isEmpty() {
		return Objects.equals(this.input, this.ops.empty());
	}

	public @NotNull DecodeException notA(@NotNull String type) {
		return new DecodeException(() -> this.pathToStringBuilder().append(" is not a ").append(type).append(": ").append(this.input).toString());
	}

	//////////////// map ////////////////

	public boolean isMap() {
		return this.ops.getMapValues(this.input).result().isPresent();
	}

	public @Nullable Map<@NotNull String, @NotNull DecodeContext<T_Encoded>> tryAsStringMap() {
		Stream<Pair<T_Encoded, T_Encoded>> stream = this.ops.getMapValues(this.input).result().orElse(null);
		return stream == null ? null : (
			stream
			.map((Pair<T_Encoded, T_Encoded> pair) -> {
				String key = this.ops.getStringValue(pair.getFirst()).result().orElse(null);
				if (key == null) throw AutoCodecUtil.rethrow(new DecodeException(() -> this.pathToStringBuilder().append(".<key> is not a string: ").append(pair.getFirst()).toString()));
				DecodeContext<T_Encoded> value = this.input(pair.getSecond(), new ObjectDecodePath(key));
				return Pair.of(key, value);
			})
			.collect(Pair.toMap())
		);
	}

	public @NotNull Map<@NotNull String, @NotNull DecodeContext<T_Encoded>> forceAsStringMap() throws DecodeException {
		Map<String, DecodeContext<T_Encoded>> map = this.tryAsStringMap();
		if (map != null) return map;
		else throw this.notA("map");
	}

	public @NotNull Map<@NotNull String, @NotNull DecodeContext<T_Encoded>> asStringMapOrEmpty() {
		Map<String, DecodeContext<T_Encoded>> map = this.tryAsStringMap();
		return map != null ? map : Collections.emptyMap();
	}

	public @Nullable Map<@NotNull DecodeContext<T_Encoded>, @NotNull DecodeContext<T_Encoded>> tryAsContextMap() {
		Stream<Pair<T_Encoded, T_Encoded>> stream = this.ops.getMapValues(this.input).result().orElse(null);
		return stream == null ? null : (
			stream
			.map((Pair<T_Encoded, T_Encoded> pair) -> {
				String keyName = this.ops.getStringValue(pair.getFirst()).result().orElse(null);
				if (keyName == null) throw AutoCodecUtil.rethrow(new DecodeException(() -> this.pathToStringBuilder().append(".<key> is not a string: ").append(pair.getFirst()).toString()));
				ObjectDecodePath path = new ObjectDecodePath(keyName);
				return Pair.of(
					this.input(pair.getFirst(), path),
					this.input(pair.getSecond(), path)
				);
			})
			.collect(Pair.toMap())
		);
	}

	public @NotNull Map<@NotNull DecodeContext<T_Encoded>, @NotNull DecodeContext<T_Encoded>> forceAsContextMap() throws DecodeException {
		Map<DecodeContext<T_Encoded>, DecodeContext<T_Encoded>> map = this.tryAsContextMap();
		if (map != null) return map;
		else throw this.notA("map");
	}

	public @NotNull Map<@NotNull DecodeContext<T_Encoded>, @NotNull DecodeContext<T_Encoded>> asContextMapOrEmpty() {
		Map<DecodeContext<T_Encoded>, DecodeContext<T_Encoded>> map = this.tryAsContextMap();
		return map != null ? map : Collections.emptyMap();
	}

	public @NotNull T_Encoded getPrimitiveMember(@NotNull String name) {
		return this.ops.get(this.input, name).result().orElse(this.ops.empty());
	}

	public @NotNull DecodeContext<T_Encoded> getMember(@NotNull String name) {
		return this.input(this.getPrimitiveMember(name), new ObjectDecodePath(name));
	}

	public @NotNull DecodeContext<T_Encoded> removeMember(@NotNull String name) {
		return this.input(this.ops.remove(this.input, name));
	}

	public @NotNull DecodeContext<T_Encoded> getFirstMember(@NotNull String @NotNull ... names) {
		DecodeContext<T_Encoded> result = this.getMember(names[0]);
		if (result.isEmpty()) {
			for (int index = 1, length = names.length; index < length; index++) {
				DecodeContext<T_Encoded> alternative = this.getMember(names[index]);
				if (!alternative.isEmpty()) return alternative;
			}
		}
		return result;
	}

	//////////////// list ////////////////

	public boolean isList() {
		return this.ops.getStream(this.input).result().isPresent();
	}

	public @Nullable List<@NotNull DecodeContext<T_Encoded>> tryAsList(boolean allowSingleton) {
		Stream<T_Encoded> stream = this.ops.getStream(this.input).result().orElse(null);
		if (stream == null) {
			return allowSingleton ? List.of(this) : null;
		}
		@SuppressWarnings("unchecked")
		T_Encoded[] primitiveArray = (T_Encoded[])(stream.toArray());
		int length = primitiveArray.length;
		DecodeContext<T_Encoded>[] contextArray = ARRAY_FACTORY.applyGeneric(length);
		for (int index = 0; index < length; index++) {
			contextArray[index] = this.input(primitiveArray[index], new ArrayDecodePath(index));
		}
		return Arrays.asList(contextArray);
	}

	public @NotNull List<@NotNull DecodeContext<T_Encoded>> forceAsList(boolean allowSingleton) throws DecodeException {
		List<@NotNull DecodeContext<T_Encoded>> list = this.tryAsList(allowSingleton);
		if (list != null) return list;
		else throw this.notA("list");
	}

	//////////////// number ////////////////

	public boolean isNumber() {
		return this.ops.getNumberValue(this.input).result().isPresent();
	}

	public @Nullable Number tryAsNumber() {
		return this.ops.getNumberValue(this.input).result().orElse(null);
	}

	public @NotNull Number forceAsNumber() throws DecodeException {
		Number number = this.tryAsNumber();
		if (number != null) return number;
		else throw this.notA("number");
	}

	//////////////// boolean ////////////////

	public boolean isBoolean() {
		return this.ops.getBooleanValue(this.input).result().isPresent();
	}

	public @Nullable Boolean tryAsBoolean() {
		return this.ops.getBooleanValue(this.input).result().orElse(null);
	}

	public @NotNull Boolean forceAsBoolean() throws DecodeException {
		Boolean value = this.tryAsBoolean();
		if (value != null) return value;
		else throw this.notA("boolean");
	}

	//////////////// string ////////////////

	public boolean isString() {
		return this.ops.getStringValue(this.input).result().isPresent();
	}

	public @Nullable String tryAsString() {
		return this.ops.getStringValue(this.input).result().orElse(null);
	}

	public @NotNull String forceAsString() throws DecodeException {
		String string = this.tryAsString();
		if (string != null) return string;
		else throw this.notA("string");
	}

	//////////////////////////////// handlers ////////////////////////////////

	public <T_Decoded> T_Decoded decodeWith(@NotNull AutoDecoder<T_Decoded> decoder) throws DecodeException {
		return this.logger().decode(decoder, this);
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
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.input + ", ops: " + this.ops + " }";
	}

	public static interface DecodePath {

		public abstract void appendTo(@NotNull StringBuilder builder);
	}

	public static enum RootDecodePath implements DecodePath {
		INSTANCE;

		@Override
		public void appendTo(@NotNull StringBuilder builder) {
			builder.append("<root>");
		}

		@Override
		public String toString() {
			return "<root>";
		}
	}

	public static record ObjectDecodePath(@NotNull String memberName) implements DecodePath {

		@Override
		public void appendTo(@NotNull StringBuilder builder) {
			if (!builder.isEmpty()) builder.append('.');
			builder.append(this.memberName);
		}

		@Override
		public String toString() {
			return this.memberName;
		}
	}

	public static record ArrayDecodePath(int index) implements DecodePath {

		@Override
		public void appendTo(@NotNull StringBuilder builder) {
			builder.append('[').append(this.index).append(']');
		}

		@Override
		public String toString() {
			return Integer.toString(this.index);
		}
	}
}