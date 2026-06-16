package io.springreact.jsc

/**
 * Computes a minimal list of patch operations to turn one [UiNode] tree into another.
 * Operations are addressed by a path of child indices from the root.
 *
 * Op shapes (all carry `op` + `path`):
 * - `text`    `{value}`            a text leaf changed
 * - `props`   `{set, remove}`      props added/changed/removed
 * - `insert`  `{index, node}`      insert a child at path
 * - `remove`  `{index}`            remove a child at path
 * - `replace` `{node}`             replace the node at path entirely
 * - `keyed`   `{items:[{key,node?}]}`  reorder/insert/remove keyed children by key
 *
 * When every child of an element carries a `key` prop, children are reconciled by key
 * (stable across reorders/removals) instead of by index.
 */
object UiTreeDiff {

    @JvmStatic
    fun diff(oldNode: UiNode, newNode: UiNode): List<Map<String, Any?>> {
        val ops = ArrayList<Map<String, Any?>>()
        diffNode(oldNode, newNode, ArrayDeque(), ops)
        return ops
    }

    private fun diffNode(
        oldNode: UiNode,
        newNode: UiNode,
        path: ArrayDeque<Int>,
        ops: MutableList<Map<String, Any?>>,
    ) {
        if (oldNode is Text && newNode is Text) {
            if (oldNode.value != newNode.value) ops.add(op("text", path, mapOf("value" to newNode.value)))
            return
        }
        if (oldNode is Element && newNode is Element && oldNode.tag == newNode.tag) {
            diffProps(oldNode, newNode, path, ops)
            if (keyed(oldNode) && keyed(newNode)) {
                diffKeyedChildren(oldNode, newNode, path, ops)
            } else {
                diffIndexedChildren(oldNode, newNode, path, ops)
            }
            return
        }
        ops.add(op("replace", path, mapOf("node" to newNode.toJson())))
    }

    private fun keyed(el: Element): Boolean =
        el.children.isNotEmpty() && el.children.all { it is Element && it.props["key"] != null }

    private fun diffIndexedChildren(
        oldEl: Element,
        newEl: Element,
        path: ArrayDeque<Int>,
        ops: MutableList<Map<String, Any?>>,
    ) {
        val common = minOf(oldEl.children.size, newEl.children.size)
        for (i in 0 until common) {
            path.addLast(i)
            diffNode(oldEl.children[i], newEl.children[i], path, ops)
            path.removeLast()
        }
        for (i in common until newEl.children.size) {
            ops.add(op("insert", path, mapOf("index" to i, "node" to newEl.children[i].toJson())))
        }
        for (i in oldEl.children.size - 1 downTo common) {
            ops.add(op("remove", path, mapOf("index" to i)))
        }
    }

    private fun diffKeyedChildren(
        oldEl: Element,
        newEl: Element,
        path: ArrayDeque<Int>,
        ops: MutableList<Map<String, Any?>>,
    ) {
        val oldByKey = HashMap<Any?, Pair<Int, Element>>()
        oldEl.children.forEachIndexed { i, c -> oldByKey[(c as Element).props["key"]] = i to c }

        // Patch kept children in place, addressed by their OLD index (applied before reorder).
        for (child in newEl.children) {
            child as Element
            val existing = oldByKey[child.props["key"]] ?: continue
            path.addLast(existing.first)
            diffNode(existing.second, child, path, ops)
            path.removeLast()
        }

        // Only reorder/insert/remove if the key sequence actually changed.
        val oldKeys = oldEl.children.map { (it as Element).props["key"] }
        val newKeys = newEl.children.map { (it as Element).props["key"] }
        if (oldKeys != newKeys) {
            val items = newEl.children.map { child ->
                child as Element
                val key = child.props["key"]
                if (oldByKey.containsKey(key)) mapOf("key" to key)
                else mapOf("key" to key, "node" to child.toJson())
            }
            ops.add(op("keyed", path, mapOf("items" to items)))
        }
    }

    private fun diffProps(
        oldEl: Element,
        newEl: Element,
        path: ArrayDeque<Int>,
        ops: MutableList<Map<String, Any?>>,
    ) {
        val set = LinkedHashMap<String, Any?>()
        val remove = ArrayList<String>()
        for ((k, v) in newEl.props) if (oldEl.props[k] != v) set[k] = v
        for (k in oldEl.props.keys) if (!newEl.props.containsKey(k)) remove.add(k)
        if (set.isNotEmpty() || remove.isNotEmpty()) {
            val body = LinkedHashMap<String, Any?>()
            if (set.isNotEmpty()) body["set"] = set
            if (remove.isNotEmpty()) body["remove"] = remove
            ops.add(op("props", path, body))
        }
    }

    private fun op(type: String, path: ArrayDeque<Int>, body: Map<String, Any?>): Map<String, Any?> {
        val m = LinkedHashMap<String, Any?>()
        m["op"] = type
        m["path"] = ArrayList(path)
        m.putAll(body)
        return m
    }
}
