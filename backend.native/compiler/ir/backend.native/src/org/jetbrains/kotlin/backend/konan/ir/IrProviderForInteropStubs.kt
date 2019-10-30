package org.jetbrains.kotlin.backend.konan.ir

import org.jetbrains.kotlin.backend.konan.descriptors.isFromInteropLibrary
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyDeclarationBase
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * Generates external IR declarations for descriptors from interop libraries.
 */
class IrProviderForInteropStubs(
        private val symbolTable: SymbolTable,
        private val typeTranslator: TypeTranslator
) : LazyIrProvider {

    override lateinit var declarationStubGenerator: DeclarationStubGenerator

    override fun getDeclaration(symbol: IrSymbol): IrLazyDeclarationBase? =
            if (symbol.descriptor.module.isFromInteropLibrary()) {
                provideIrDeclaration(symbol)
            } else {
                null
            }

    private fun provideIrDeclaration(symbol: IrSymbol): IrLazyDeclarationBase = when (symbol) {
        is IrSimpleFunctionSymbol -> provideIrFunction(symbol)
        else -> error("Unsupported interop declaration: symbol=$symbol, descriptor=${symbol.descriptor}")
    }

    private fun provideIrFunction(symbol: IrSimpleFunctionSymbol): IrLazyFunction =
            symbolTable.declareSimpleFunction(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB,
                    symbol.descriptor, this::createFunctionDeclaration
            ) as IrLazyFunction

    private fun createFunctionDeclaration(symbol: IrSimpleFunctionSymbol) =
            IrLazyFunction(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB,
                    symbol, declarationStubGenerator, typeTranslator
            )
}