package builderb0y.autocodec.reflection;

import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.common.TaskContext;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class ReflectContext<T_Owner> extends TaskContext implements ReflectContextProvider {

	public final @NotNull ReifiedType<T_Owner> owner;

	public ReflectContext(@NotNull AutoCodec autoCodec, @NotNull ReifiedType<T_Owner> owner) {
		super(autoCodec);
		this.owner = owner;
	}

	@Override
	@SuppressWarnings("unchecked")
	public @NotNull <T_NewOwner> ReflectContext<T_NewOwner> reflect(@NotNull ReifiedType<T_NewOwner> type) {
		if (ReifiedType.ORDERED_ANNOTATIONS_STRATEGY.equals(this.owner, type)) {
			return (ReflectContext<T_NewOwner>)(this);
		}
		return new ReflectContext<>(this.autoCodec, type);
	}

	@Override
	public @NotNull TaskLogger logger() {
		return this.autoCodec.factoryLogger;
	}

	public @NotNull ReflectionManager reflectionManager() {
		return this.autoCodec.reflectionManager;
	}

	public MethodHandles.@NotNull Lookup lookup() {
		return this.reflectionManager().getLookup(this.owner);
	}

	public @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] getFields(boolean inherited) {
		return this.logger().getFields(this, inherited);
	}

	public <T_Collect> T_Collect searchFields(
		boolean inherited,
		@NotNull Predicate<? super FieldLikeMemberView<T_Owner, ?>> predicate,
		@NotNull MemberCollector<FieldLikeMemberView<T_Owner, ?>, T_Collect> collector
	) {
		return this.logger().searchFields(this, inherited, predicate, collector);
	}

	public @NotNull MethodLikeMemberView<T_Owner, ?> @NotNull [] getMethods(boolean inherited) {
		return this.logger().getMethods(this, inherited);
	}

	public <T_Collect> T_Collect searchMethods(
		boolean inherited,
		@NotNull Predicate<? super MethodLikeMemberView<T_Owner, ?>> predicate,
		@NotNull MemberCollector<MethodLikeMemberView<T_Owner, ?>, T_Collect> collector
	) {
		return this.logger().searchMethods(this, inherited, predicate, collector);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { owner: " + this.owner + " }";
	}
}