package io.springreact.jsc

/**
 * Computes a minimal list of patch operations to turn one [UiNode] tree into another, so
 * the server streams just the changes instead of the whole tree on every update.
 * Operations are addressed by a path of child indices from the root.
 *
 * Op shapes (all carry `op` + `path`):
 * - `text`    `{value}`        a text leaf changed
 * - `props`   `{set, remove}`  props added/changed/removed
 * - `insert`  `{index, node}`  insert a child at path
 * - `remove`  `{index}`        remove a child at path
 * - `replace` `{node}`         replace the node at path entirely
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
            val common = minOf(oldNode.children.size, newNode.children.size)
            for (i in 0 until common) {
                path.addLast(i)
                diffNode(oldNode.children[i], newNode.children[i], path, ops)
                path.removeLast()
            }
            for (i in common until newNode.children.size) {
                ops.add(op("insert", path, mapOf("index" to i, "node" to newNode.children[i].toJson())))
            }
            for (i in oldNode.children.size - 1 downTo common) {
                ops.add(op("remove", path, mapOf("index" to i)))
            }
            return
        }
        ops.add(op("replace", path, mapOf("node" to newNode.toJson())))
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
