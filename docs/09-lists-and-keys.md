# 9. Lists & Keys

When you render a list, give each item a stable `key`. This lets SpringReact update lists
efficiently (and keeps things like input focus and animations stable).

## Without keys (fine for static lists)

```kotlin
ul(items.map { li(it.text) })
```

If the list never reorders and items aren't inserted/removed in the middle, this is fine.
SpringReact diffs by position.

## With keys (recommended for dynamic lists)

```kotlin
ul(items.map { li(key(it.id), it.text) })
```

Now SpringReact matches items by `key` across renders. If you remove the first item, it
sends a tiny "the list is now [2,3]" patch instead of rewriting every row.

## Why it matters

Removing item #1 from `[1,2,3]`:

| | Without keys | With keys |
|---|---|---|
| What's sent | rewrite row 1→2, row 2→3, drop row 3 | "keep keys 2 and 3, in this order" |
| React identity | rows shift, can lose focus/animation state | each row keeps its identity |

## Rules of thumb

- Use a **stable, unique** key per item — a database id, not the array index.
- Use keys when items can be **added, removed, or reordered**.
- Keys must be unique among siblings.

## Example: a reorderable list

```kotlin
@LiveComponent("Tasks")
class TasksScreen : ServerComponent {
    @LiveState var tasks = mutableListOf(Task(1, "A"), Task(2, "B"), Task(3, "C"))

    @LiveAction fun remove(id: Int) { tasks.removeIf { it.id == id } }
    @LiveAction fun moveUp(id: Int) { /* reorder */ }

    override fun render(): UiNode =
        ul(cls("list"), tasks.map { t ->
            li(key(t.id),                       // <-- stable key
               span(t.title),
               button(onClick("moveUp", t.id), "↑"),
               button(onClick("remove", t.id), "✕"))
        })

    data class Task(val id: Int, val title: String)
}
```

## TODO checklist

- [ ] Add `key(item.id)` to list children
- [ ] Remove/reorder items and confirm it still works
- [ ] Next: [Configuration](10-configuration.md)
