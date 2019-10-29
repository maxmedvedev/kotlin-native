package org.jetbrains.kotlin.backend.konan.ir

import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.backend.konan.descriptors.isFromIrLessLibrary
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.ir.util.IrProvider
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * Generates external IR declarations for descriptors from interop libraries.
 */
class IrProviderForInteropStubs(
        private val symbolTable: SymbolTable,
        private val typeTranslator: TypeTranslator
) : IrProvider {
    private val interopFakeFiles = mutableMapOf<PackageFragmentDescriptor, IrFile>()

    private val PackageFragmentDescriptor.fakeFile: IrFile get() = interopFakeFiles.getOrPut(this) {
            val symbol = IrFileSymbolImpl(this)
            IrFileImpl(NaiveSourceBasedFileEntryImpl("Pseudo-file for $fqName"), symbol)
        }

    override fun getDeclaration(symbol: IrSymbol): IrDeclaration? =
            if (symbol.descriptor.module.isFromIrLessLibrary()) {
                provideIrDeclaration(symbol)
            } else {
                null
            }

    private fun provideIrDeclaration(symbol: IrSymbol): IrDeclaration = when (symbol) {
        is IrSimpleFunctionSymbol -> provideIrFunction(symbol)
        else -> error("Unsupported interop declaration: symbol=$symbol, descriptor=${symbol.descriptor}")
    }

    private fun provideIrFunction(symbol: IrSimpleFunctionSymbol): IrFunction =
            symbolTable.declareSimpleFunction(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB,
                    symbol.descriptor, this::createExternalFunctionDeclaration
            )

    private fun createExternalFunctionDeclaration(symbol: IrSimpleFunctionSymbol): IrFunctionImpl {
        val function = IrFunctionImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB, symbol,
                typeTranslator.translateType(symbol.descriptor.returnType!!)
        )
        function.annotations += symbol.descriptor.annotations
                .mapNotNull(typeTranslator.constantValueGenerator::generateAnnotationConstructorCall)
        function.parent = symbol.descriptor.findPackage().fakeFile.also { it.declarations += function }
        return function
    }
}