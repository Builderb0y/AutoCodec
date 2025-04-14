package builderb0y.autocodec.common;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataReader;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.fixers.AutoFixer;
import builderb0y.autocodec.fixers.DataFixContext;
import builderb0y.autocodec.fixers.DataFixException;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintContext;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;

/**
this class exists solely for {@link DataFixContext} to throw
{@link DataFixContext} instead of {@link DecodeException},
which it would not be able to do if it extended {@link DecodeContext}
directly due to conflicting implements clauses.
*/
public abstract class AbstractDecodeContext<T_Encoded, T_Exception extends Exception> extends DynamicOpsContext<T_Encoded> implements DataReader<T_Encoded, T_Exception> {

	public final @Nullable AbstractDecodeContext<T_Encoded, ?> parent;
	public final @NotNull DecodePath path;
	public final @NotNull Data<T_Encoded> input;

	public AbstractDecodeContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data<T_Encoded> input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, ops);
		this.parent = parent;
		this.path = path;
		this.input = input;
	}

	public AbstractDecodeContext(@NotNull AbstractDecodeContext<T_Encoded, ?> from) {
		this(from.autoCodec, from.parent, from.path, from.input, from.ops);
	}

	@Override
	public @NotNull TaskLogger logger() {
		return this.autoCodec.decodeLogger;
	}

	@Override
	public @NotNull Data<T_Encoded> data() {
		return this.input;
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
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.input + ", ops: " + this.ops + " }";
	}
}