package crawlercommons.robots_tag;

public final class ExceptionHandlers {
    /**
     * Throws all exceptions.
     *
     * @throws T if this method is invoked
     */
    public static <T extends RuntimeException> void throwing(T exception) {
        throw exception;
    }

    /**
     * Ignores all exceptions and does nothing.
     */
    public static <T extends RuntimeException> void ignoring(T exception) {}
}
