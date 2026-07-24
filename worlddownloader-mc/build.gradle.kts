// Root is config-only; each anchor build lives under versions/<id>/.
apply(from = "versions.gradle.kts")

@Suppress("UNCHECKED_CAST")
val allRows = extra["allRows"] as List<String>
@Suppress("UNCHECKED_CAST")
val proofIds = extra["proofIds"] as List<String>

tasks.register("anchors") {
    doLast { println("Buildable anchors (A proof): ${proofIds.joinToString(", ")}") }
}

tasks.register("matrixCheck") {
    doLast {
        println("matrix rows (version x loader): ${allRows.size}")
        require(proofIds.all { it in allRows }) { "proof id missing from generated matrix" }
        println("proof ids present in matrix: ok")
    }
}

tasks.register("matrixList") {
    doLast {
        val byLoader = allRows.groupBy { it.substringAfterLast('-') }
        byLoader.toSortedMap().forEach { (loader, rows) ->
            println("=== $loader (${rows.size}) ===")
            println(rows.joinToString(" ") { it.substringBeforeLast('-') })
        }
        println("TOTAL: ${allRows.size} (version x loader) build targets")
    }
}
