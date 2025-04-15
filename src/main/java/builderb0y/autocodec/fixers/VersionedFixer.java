package builderb0y.autocodec.fixers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.MapData;
import builderb0y.autocodec.data.StringData;
import builderb0y.autocodec.fixers.AutoFixer.NamedFixer;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public abstract class VersionedFixer<T_Decoded> extends NamedFixer<T_Decoded> {

	public final @NotNull String versionKey;
	public final int version;

	public VersionedFixer(@NotNull ReifiedType<T_Decoded> handledType, @NotNull String versionKey, int version) {
		super(handledType);
		this.versionKey = versionKey;
		this.version = version;
	}

	public VersionedFixer(@NotNull String toString, @NotNull String versionKey, int version) {
		super(toString);
		this.versionKey = versionKey;
		this.version = version;
	}

	public VersionedFixer(@NotNull ReifiedType<T_Decoded> handledType, int version) {
		super(handledType);
		this.versionKey = "version";
		this.version = version;
	}

	public VersionedFixer(@NotNull String toString, int version) {
		super(toString);
		this.versionKey = "version";
		this.version = version;
	}

	public abstract <T_Encoded> @NotNull DataFixContext<T_Encoded> fixData(@NotNull DataFixContext<T_Encoded> context, int version) throws DataFixException;

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull DataFixContext<T_Encoded> fixData(@NotNull DataFixContext<T_Encoded> context) throws DataFixException {
		MapData map = context.forceAsMap();
		Data versionData = map.value.remove(new StringData(this.versionKey));
		if (versionData == null) throw new DataFixException(() -> "Missing version!");
		return this.fixData(context, context.input(this.versionKey, versionData).forceAsInt());
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull DataAppendContext<T_Encoded, T_Decoded> appendData(@NotNull DataAppendContext<T_Encoded, T_Decoded> context) throws DataAppendException {
		context.putInt("version", this.version);
		return super.appendData(context);
	}
}