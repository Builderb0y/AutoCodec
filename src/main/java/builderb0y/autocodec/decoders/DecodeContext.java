package builderb0y.autocodec.decoders;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.DynamicOpsContext;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataReader;
import builderb0y.autocodec.data.ListData;
import builderb0y.autocodec.data.MapData;
import builderb0y.autocodec.fixers.AutoFixer;
import builderb0y.autocodec.fixers.DataFixContext;
import builderb0y.autocodec.fixers.DataFixException;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintContext;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;

public class DecodeContext<T_Encoded> extends DynamicOpsContext<T_Encoded> implements DataReader<T_Encoded, DecodeException> {

	public static final @NotNull ObjectArrayFactory<DecodeContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DecodeContext.class).generic();

	public final @Nullable DecodeContext<T_Encoded> parent;
	public final @NotNull DecodePath path;
	public final @NotNull Data<T_Encoded> input;

	public DecodeContext(
		@NotNull AutoCodec autoCodec,
		@Nullable DecodeContext<T_Encoded> parent,
		@NotNull DecodePath path,
		@NotNull Data<T_Encoded> input,
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

	public @NotNull DecodeContext<T_Encoded> input(@NotNull Data<T_Encoded> input) {
		return this.input == input ? this : new DecodeContext<>(this.autoCodec, this.parent, this.path, input, this.ops);
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull Data<T_Encoded> input, @NotNull DecodePath nextPath) {
		return new DecodeContext<>(this.autoCodec, this, nextPath, input, this.ops);
	}

	public @NotNull DecodeContext<T_Encoded> input(@NotNull String memberName, @NotNull Data<T_Encoded> member) {
		return this.input(member, new ObjectDecodePath(memberName));
	}

	public @NotNull DecodeContext<T_Encoded> input(int index, @NotNull Data<T_Encoded> element) {
		return this.input(element, new ArrayDecodePath(index));
	}

	@Override
	public @NotNull DecodeException notA(@NotNull String type) {
		return new DecodeException(() -> this.pathToStringBuilder().append(" is not a ").append(type).append(": ").append(this.input).toString());
	}

	@Override
	public @NotNull Data<T_Encoded> data() {
		return this.input;
	}

	@Override
	public @NotNull DecodeContext<T_Encoded> getElement(int index) throws DecodeException {
		ListData<T_Encoded> list = this.tryAsList();
		if (list != null) {
			Data<T_Encoded> element = list.value.get(index);
			return this.input(element, new ArrayDecodePath(index));
		}
		return this.input(this.empty(), new ArrayDecodePath(index));
	}

	@Override
	public @NotNull DecodeContext<T_Encoded> getMember(@NotNull String key) throws DecodeException {
		MapData<T_Encoded> map = this.tryAsMap();
		if (map != null) {
			Data<T_Encoded> value = map.value.get(this.createString(key));
			if (value != null) {
				return this.input(value, new ObjectDecodePath(key));
			}
		}
		return this.input(this.empty(), new ObjectDecodePath(key));
	}

	//////////////////////////////// handlers ////////////////////////////////

	public <T_Decoded> @NotNull DataFixContext<T_Encoded> fixWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataFixException {
		return this.logger().fix(fixer, new DataFixContext<>(this));
	}

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