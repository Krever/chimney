//> using scala 3.3.3

import java.io.File
import java.nio.file.{Files, Paths}
import scala.util.Using
import scala.sys.process.*
import scala.Console.{MAGENTA, RESET}

// use for local development

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

val ignored: Set[String] = Set(
  "cookbook_Reusing-flags-for-several-transformationspatchings_3", // abstract
  "cookbook_Automatic-vs-semiautomatic_1", // abstract
  "cookbook_Automatic-vs-semiautomatic_2", // abstract
  "cookbook_Automatic-vs-semiautomatic_3", // abstract
  "cookbook_Automatic-vs-semiautomatic_4", // abstract
  "cookbook_Automatic-vs-semiautomatic_5", // abstract
  "cookbook_Performance-concerns_2", // example of code generated by macro
  "cookbook_Performance-concerns_3", // example of code generated by macro
  "cookbook_UnknownFieldSet_1", // we're expecting an error here (verify that it matches!)
  "cookbook_UnknownFieldSet_2", // continuation from cookbook_UnknownFieldSet_1
  "cookbook_UnknownFieldSet_3", // continuation from cookbook_UnknownFieldSet_1
  "cookbook_oneof-fields_1", // depends on code generated by codegen
  "cookbook_oneof-fields_2", // depends oncode generated by codegen
  "cookbook_oneof-fields_3", // depends oncode generated by codegen
  "cookbook_oneof-fields_4", // depends oncode generated by codegen
  "cookbook_oneof-fields_5", // depends oncode generated by codegen
  "cookbook_sealed_value-oneof-fields_1", // depends on code generated by codegen
  "cookbook_sealed_value-oneof-fields_2", // depends on code generated by codegen
  "cookbook_sealed_value-oneof-fields_3", // depends on code generated by codegen
  "cookbook_sealed_value_optional-oneof-fields_1", // depends on code generated by codegen
  "cookbook_sealed_value_optional-oneof-fields_2", // depends on code generated by codegen
  "cookbook_Libraries-with-smart-constructors_5", // abstract
  "index__2", // landing page (demo should be verified manually)!
  "index__3", // landing page (demo should be verified manually)!
  "index__4", // landing page (demo should be verified manually)!
  "index__5", // landing page (demo should be verified manually)!
  "index__6", // landing page (demo should be verified manually)!
  "quickstart_Quick-Start_1", // sbt example
  "quickstart_Quick-Start_2", // sbt example
  "quickstart_Java-collections-integration_1", // sbt example
  "quickstart_Java-collections-integration_2", // sbt example
  "quickstart_Cats-integration_1", // sbt example
  "quickstart_Cats-integration_2", // sbt example
  "quickstart_Protocol-Buffers-integration_1", // sbt example
  "quickstart_Protocol-Buffers-integration_2", // sbt example
  "quickstart_Protocol-Buffers-integration_1", // sbt example
  "quickstart_Protocol-Buffers-integration_2", // sbt example
  "supported-patching_Ignoring-fields-in-patches_1", // we're expecting an error here (verify that it matches!)
  "supported-patching_Ignoring-fields-in-patches_3", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Reading-from-methods_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Reading-from-inherited-valuesmethods_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Reading-from-Bean-getters_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Writing-to-Bean-setters_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Ignoring-unmatched-Bean-setters_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Allowing-fallback-to-the-constructors-default-values_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Allowing-fallback-to-None-as-the-constructors-argument_3", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Customizing-field-name-matching_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Frominto-an-AnyVal_2", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Between-sealedenums_2", // snippet fails!!! investigate later
  "supported-transformations_Between-sealedenums_3", // snippet throws exception!!! investigate later
  "supported-transformations_Between-sealedenums_4", // snippet throws exception!!! investigate later
  "supported-transformations_Javas-enums_1", // requires previous snipper with Java code (verify manually!!!)
  "supported-transformations_Javas-enums_2", // requires previous snipper with Java code (verify manually!!!)
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_3", // snippet throws exception!!! investigate later
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_4", // requires previous snipper with Java code (verify manually!!!)
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_5", // requires previous snipper with Java code (verify manually!!!)
  "supported-transformations_Handling-a-specific-sealed-subtype-with-a-computed-value_6", // requires previous snipper with Java code (verify manually!!!)
  "supported-transformations_Customizing-subtype-name-matching_3", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Controlling-automatic-Option-unwrapping_1", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Types-with-manually-provided-constructors_3", // example split into multiple files (verify manually!!!)
  "supported-transformations_Types-with-manually-provided-constructors_4", // contunuation from the previous snippet (verify manually!!!)
  "supported-transformations_Types-with-manually-provided-constructors_5", // example split into multiple files (verify manually!!!)
  "supported-transformations_Types-with-manually-provided-constructors_6", // contunuation from the previous snippet (verify manually!!!)
  "supported-transformations_Resolving-priority-of-implicit-Total-vs-Partial-Transformers_1", // we're expecting an error here (verify that it matches!)
  "supported-transformations_Defining-custom-name-matching-predicate_1", // example split into multiple files (verify manually!!!)
  "supported-transformations_Defining-custom-name-matching-predicate_2", // contunuation from the previous snippet (verify manually!!!)
)

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