package kotlinx.ast.common.klass

import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstGroup
import kotlinx.ast.common.ast.AstNode

sealed class Klass() : AstGroup

data class KlassModifierGroup(val group: String)

data class KlassModifier(
    val modifier: String,
    val group: KlassModifierGroup
) : Klass() {
    override val description: String = "KlassModifier($modifier, ${group.group})"
}

val starProjection = KlassIdentifier("*")

fun List<KlassIdentifier>.identifierName(): String {
    return joinToString(
        separator = ".",
        transform = KlassIdentifier::rawName
    )
}

data class KlassIdentifier(
    val identifier: String,
    val parameter: List<KlassIdentifier> = emptyList(),
    val nullable: Boolean = false
) : Klass() {
    val rawName: String = listOfNotNull(
        identifier,
        if (parameter.isEmpty()) {
            null
        } else {
            parameter.map(KlassIdentifier::rawName).joinToString(
                prefix = "<",
                separator = ", ",
                postfix = ">"
            )
        },
        if (nullable) {
            "?"
        } else {
            null
        }
    ).joinToString("")

    override val description: String = "KlassIdentifier($rawName)"

    fun parameterizedBy(parameter: KlassIdentifier): KlassIdentifier {
        return copy(
            parameter = this.parameter + parameter
        )
    }

    fun parameterizedBy(vararg parameter: KlassIdentifier): KlassIdentifier {
        return parameter.fold(this, KlassIdentifier::parameterizedBy)
    }
}

data class KlassString(
    override val children: List<StringComponent>
) : Klass(), AstNode {
    constructor(vararg children: StringComponent) : this(children.toList())

    override val description: String = "KlassString"
}

data class KlassAnnotation(
    val identifier: List<KlassIdentifier>,
    val arguments: List<KlassDeclaration>
) : Klass(), AstNode {
    override val description: String = "KlassAnnotation(${identifier.identifierName()})"

    override val children: List<Ast> = arguments
}

data class KlassTypeParameter(
    val generic: KlassIdentifier,
    val base: KlassIdentifier?
) : Klass(), AstNode {
    override val description: String = "KlassTypeParameter"

    override val children: List<Ast> = listOfNotNull(
        generic,
        base
    )
}

data class KlassInheritance(
    val type: KlassIdentifier,
    val annotations: List<KlassAnnotation> = emptyList()
) : Klass(), AstNode {
    override val description: String = "KlassInheritance"
    override val children: List<Ast> = listOf(
        type
    ) + annotations
}

data class KlassDeclaration(
    val keyword: String,
    val identifier: KlassIdentifier? = null,
    val type: KlassIdentifier? = null,
    val annotations: List<KlassAnnotation> = emptyList(),
    val modifiers: List<KlassModifier> = emptyList(),
    val parameter: List<KlassDeclaration> = emptyList(),
    val typeParameters: List<KlassTypeParameter> = emptyList(),
    val inheritance: List<KlassInheritance> = emptyList(),
    val expressions: List<Ast> = emptyList()
) : Klass(), AstNode {
    override val description: String =
        listOfNotNull(
            keyword,
            identifier?.rawName,
            type?.rawName
        ).joinToString(" ", "KlassDeclaration(", ")")

    override val children: List<Ast> = listOf(
        annotations,
        modifiers,
        parameter,
        typeParameters,
        inheritance,
        expressions
    ).flatten()
}
