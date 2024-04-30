//> using scala 3.3.3

import java.io.File
import java.nio.file.{Files, Paths}
import scala.Console.{MAGENTA, RESET}
import scala.collection.immutable.ListMap
import scala.util.Using
import scala.sys.process.*

// used for local development

var gitTag: String = ""
var tmpDir = Files.createTempDirectory(s"docs-snippets").toFile()

// config

def resolveVersion(): String =
  if gitTag.matches(".+-[0-9]+-[0-9a-z]{8}") then gitTag + "-SNAPSHOT"
  else gitTag

lazy val patterns = Map(
  // keeps in sync with what sbt produces
  "{{ chimney_version() }}" -> resolveVersion(),
  // keep in sync with mkdocs.yml
  "{{ libraries.ducktape }}" -> "0.2.0",
  "{{ libraries.henkan }}" -> "0.6.5",
  "{{ libraries.scala_automapper }}" -> "0.7.0",
  "{{ scala.2_12 }}" -> "2.12.18",
  "{{ scala.2_13 }}" -> "2.13.13",
  "{{ scala.3 }}" -> "3.3.3"
)

enum SpecialHandling:
  case NotExample(reason: String)
  case NeedManual(reason: String)
  case TestErrors

val specialHandling: ListMap[String, SpecialHandling] = ListMap(
  "cookbook_Reusing-flags-for-several-transformationspatchings_3" -> SpecialHandling.NotExample("pseudocode"),
  "cookbook_Automatic-vs-semiautomatic_1" -> SpecialHandling.NotExample("pseudocode"),
  "cookbook_Automatic-vs-semiautomatic_2" -> SpecialHandling.NotExample("pseudocode"),
  "cookbook_Automatic-vs-semiautomatic_3" -> SpecialHandling.NotExample("pseudocode"),
  "cookbook_Automatic-vs-semiautomatic_4" -> SpecialHandling.NotExample("pseudocode"),
  "cookbook_Automatic-vs-semiautomatic_5" -> SpecialHandling.NotExample("pseudocode"),
  "cookbook_Performance-concerns_2" -> SpecialHandling.NotExample("example of code generated by macro"),
  "cookbook_Performance-concerns_3" -> SpecialHandling.NotExample("example of code generated by macro"),
  "cookbook_UnknownFieldSet_1" -> SpecialHandling.TestErrors,
  "cookbook_UnknownFieldSet_2" -> SpecialHandling.NeedManual("continuation from cookbook_UnknownFieldSet_1"),
  "cookbook_UnknownFieldSet_3" -> SpecialHandling.NeedManual("continuation from cookbook_UnknownFieldSet_1"),
  "cookbook_oneof-fields_1" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_oneof-fields_2" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_oneof-fields_3" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_oneof-fields_4" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_oneof-fields_5" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_sealed_value-oneof-fields_1" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_sealed_value-oneof-fields_2" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_sealed_value-oneof-fields_3" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_sealed_value_optional-oneof-fields_1" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_sealed_value_optional-oneof-fields_2" -> SpecialHandling.NeedManual("depends on code generated by codegen"),
  "cookbook_Libraries-with-smart-constructors_5" -> SpecialHandling.NotExample("pseudocode"),
  "index__2" -> SpecialHandling.NeedManual("landing page"),
  "index__3" -> SpecialHandling.NeedManual("landing page"),
  "index__4" -> SpecialHandling.NeedManual("landing page"),
  "index__5" -> SpecialHandling.NeedManual("landing page"),
  "index__6" -> SpecialHandling.NeedManual("landing page"),
  "quickstart_Quick-Start_1" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Quick-Start_2" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Java-collections-integration_1" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Java-collections-integration_2" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Cats-integration_1" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Cats-integration_2" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Protocol-Buffers-integration_1" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Protocol-Buffers-integration_2" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Protocol-Buffers-integration_1" -> SpecialHandling.NotExample("sbt example"),
  "quickstart_Protocol-Buffers-integration_2" -> SpecialHandling.NotExample("sbt example"),
  "supported-patching_Ignoring-fields-in-patches_1" -> SpecialHandling.TestErrors,
  "supported-patching_Ignoring-fields-in-patches_3" -> SpecialHandling.TestErrors,
  "supported-transformations_Reading-from-methods_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Reading-from-inherited-valuesmethods_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Reading-from-Bean-getters_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Writing-to-Bean-setters_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Ignoring-unmatched-Bean-setters_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Allowing-fallback-to-the-constructors-default-values_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Allowing-fallback-to-None-as-the-constructors-argument_3" -> SpecialHandling.TestErrors,
  "supported-transformations_Customizing-field-name-matching_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Frominto-an-AnyVal_2" -> SpecialHandling.TestErrors,
  "supported-transformations_Between-sealedenums_2" -> SpecialHandling.NeedManual("snippet fails!!! investigate later"), // FIXME
  "supported-transformations_Between-sealedenums_3" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "supported-transformations_Between-sealedenums_4" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "supported-transformations_Javas-enums_1" -> SpecialHandling.NeedManual("requires previous snipper with Java code"),
  "supported-transformations_Javas-enums_2" -> SpecialHandling.NeedManual("requires previous snipper with Java code"),
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_3" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_4" -> SpecialHandling.NeedManual("requires previous snipper with Java code"),
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_5" -> SpecialHandling.NeedManual("requires previous snipper with Java code"),
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_6" -> SpecialHandling.NeedManual("requires previous snipper with Java code"),
  "supported-transformations_Customizing-subtype-name-matching_3" -> SpecialHandling.TestErrors,
  "supported-transformations_Controlling-automatic-Option-unwrapping_1" -> SpecialHandling.TestErrors,
  "supported-transformations_Types-with-manually-provided-constructors_3" -> SpecialHandling.NeedManual("example split into multiple files"),
  "supported-transformations_Types-with-manually-provided-constructors_4" -> SpecialHandling.NeedManual("contunuation from the previous snippet"),
  "supported-transformations_Types-with-manually-provided-constructors_5" -> SpecialHandling.NeedManual("example split into multiple files"),
  "supported-transformations_Types-with-manually-provided-constructors_6" -> SpecialHandling.NeedManual("contunuation from the previous snippet"),
  "supported-transformations_Resolving-priority-of-implicit-Total-vs-Partial-Transformers_1" -> SpecialHandling.TestErrors,
  "supported-transformations_Defining-custom-name-matching-predicate_1" -> SpecialHandling.NeedManual("example split into multiple files"),
  "supported-transformations_Defining-custom-name-matching-predicate_2" -> SpecialHandling.NeedManual("contunuation from the previous snippet"),
  "troubleshooting_Replacing-Lifted-Transformers-TransformerF-with-PartialTransformers_1" -> SpecialHandling.NotExample("pseudocode"),
  "troubleshooting_Explicit-enabling-of-default-values_1" -> SpecialHandling.NotExample("pseudocode"),
  "troubleshooting_Ducktape_2" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "troubleshooting_Ducktape_4" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "troubleshooting_Ducktape_8" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "troubleshooting_Ducktape_10" -> SpecialHandling.NeedManual("snippet throws exception!!! investigate later"), // FIXME
  "troubleshooting_Recursive-types-fail-to-compile_1" -> SpecialHandling.NotExample("pseudocode"),
  "troubleshooting_Recursive-types-fail-to-compile_2" -> SpecialHandling.NotExample("pseudocode"),
  "troubleshooting_Recursive-calls-on-implicits_1" -> SpecialHandling.NotExample("pseudocode"),
  "troubleshooting_Recursive-calls-on-implicits_2" -> SpecialHandling.NotExample("pseudocode"),
  "troubleshooting_Recursive-calls-on-implicits_3" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-summons-Transformer-instance_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-summons-Transformer-instance_2" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-summons-Transformer-instance_3" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-summons-Transformer-instance_4" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-summons-Transformer-instance_5" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-summons-Transformer-instance_6" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_How-DSL-manages-customizations_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Carrying-around-the-runtime-configuration_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Carrying-around-the-runtime-configuration_2" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Carrying-around-the-runtime-configuration_3" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Carrying-around-the-runtime-configuration_4" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Carrying-around-the-type-level-configuration_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Total-vs-Partial_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Total-vs-Partial_2" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Total-vs-Partial_3" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Summoning-implicits_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Sealed-hierarchies_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Sealed-hierarchies_2" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_1" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_3" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_4" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_5" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_6" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_7" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_8" -> SpecialHandling.NotExample("pseudocode"),
  "under-the-hood_Scala-2-vs-Scala-3-in-derivation_9" -> SpecialHandling.NotExample("pseudocode"),
)

