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

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([a-zA-Z0-9_+-]*)[ \\t]*\\r?\\n([\\s\\S]*?)```");

    private final JavaParser javaParser;

    public JavaCodeValidationService() {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        configuration.setAttributeComments(false);
        this.javaParser = new JavaParser(configuration);
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
     * Extracts all markdown code blocks (```lang ... ```) from the text.
     */
    public List<ExtractedBlock> extractCodeBlocks(String markdown) {
        List<ExtractedBlock> blocks = new ArrayList<>();
        if (markdown == null || markdown.isBlank()) {
            return blocks;
        }

        Matcher matcher = CODE_BLOCK_PATTERN.matcher(markdown);
        int index = 0;
        while (matcher.find()) {
            String lang = matcher.group(1) != null ? matcher.group(1).trim().toLowerCase() : "";
            String code = matcher.group(2) != null ? matcher.group(2) : "";
            blocks.add(new ExtractedBlock(index++, lang, code, matcher.start(), matcher.end(), matcher.group(0)));
        }
        return blocks;
    }

    /**
     * Determines if a code block is identified as Java code.
     */
    public boolean isJavaBlock(String language, String conversationLanguage, String code) {
        if (language != null && !language.isBlank()) {
            String l = language.toLowerCase().trim();
            if ("java".equals(l)) {
                return true;
            }
            // Explicit non-Java languages must not be validated with JavaParser
            if ("python".equals(l) || "py".equals(l) || "javascript".equals(l) || "js".equals(l)
                    || "typescript".equals(l) || "ts".equals(l) || "sql".equals(l) || "html".equals(l)
                    || "css".equals(l) || "bash".equals(l) || "sh".equals(l) || "json".equals(l)
                    || "c".equals(l) || "cpp".equals(l) || "csharp".equals(l) || "cs".equals(l)
                    || "go".equals(l) || "rust".equals(l) || "ruby".equals(l) || "php".equals(l)) {
                return false;
            }
        }

        // If no explicit language tag is provided, check conversation context
        if (conversationLanguage != null && conversationLanguage.equalsIgnoreCase("java")) {
            // Heuristic check to verify it looks like Java code (not plain text or SQL)
            return looksLikeJava(code);
        }

        return false;
    }

    private boolean looksLikeJava(String code) {
        if (code == null || code.isBlank()) return false;
        String trimmed = code.trim();
        return trimmed.contains("public ") || trimmed.contains("private ") || trimmed.contains("protected ")
                || trimmed.contains("class ") || trimmed.contains("interface ") || trimmed.contains("record ")
                || trimmed.contains("void ") || trimmed.contains("int ") || trimmed.contains("String ")
                || trimmed.contains("System.out") || trimmed.contains("import java.");
    }

    /**
     * Validates syntax of Java code using JavaParser with Java 21 grammar.
     * Evaluates compilation units, class bodies, method declarations, and statement blocks.
     */
    public ValidationResult validateJavaSnippet(String code) {
        if (code == null || code.isBlank()) {
            return ValidationResult.notChecked("Empty code block");
        }

        String cleaned = code.trim();
        if (isOnlyComments(cleaned)) {
            return ValidationResult.notChecked("Code block contains only comments");
        }

        // Attempt 1: Direct compilation unit (classes, interfaces, records, enums, full files)
        try {
            ParseResult<CompilationUnit> cuResult = javaParser.parse(cleaned);
            if (cuResult.isSuccessful() && cuResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java syntax parsed successfully (Java 21 grammar)");
            }

            // Attempt 2: Method / Member declarations wrapped inside a class
            // Handles code like: public int twoSum(...) { ... }
            String wrappedInClass = buildWrappedClass(cleaned);
            ParseResult<CompilationUnit> classResult = javaParser.parse(wrappedInClass);
            if (classResult.isSuccessful() && classResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java method/declaration syntax parsed successfully (Java 21 grammar)");
            }

            // Attempt 3: Statements inside a method
            // Handles code like: List<String> list = new ArrayList<>(); System.out.println(list);
            String wrappedInMethod = buildWrappedMethod(cleaned);
            ParseResult<CompilationUnit> methodResult = javaParser.parse(wrappedInMethod);
            if (methodResult.isSuccessful() && methodResult.getProblems().isEmpty()) {
                return ValidationResult.valid("Java statement/block syntax parsed successfully (Java 21 grammar)");
            }

            // All parsing attempts failed; collect problems from original compilation unit parse
            List<Problem> problems = cuResult.getProblems();
            if (!problems.isEmpty()) {
                Problem first = problems.get(0);
                String msg = sanitizeProblemMessage(first.getMessage());
                return ValidationResult.invalid(msg);
            }

            // Fallback to method-level problem message
            if (!methodResult.getProblems().isEmpty()) {
                Problem first = methodResult.getProblems().get(0);
                String msg = sanitizeProblemMessage(first.getMessage());
                return ValidationResult.invalid(msg);
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

    private boolean isOnlyComments(String code) {
        String noLineComments = code.replaceAll("//.*", "");
        String noBlockComments = noLineComments.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        return noBlockComments.trim().isEmpty();
    }

    private String buildWrappedClass(String code) {
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
