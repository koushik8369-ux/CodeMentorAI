package com.codementor.api.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JavaCodeValidationService {

    private static final Logger logger = LoggerFactory.getLogger(JavaCodeValidationService.class);

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([\\s\\S]*?)```");
    private static final Pattern HEADER_LANG_PATTERN = Pattern.compile("^[a-zA-Z0-9_+-]+$");
    private static final Pattern LINE_SEMICOLON_PATTERN = Pattern.compile(";\\s*$", Pattern.MULTILINE);
    private static final Pattern JAVA_ANNOTATION_PATTERN = Pattern.compile("(?m)^\\s*@[A-Z][A-Za-z0-9_]*");
    private static final Pattern JAVA_IMPORT_PKG_PATTERN = Pattern.compile("(?m)^\\s*(package|import)\\s+[a-zA-Z_][a-zA-Z0-9_.*]*\\s*;?");
    private static final Pattern JAVA_MODIFIER_DECL_PATTERN = Pattern.compile("(?m)^\\s*(?:public|private|protected|static|final|abstract|synchronized|default|class|interface|record|enum)\\s+[A-Za-z_<]");
    private static final Pattern JAVA_VAR_ASSIGN_PATTERN = Pattern.compile("(?m)^\\s*(?:final\\s+)?(?:int|long|double|float|boolean|char|byte|short|var|String|List|Map|Set)\\b[^\\n]*=");
    private static final Pattern JAVA_CONTROL_PAREN_PATTERN = Pattern.compile("(?m)^\\s*(?:if|for|while|switch|catch)\\s*\\(");
    private static final Pattern JAVA_RETURN_STMT_PATTERN = Pattern.compile("(?m)^\\s*return\\s+[^\\n.]+$");
    private static final Pattern JAVA_CALL_STMT_PATTERN = Pattern.compile("(?m)^\\s*[a-zA-Z_][a-zA-Z0-9_.]*\\s*\\([^\\n]*\\)\\s*$");
    private static final Pattern CONSTRUCTOR_NAME_PATTERN = Pattern.compile("(?m)^\\s*(?:@[A-Za-z0-9_.]*(?:\\([^)]*\\))?\\s*)*(?:(?:public|private|protected)\\s+)?([A-Z][A-Za-z0-9_]*)\\s*\\(");

    private final ParserConfiguration parserConfiguration;

    public JavaCodeValidationService() {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        configuration.setAttributeComments(false);
        this.parserConfiguration = configuration;
    }

    private JavaParser createParser() {
        return new JavaParser(parserConfiguration);
    }

    public static class ValidationResult {
        private final String status; // "VALID", "INVALID", "NOT_CHECKED"
        private final String message;
        private final boolean valid;

        private ValidationResult(String status, String message, boolean valid) {
            this.status = status;
            this.message = message;
            this.valid = valid;
        }

        public static ValidationResult valid(String message) {
            return new ValidationResult("VALID", message, true);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult("INVALID", message, false);
        }

        public static ValidationResult notChecked(String message) {
            return new ValidationResult("NOT_CHECKED", message, false);
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isValid() {
            return valid;
        }
    }

    public static class ExtractedBlock {
        private final int index;
        private final String language;
        private final String code;
        private final int startIndex;
        private final int endIndex;
        private final String rawMarkdown;

        public ExtractedBlock(int index, String language, String code, int startIndex, int endIndex, String rawMarkdown) {
            this.index = index;
            this.language = language;
            this.code = code;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.rawMarkdown = rawMarkdown;
        }

        public int getIndex() {
            return index;
        }

        public String getLanguage() {
            return language;
        }

        public String getCode() {
            return code;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public String getRawMarkdown() {
            return rawMarkdown;
        }
    }

    /**
     * Extracts all markdown code blocks (```...```) consistently with frontend rendering.
     */
    public List<ExtractedBlock> extractCodeBlocks(String markdown) {
        List<ExtractedBlock> blocks = new ArrayList<>();
        if (markdown == null || markdown.isBlank()) {
            return blocks;
        }

        Matcher matcher = CODE_BLOCK_PATTERN.matcher(markdown);
        int index = 0;
        while (matcher.find()) {
            String inner = matcher.group(1) != null ? matcher.group(1) : "";
            String lang = "";
            String code = inner;

            int nlIdx = inner.indexOf('\n');
            if (nlIdx != -1) {
                String headerLine = inner.substring(0, nlIdx).replace("\r", "").trim();
                String bodyContent = inner.substring(nlIdx + 1);
                if (HEADER_LANG_PATTERN.matcher(headerLine).matches()) {
                    lang = headerLine.toLowerCase();
                    code = bodyContent;
                } else if (headerLine.isEmpty()) {
                    lang = "";
                    code = bodyContent;
                }
            }

            blocks.add(new ExtractedBlock(index++, lang, code, matcher.start(), matcher.end(), matcher.group(0)));
        }
        return blocks;
    }

    /**
     * Determines if a code block should be evaluated as Java syntax.
     * Explicit non-Java languages, comment-only blocks, and ordinary prose return false.
     */
    public boolean isJavaBlock(String language, String conversationLanguage, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        if (language != null && !language.isBlank()) {
            String l = language.toLowerCase().trim();
            if ("java".equals(l) || "java21".equals(l) || "java17".equals(l)) {
                return !isOnlyComments(code) && !isOrdinaryProse(code);
            }
            // Any other explicit language tag (python, sql, js, ts, text, pseudocode, etc.) is NOT Java
            return false;
        }

        // Untagged code block: only evaluate if conversation context is Java AND content is genuinely Java code
        if (conversationLanguage != null && conversationLanguage.equalsIgnoreCase("java")) {
            return looksLikeJava(code);
        }

        return false;
    }

    private boolean looksLikeJava(String code) {
        if (code == null || code.isBlank() || isOnlyComments(code) || isOrdinaryProse(code)) {
            return false;
        }
        String body = stripComments(code);

        // Exclude obvious non-Java syntax constructs
        String lower = body.toLowerCase();
        if (lower.startsWith("select ") || lower.startsWith("create table") || lower.startsWith("insert into")
                || lower.startsWith("def ") || body.contains("console.log(") || body.contains("function ")
                || (body.startsWith("{") && body.contains("\":"))) {
            return false;
        }

        boolean hasJavaKeyword = JAVA_MODIFIER_DECL_PATTERN.matcher(body).find()
                || JAVA_IMPORT_PKG_PATTERN.matcher(body).find()
                || JAVA_ANNOTATION_PATTERN.matcher(body).find()
                || body.contains("System.out.")
                || JAVA_VAR_ASSIGN_PATTERN.matcher(body).find();

        boolean hasSyntaxDelimiter = body.contains("{") || body.contains("}") || LINE_SEMICOLON_PATTERN.matcher(body).find();
        return hasJavaKeyword && hasSyntaxDelimiter;
    }

    public boolean isOnlyComments(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return stripComments(code).isEmpty();
    }

    public boolean isOrdinaryProse(String code) {
        String body = stripComments(code);
        if (body.isEmpty()) {
            return false;
        }

        boolean hasBraces = body.contains("{") || body.contains("}");
        boolean hasArrow = body.contains("->");
        boolean hasLineSemicolon = LINE_SEMICOLON_PATTERN.matcher(body).find();
        boolean hasAnnotation = JAVA_ANNOTATION_PATTERN.matcher(body).find();
        boolean hasJavaImportOrPkg = JAVA_IMPORT_PKG_PATTERN.matcher(body).find();
        boolean hasSystemOut = body.contains("System.out.") || body.contains("System.err.");

        if (hasBraces || hasArrow || hasLineSemicolon || hasAnnotation || hasJavaImportOrPkg || hasSystemOut) {
            return false;
        }

        boolean hasJavaModifierDecl = JAVA_MODIFIER_DECL_PATTERN.matcher(body).find();
        boolean hasJavaVarAssign = JAVA_VAR_ASSIGN_PATTERN.matcher(body).find();
        boolean hasControlWithParen = JAVA_CONTROL_PAREN_PATTERN.matcher(body).find();
        boolean hasLowercaseReturnStmt = JAVA_RETURN_STMT_PATTERN.matcher(body).find();
        boolean hasMethodCallParen = JAVA_CALL_STMT_PATTERN.matcher(body).find();

        return !(hasJavaModifierDecl || hasJavaVarAssign || hasControlWithParen || hasLowercaseReturnStmt || hasMethodCallParen);
    }

    private String stripComments(String code) {
        if (code == null) {
            return "";
        }
        String noLineComments = code.replaceAll("//.*", "");
        String noBlockComments = noLineComments.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        return noBlockComments.trim();
    }

    /**
     * Validates syntax of Java code using JavaParser with Java 21 grammar.
     * Evaluates compilation units, standalone methods, constructors, interface methods, and statement blocks.
     * Comment-only blocks and ordinary explanatory prose return NOT_CHECKED without invoking JavaParser.
     */
    public ValidationResult validateJavaSnippet(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.notChecked("Empty code block");
        }

        String cleaned = code.trim();
        if (isOnlyComments(cleaned)) {
            return ValidationResult.notChecked("Code block contains only comments");
        }

        if (isOrdinaryProse(cleaned)) {
            return ValidationResult.notChecked("Plain explanatory text (not Java code)");
        }

        JavaParser parser = createParser();

        try {
            // Attempt 1: Direct compilation unit (classes, interfaces, records, enums, full files)
            ParseResult<CompilationUnit> cuResult = parser.parse(cleaned);
            if (cuResult.isSuccessful() && cuResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java syntax parsed successfully (Java 21 grammar)");
            }

            // Attempt 2: Method / Member declarations wrapped inside a temporary class
            String wrappedInClass = buildWrappedClass(cleaned);
            ParseResult<CompilationUnit> classResult = parser.parse(wrappedInClass);
            if (classResult.isSuccessful() && classResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java method/declaration syntax parsed successfully (Java 21 grammar)");
            }

            // Attempt 3: Interface default / abstract method declarations wrapped inside a temporary interface
            String wrappedInInterface = buildWrappedInterface(cleaned);
            ParseResult<CompilationUnit> interfaceResult = parser.parse(wrappedInInterface);
            if (interfaceResult.isSuccessful() && interfaceResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java interface method syntax parsed successfully (Java 21 grammar)");
            }

            // Attempt 4: Standalone constructor wrapped in a class with matching name
            String constructorClassName = detectConstructorName(cleaned);
            if (constructorClassName != null) {
                String wrappedConstructor = buildWrappedNamedClass(cleaned, constructorClassName);
                ParseResult<CompilationUnit> ctorResult = parser.parse(wrappedConstructor);
                if (ctorResult.isSuccessful() && ctorResult.getProblems().isEmpty()) {
                    return ValidationResult.valid("Java constructor syntax parsed successfully (Java 21 grammar)");
                }
            }

            // Attempt 5: Statements/expressions inside a method
            String wrappedInMethod = buildWrappedMethod(cleaned);
            ParseResult<CompilationUnit> methodResult = parser.parse(wrappedInMethod);
            if (methodResult.isSuccessful() && methodResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java statement/block syntax parsed successfully (Java 21 grammar)");
            }

            // Prefer the most relevant parse error diagnostic
            boolean declaresTopLevelType = Pattern.compile("\\b(class|interface|record|enum)\\s+[A-Za-z_]").matcher(cleaned).find();
            if (!declaresTopLevelType && !classResult.getProblems().isEmpty()) {
                return ValidationResult.invalid(sanitizeProblemMessage(classResult.getProblems().get(0).getMessage()));
            }

            if (!cuResult.getProblems().isEmpty()) {
                return ValidationResult.invalid(sanitizeProblemMessage(cuResult.getProblems().get(0).getMessage()));
            }

            if (!methodResult.getProblems().isEmpty()) {
                return ValidationResult.invalid(sanitizeProblemMessage(methodResult.getProblems().get(0).getMessage()));
            }

            return ValidationResult.invalid("Syntax parsing error detected in Java code snippet");

        } catch (ParseProblemException ex) {
            String msg = ex.getProblems().isEmpty() ? ex.getMessage() : ex.getProblems().get(0).getMessage();
            return ValidationResult.invalid(sanitizeProblemMessage(msg));
        } catch (Exception ex) {
            logger.debug("JavaParser unexpected failure: {}", ex.getMessage());
            return ValidationResult.invalid("Syntax validation encountered an unexpected parsing error");
        }
    }

    private String detectConstructorName(String code) {
        String body = stripComments(code);
        Matcher m = CONSTRUCTOR_NAME_PATTERN.matcher(body);
        if (m.find()) {
            String candidate = m.group(1);
            if (candidate != null && !candidate.isBlank()
                    && !"String".equals(candidate) && !"Integer".equals(candidate)
                    && !"List".equals(candidate) && !"Map".equals(candidate) && !"Set".equals(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String buildWrappedClass(String code) {
        return buildWrappedNamedClass(code, "__CodeMentorSnippetWrapper__");
    }

    private String buildWrappedNamedClass(String code, String className) {
        List<String> imports = new ArrayList<>();
        StringBuilder body = new StringBuilder();

        for (String line : code.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
                imports.add(trimmed);
            } else {
                body.append(line).append("\n");
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String imp : imports) {
            sb.append(imp).append("\n");
        }
        sb.append("class ").append(className).append(" {\n");
        sb.append(body);
        sb.append("\n}\n");
        return sb.toString();
    }

    private String buildWrappedInterface(String code) {
        List<String> imports = new ArrayList<>();
        StringBuilder body = new StringBuilder();

        for (String line : code.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
                imports.add(trimmed);
            } else {
                body.append(line).append("\n");
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String imp : imports) {
            sb.append(imp).append("\n");
        }
        sb.append("interface __CodeMentorSnippetInterfaceWrapper__ {\n");
        sb.append(body);
        sb.append("\n}\n");
        return sb.toString();
    }

    private String buildWrappedMethod(String code) {
        List<String> imports = new ArrayList<>();
        StringBuilder body = new StringBuilder();

        for (String line : code.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
                imports.add(trimmed);
            } else {
                body.append(line).append("\n");
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String imp : imports) {
            sb.append(imp).append("\n");
        }
        sb.append("class __CodeMentorSnippetWrapper__ {\n");
        sb.append("  void __snippetMethod__() throws Throwable {\n");
        sb.append(body);
        sb.append("\n  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String sanitizeProblemMessage(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Syntax error in Java code snippet";
        }
        // Remove file path references, internal class names, stack traces
        String cleaned = raw.replaceAll("(?i)(/|\\\\|[a-z]:\\\\)[^\\s:]+", "");
        cleaned = cleaned.replaceAll("(?i)com\\.github\\.javaparser\\.[^\\s:]+", "");
        cleaned = cleaned.replaceAll("\\r?\\n", " ").trim();
        if (cleaned.length() > 160) {
            cleaned = cleaned.substring(0, 160) + "...";
        }
        return cleaned.isBlank() ? "Syntax error in Java code snippet" : cleaned;
    }
}
