package io.springreact.jsc;

/**
 * A Java Server Component: its {@link #render()} returns a UI tree built with the
 * {@link Html} DSL. Combine with {@code @LiveComponent}/{@code @LiveState}/
 * {@code @LiveAction} — after every action the server re-renders and streams the new tree
 * to the universal React client, which reconciles it. You never write JSX for the screen.
 */
public interface ServerComponent {

    UiNode render();
}