val ignored: Set[String] = specialHandling.keySet

// models

case class Snippet(name: String, hint: String, content: String) {

  lazy val snippetFile: File = File(s"${tmpDir.getPath()}/$name/snippet.sc")
  lazy val snippetDir: String = snippetFile.getParent()

  def isIgnored: Boolean = ignored(name)

  def save(): Unit = {
    snippetFile.getParentFile().mkdirs()
    Files.writeString(snippetFile.toPath(), content)
  }
}
object Snippet {

  def extractAll(markdown: Markdown): List[Snippet] = {
    val name = markdown.name

    case class Example(section: String, ordinal: Int = 0) {

      def next: Example = copy(ordinal = ordinal + 1)

      def toName: String = s"${name}_${section}_$ordinal".replaceAll(" +", "-").replaceAll("[^A-Za-z0-9_-]+", "")
    }

    enum Mode:
      case Reading(lineNo: Int, indent: Int, contentReverse: List[String])
      case Awaiting

    import Mode.*

    val start = "```scala"
    val end = "```"
    val sectionName = "#+(.+)".r

    def adjustLine(line: String, indent: Int): String = {
      val stripIndent = if line.length() > indent then line.substring(indent) else line
      patterns.foldLeft(stripIndent) { case (s, (k, v)) =>
        s.replace(k, v)
      }
    }

    def mkSnippet(example: Example, lineNo: Int, contentReverse: List[String]): Snippet = {
      val content0 = contentReverse.reverse.mkString("\n")
      val content =
        if content0.startsWith("//> using scala") then content0
        else "//> using scala 2.13.13\n" + content0
      Snippet(example.toName, s"$name:$lineNo", content)
    }

    def loop(content: List[(String, Int)], example: Example, mode: Mode, reverseResult: List[Snippet]): List[Snippet] =
      content match {
        case (line, lineNo) :: lines =>
          mode match {
            case Reading(lineNo, indent, contentReverse) =>
              if line.trim() == end then
                loop(lines, example, Awaiting, mkSnippet(example, lineNo, contentReverse) :: reverseResult)
              else
                loop(lines, example, Reading(lineNo, indent, adjustLine(line, indent) :: contentReverse), reverseResult)
            case Awaiting =>
              line.trim() match {
                case `start` => loop(lines, example.next, Reading(lineNo + 1, line.indexOf(start), Nil), reverseResult)
                case sectionName(section) => loop(lines, Example(section.trim()), Awaiting, reverseResult)
                case _                    => loop(lines, example, Awaiting, reverseResult)
              }
          }
        case Nil => reverseResult.reverse
      }

    loop(markdown.content.zipWithIndex, Example(""), Awaiting, Nil)
  }
}

