package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.indexer.*
import org.junit.Test
import kotlin.test.assertEquals

class ManglingSmokeTests {

    private val mangler: InteropMangler = KotlinLikeInteropMangler()

    @Test
    fun `typedef should not affect mangling`() {

        val fakeLocation = Location(HeaderId("zzzz"))
        val cBoolTypedef = Typedef(TypedefDef(CBoolType, "MyBool", fakeLocation))

        val functions = listOf(
                FunctionDecl("a", listOf(Parameter("a", CBoolType, false)), CBoolType, "", false, false),
                FunctionDecl("a", listOf(Parameter("b", CBoolType, false)), cBoolTypedef, "", false, false),
                FunctionDecl("a", listOf(Parameter("a", cBoolTypedef, false)), CBoolType, "", false, false),
                FunctionDecl("a", listOf(Parameter("a", cBoolTypedef, false)), cBoolTypedef, "", false, false)
        )
        with (mangler) {
            functions.reduce { left, right ->
                assertEquals(left.uniqueSymbolName, right.uniqueSymbolName)
                left
            }
        }
    }

    @Test
    fun `mangling should not depend on parameter names`() {

        val functionDeclarationA = FunctionDecl("a", listOf(Parameter("a", CBoolType, false)), CBoolType, "", false, false)
        val functionDeclarationB = FunctionDecl("a", listOf(Parameter("b", CBoolType, false)), CBoolType, "", false, false)

        with (mangler) {
            assertEquals(functionDeclarationA.uniqueSymbolName, functionDeclarationB.uniqueSymbolName)
        }
    }
}