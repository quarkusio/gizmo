package io.quarkus.gizmo;

import java.util.List;
import java.util.function.Consumer;

/**
 * A switch statement.
 * <p>
 * This construct is not thread-safe and should not be re-used.
 *
 * @param <T> Constant type
 */
public interface Switch<T> {

    /**
     * Enables fall through.
     * <p>
     * By default, the fall through is disabled. A case block is treated as a switch rule block; i.e. it's not necessary to add
     * the break statement to prevent the fall through. However, if fall through is enabled then a case block is treated as a
     * labeled statement group; i.e. it's necessary to add the break statement to prevent the fall through.
     * <p>
     * For example, if fall through is disabled then:
     * 
     * <pre>
     * <code>
     * StringSwitch s = method.stringSwitch(val);
       s.caseOf(List.of("boom", "foo"), bc -> {...});
     * </code>
     * </pre>
     * 
     * is an equivalent of:
     * 
     * <pre>
     * switch (val) {
     *     case "boom", "foo" -> // statements provided by the consumer
     * }
     * </pre>
     * 
     * However, if fall though is enabled then:
     * 
     * <pre>
     * <code>
     * StringSwitch s = method.stringSwitch(val);
     * s.fallThrough();
     * s.caseOf(List.of("boom", "foo"), bc -> {...});
     * </code>
     * </pre>
     * 
     * is an equivalent of:
     * 
     * <pre>
     * switch (val) {
     *     case "val1":
     *     case "val2":
     *         // statements provided by the consumer
     * }
     * </pre>
     */
    void fallThrough();

    /**
     * Adds a case block.
     * 
     * @param value The value for the case label
     * @param caseBlockConsumer The consumer used to define the case block
     * @throws IllegalArgumentException If a case block for the specified value was already added
     */
    void caseOf(T value, Consumer<BytecodeCreator> caseBlockConsumer);

    /**
     * Adds multiple case labels for a single block.
     * 
     * @param values
     * @param caseBlockConsumer
     * @throws IllegalArgumentException If a case block for the specified value was already added
     */
    void caseOf(List<T> values, Consumer<BytecodeCreator> caseBlockConsumer);

    /**
     * Adds the default block.
     * 
     * @param defatultBlockConsumer
     */
    void defaultCase(Consumer<BytecodeCreator> defatultBlockConsumer);

    /**
     * Writes bytecode into the provided {@link BytecodeCreator} to make it exit the
     * switch, effectively issuing a Java 'break' statement.
     *
     * @param creator
     * @see #fallThrough()
     */
    void doBreak(BytecodeCreator creator);

    /**
     * A switch for {@link String}.
     */
    interface StringSwitch extends Switch<String> {
    }

    /**
     * A switch for {@link Enum}.
     */
    interface EnumSwitch<E extends Enum<E>> extends Switch<E> {
    }

}