case class Markdown(name: String, content: List[String]) {

  def extractAll: List[Snippet] = Snippet.extractAll(this)
}
object Markdown {

  def readAllInDir(dir: File): List[Markdown] =
    for {
      files <- Option(dir.listFiles()).toList
      markdownFile <- files.sortBy(_.getName()) if markdownFile.getAbsolutePath().endsWith(".md")
    } yield Using(io.Source.fromFile(markdownFile)) { src =>
      val name = markdownFile.getName()
      Markdown(name.substring(0, name.length() - ".md".length()), src.getLines().toList)
    }.get
}

// program

/** Usage:
  *
  * On CI:
  * {{{
  * # run all tests, use artifacts published locally from current tag
  * scala-cli run test-examples.scala -- "../docs/docs" "${git describe --tags}" "" -1 -1
  * }}}
  *
  * During development:
  * {{{
  * # fix: version to use, tmp directory, drop and take from snippets list (the ordering is deterministic)
  * scala-cli run test-examples.scala -- "../docs/docs" "1.0.0-RC1" /var/folders/m_/sm90t09d5591cgz5h242bkm80000gn/T/docs-snippets13141962741435068727 0 44
  * }}}
  */
@main def testExamples(
    path: String,
    providedGitTag: String,
    providedTmpDir: String,
    providedSnippetsDrop: Int,
    providedSnippetsTake: Int
): Unit = {
  gitTag = providedGitTag
  tmpDir =
    if providedTmpDir.isEmpty() then Files.createTempDirectory(s"docs-snippets").toFile() else File(providedTmpDir)
  val snippetsDrop = Option(providedSnippetsDrop).filter(_ >= 0).getOrElse(0)
  val snippetsTake = Option(providedSnippetsTake).filter(_ > 0).getOrElse(Int.MaxValue)

  extension (s: StringContext) def hl(args: Any*): String = s"$MAGENTA${s.s(args*)}$RESET"

  val docsDir = File(path)
  println(hl"Started reading from ${docsDir.getAbsolutePath()}")
  val markdowns = Markdown.readAllInDir(docsDir)
  println(hl"Read files: ${markdowns.map(_.name)}")
  val snippets = markdowns.flatMap(_.extractAll).drop(snippetsDrop).take(snippetsTake)
  println(hl"Found snippets" + ":\n" + snippets.map(s => hl"${s.hint} (${s.name})").mkString("\n") + "\n")
  val (ignoredSnippets, testedSnippets) = snippets.partition(_.isIgnored)
  println(hl"Ignoring snippets" + ":\n" + ignoredSnippets.map(s => hl"${s.hint} (${s.name})").mkString("\n") + "\n")
  val ignoredNotFound = ignored.filterNot(i => snippets.exists(_.name == i)).toList.sorted
  if ignoredNotFound.nonEmpty && snippetsDrop == 0 then {
    println(
      hl"Some ignored snippets have been moved, their indices changed and cannot be matched" + ":\n" + ignoredNotFound
        .mkString("\n")
    )
    sys.exit(1)
  }
  val saved = testedSnippets.foreach(_.save())
  val failed = testedSnippets.flatMap { snippet =>
    import snippet.{hint, name, snippetDir}
    println(hl"Testing: $hint ($name, saved in $snippetDir)" + ":")
    try {
      s"scala-cli run '$snippetDir'".!!
      List.empty[String]
    } catch {
      case _: Throwable => List(s"$hint ($name)")
    }
  }
  if failed.nonEmpty then {
    println(hl"Failed snippets (${failed.length}/${testedSnippets.length})" + s":\n${failed.mkString("\n")}")
    println(hl"Fix them or add to ignored list (name in parenthesis is less subject to change)")
    sys.exit(1)
  } else {
    println(hl"All snippets (${testedSnippets.length}) run succesfully!")
  }
}
