package builderb0y.autocodec.constructors;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.UseImplementation;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor.NamedConstructorFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.reflection.reification.TypeClassification;

public class UseImplementationConstructorFactory extends NamedConstructorFactory {

	public static final UseImplementationConstructorFactory INSTANCE = new UseImplementationConstructorFactory();

	@Override
	public <T_HandledType> @Nullable AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		UseImplementation annotation = context.type.getAnnotations().getFirst(UseImplementation.class);
		if (annotation != null) {
			Class<?> implementationClass = annotation.value();
			ReifiedType<?> implementationType;
			ReifiedType<?>[] targets = context.type.getParameters();
			if (targets == null) targets = ReifiedType.ARRAY_FACTORY.empty();
			TypeVariable<? extends Class<?>>[] fromParameters = implementationClass.getTypeParameters();
			int parameterCount = fromParameters.length;
			if (parameterCount == 0) {
				//fast path: StringList implements List<String>.
				//in this case, there is nothing to parameterize.
				implementationType = ReifiedType.from(implementationClass);
			}
			else if (targets.length == 0) {
				//alternate fast path: GenericBox<T> extends Box.
				//in this case, it does not matter what T is, as it will not affect Box.
				//so, we can fill it in with a simple wildcard.
				implementationType = ReifiedType.parameterizeWithWildcards(implementationClass);
			}
			else {
				//slow path: HashMap<K, V> implements Map<K, V>.
				//in this case, we have a convenient way to resolve
				//HashMap<String, Integer> -> Map<String, Integer>,
				//but not the other way around.
				//so, we fill in the type parameters with placeholders, and resolve them.
				//HashMap<placeholder1, placeholder2> resolves to Map<placeholder1, placeholder2>.
				//then we go back to our target, Map<String, Integer> and say
				//placeholder1 should be substituted for String, and
				//placeholder2 should be substituted for Integer.
				//if there are any leftover type parameters, they are substituted for wildcards.
				ReifiedType<?>[] placeholders = new ReifiedType<?>[parameterCount];
				for (int index = 0; index < parameterCount; index++) {
					placeholders[index] = newPlaceholder(fromParameters[index]);
				}
				implementationType = ReifiedType.parameterize(implementationClass, placeholders);
				ReifiedType<?>[] resolutions = implementationType.resolveParameters(context.type.requireRawClass());
				if (resolutions == null) resolutions = ReifiedType.ARRAY_FACTORY.empty();
				assert resolutions.length == targets.length : "resolution length != target length";
				ReifiedType<?>[] newParameters = new ReifiedType<?>[parameterCount];
				Arrays.fill(newParameters, ReifiedType.WILDCARD);
				for (int resolutionIndex = 0, resolutionCount = resolutions.length; resolutionIndex < resolutionCount; resolutionIndex++) {
					int sourceIndex = indexOf(placeholders, resolutions[resolutionIndex]);
					if (sourceIndex >= 0) newParameters[sourceIndex] = targets[resolutionIndex];
				}
				implementationType = ReifiedType.parameterize(implementationClass, newParameters);
			}
			return context.type(implementationType).forceCreateConstructor();
		}
		return null;
	}

	/**
	I am expecting few enough parameters in practice that
	it's not worth using a Map-based implementation here.
	*/
	public static int indexOf(@NotNull ReifiedType<?> @NotNull [] placeholders, @NotNull ReifiedType<?> placeholder) {
		for (int index = 0, length = placeholders.length; index < length; index++) {
			if (placeholders[index].getUnresolvableVariable() == placeholder.getUnresolvableVariable()) return index;
		}
		return -1;
	}

	public static @NotNull ReifiedType<?> newPlaceholder(@NotNull TypeVariable<?> variable) {
		ReifiedType<?> placeholder = ReifiedType.blank();
		placeholder.classification = TypeClassification.UNRESOLVABLE_VARIABLE;
		placeholder.sharedType = ReifiedType.OBJECT;
		placeholder.unresolvableVariable = variable;
		return placeholder;
	}
}