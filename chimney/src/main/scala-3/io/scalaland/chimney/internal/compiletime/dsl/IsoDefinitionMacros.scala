package io.scalaland.chimney.internal.compiletime.dsl

import io.scalaland.chimney.dsl.*
import io.scalaland.chimney.internal.compiletime.dsl.utils.DslMacroUtils
import io.scalaland.chimney.internal.runtime.{Path, TransformerFlags, TransformerOverrides}
import io.scalaland.chimney.internal.runtime.TransformerOverrides.*

import scala.quoted.*

object IsoDefinitionMacros {

  def withFieldRenamedImpl[
      First: Type,
      Second: Type,
      FirstOverrides <: TransformerOverrides: Type,
      SecondOverrides <: TransformerOverrides: Type,
      Flags <: TransformerFlags: Type,
      T: Type,
      U: Type
  ](
      id: Expr[IsoDefinition[First, Second, FirstOverrides, SecondOverrides, Flags]],
      selectorFirst: Expr[First => T],
      selectorSecond: Expr[Second => U]
  )(using Quotes): Expr[IsoDefinition[First, Second, ? <: TransformerOverrides, ? <: TransformerOverrides, Flags]] =
    DslMacroUtils().applyFieldNameTypes {
      [firstPath <: Path, secondPath <: Path] =>
        (_: Type[firstPath]) ?=>
          (_: Type[secondPath]) ?=>
            '{
              $id.asInstanceOf[IsoDefinition[
                First,
                Second,
                RenamedFrom[firstPath, secondPath, FirstOverrides],
                RenamedFrom[secondPath, firstPath, SecondOverrides],
                Flags
              ]]
          }
    }(selectorFirst, selectorSecond)
}
