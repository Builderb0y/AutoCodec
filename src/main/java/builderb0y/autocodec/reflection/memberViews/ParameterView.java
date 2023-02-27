package builderb0y.autocodec.reflection.memberViews;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.TypeFormatter;
import builderb0y.autocodec.util.TypeFormatter.TypeFormatterAppendable;

public class ParameterView<T_Owner, T_Param> implements TypeFormatterAppendable {

	public static final @NotNull ObjectArrayFactory<ParameterView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(ParameterView.class).generic();

	public final @NotNull MethodLikeMemberView<T_Owner, ?> method;
	public final @Nullable String name;
	public final @NotNull ReifiedType<T_Param> type;
	public final int index;

	public ParameterView(
		@NotNull MethodLikeMemberView<T_Owner, ?> method,
		@Nullable String name,
		@NotNull ReifiedType<T_Param> type,
		int index
	) {
		this.method = method;
		this.name   = name;
		this.type   = type;
		this.index  = index;
	}

	public @NotNull MethodLikeMemberView<T_Owner, ?> getMethod() {
		return this.method;
	}

	public @Nullable String getName() {
		return this.name;
	}

	public boolean hasName() {
		return this.name != null;
	}

	public @NotNull ReifiedType<T_Param> getType() {
		return this.type;
	}

	public int getIndex() {
		return this.index;
	}

	@Override
	public void appendTo(TypeFormatter formatter) {
		formatter.append(this.getType());
		if (this.hasName()) {
			formatter.append(' ').append(this.getName());
		}
	}

	@Override
	public String toString() {
		return new TypeFormatter(64).annotations(true).simplify(true).append(this).toString();
	}
}