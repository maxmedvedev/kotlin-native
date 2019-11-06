package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.indexer.*

/**
 * We need a way to refer external declarations from Kotlin Libraries
 * by stable unique identifier. To be able to do it, we mangle them.
 */
interface InteropMangler {
    val StructDecl.uniqueSymbolName: String
    val EnumDef.uniqueSymbolName: String
    val ObjCClass.uniqueSymbolName: String
    val ObjCProtocol.uniqueSymbolName: String
    val ObjCCategory.uniqueSymbolName: String
    val TypedefDef.uniqueSymbolName: String
    val FunctionDecl.uniqueSymbolName: String
    val ConstantDef.uniqueSymbolName: String
    val WrappedMacroDef.uniqueSymbolName: String
    val GlobalDecl.uniqueSymbolName: String
}

/**
 * Mangler that mimics behaviour of the one from the Kotlin compiler.
 */
class KotlinLikeInteropMangler : InteropMangler {

    override val StructDecl.uniqueSymbolName: String
        get() = "ktype:"

    override val EnumDef.uniqueSymbolName: String
        get() = "ktype:"

    override val ObjCClass.uniqueSymbolName: String
        get() = "ktype:"

    override val ObjCProtocol.uniqueSymbolName: String
        get() = "ktype:"

    override val ObjCCategory.uniqueSymbolName: String
        get() = TODO("not implemented")

    override val TypedefDef.uniqueSymbolName: String
        get() = "ktypealias:"

    override val FunctionDecl.uniqueSymbolName: String
        get() = "kfun:$functionName$signature"

    override val ConstantDef.uniqueSymbolName: String
        get() = TODO("not implemented")

    override val WrappedMacroDef.uniqueSymbolName: String
        get() = TODO("not implemented")

    override val GlobalDecl.uniqueSymbolName: String
        get() = TODO("not implemented")

    private val FunctionDecl.functionName: String
        get() = name

    private val FunctionDecl.signature: String
        get() = "${returnType.mangle}${parameters.joinToString { it.type.mangle }}"

    private val Type.mangle: String
        get() = when (this) {
            CharType, is BoolType -> "char"
            is IntegerType -> spelling
            is FloatingType -> spelling
            is RecordType -> decl.spelling
            is EnumType -> def.baseType.mangle
            is PointerType -> "void*"
            is ConstArrayType -> elemType.mangle
            is IncompleteArrayType -> elemType.mangle
            is Typedef -> def.aliased.mangle
            is ObjCPointer -> "void*"
            else -> error("Unexpected type $this")
        }
}