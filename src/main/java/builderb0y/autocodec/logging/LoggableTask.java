package builderb0y.autocodec.logging;

/**
a task that can be logged. see also: {@link TaskLogger}.
this is an abstract class instead of an interface for the
sole reason that I want to make {@link #toString()} abstract.
*/
public abstract class LoggableTask<R, X extends Throwable> {

	public abstract R run() throws X;

	@Override
	public abstract String toString();
}