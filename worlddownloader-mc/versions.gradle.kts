// Source of truth for the full version x loader matrix.
// Generated from an ordered version list + per-loader availability windows — never hand-typed.
// Applied (via `apply(from = ...)`) into the root build; exposes `allRows` and `proofIds` on extra.

val allVersions = listOf(
    "1.7.10",
    "1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9",
    "1.9", "1.9.1", "1.9.2", "1.9.3", "1.9.4",
    "1.10", "1.10.1", "1.10.2", "1.11", "1.11.1", "1.11.2", "1.12", "1.12.1", "1.12.2",
    "1.13", "1.13.1", "1.13.2",
    "1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4", "1.15", "1.15.1", "1.15.2",
    "1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5", "1.17", "1.17.1",
    "1.18", "1.18.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
    "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2", "26.2"
)

fun mcIdx(v: String): Int {
    val i = allVersions.indexOf(v)
    require(i >= 0) { "unknown MC version: $v" }
    return i
}

// Loader availability windows (inclusive), verified against each loader's release history:
//   forge        : 1.7.10 .. 1.21.x   (26.x Forge unconfirmed -> NeoForge covers 26.x)
//   legacyfabric : 1.7.10 .. 1.13.2   (pre-official-Fabric)
//   fabric       : 1.14   .. latest
//   quilt        : 1.18.2 .. latest
//   neoforge     : 1.20.2 .. latest
fun loadersFor(v: String): List<String> {
    val i = mcIdx(v)
    val out = mutableListOf<String>()
    if (i <= mcIdx("1.21.11")) out += "forge"
    if (i <= mcIdx("1.13.2")) out += "legacyfabric"
    if (i >= mcIdx("1.14")) out += "fabric"
    if (i >= mcIdx("1.18.2")) out += "quilt"
    if (i >= mcIdx("1.20.2")) out += "neoforge"
    return out
}

val proofIds = listOf("1.8.9-forge", "1.20.1-fabric", "26.2-neoforge")

// Every valid (version, loader) pair = one matrix row id "<version>-<loader>".
val allRows: List<String> = allVersions.flatMap { v -> loadersFor(v).map { l -> "$v-$l" } }

extra["allRows"] = allRows
extra["proofIds"] = proofIds
