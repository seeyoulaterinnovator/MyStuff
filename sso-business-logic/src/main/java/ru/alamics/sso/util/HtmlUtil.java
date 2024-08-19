package ru.alamics.sso.util;

import com.helger.css.ECSSVersion;
import com.helger.css.ICSSWriteable;
import com.helger.css.decl.*;
import com.helger.css.reader.CSSReader;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HtmlUtil {
    public static String applyEmailCssToHtml(String html) {
        Document document = Jsoup.parse(html, StandardCharsets.UTF_8.name());
        for (Element element : document.select("[style]")) {
            element.attr("old-style", element.attr("style"));
            element.removeAttr("style");
        }
        List<StyleRule> styleRules = new ArrayList<>();
        for (Element style : document.head().select("style")) {
            styleRules.addAll(getStyleRules(style.html()));
        }
        styleRules = styleRules.stream()
                .sorted(Comparator.comparing(StyleRule::getSpecificity))
                .collect(Collectors.toList());
        for(StyleRule styleRule : styleRules) {
            try {
                for (Element element : document.select(styleRule.getSelector())) {
                    String css = element.attr("style") + ";" + styleRule.getStyle();
                    element.attr("style", css);
                }
            } catch (Selector.SelectorParseException e) {
                // skip
            }
        }
        for (Element element : document.select("[old-style]")) {
            element.attr(
                    "style",
                    element.attr("style") + "; " + element.attr("old-style")
            );
            element.removeAttr("old-style");
        }
        return document.toString();
    }

    static List<StyleRule> getStyleRules(String css) {
        List<StyleRule> styleRules = new ArrayList<>();
        CascadingStyleSheet styleSheet = CSSReader.readFromString(css, ECSSVersion.CSS30);
        if(styleSheet != null) {
        for (ICSSTopLevelRule styleRule : styleSheet.getAllRules()) {
            if(styleRule instanceof CSSStyleRule) {
                CSSStyleRule cssStyleRule = (CSSStyleRule) styleRule;
                for(CSSSelector selector : cssStyleRule.getAllSelectors()) {
                    styleRules.add(
                            StyleRule.builder()
                                    .selector(selector.getAllMembers()
                                            .stream()
                                            .map(ICSSWriteable::getAsCSSString)
                                            .collect(Collectors.joining("")))
                                    .specificity(getSpecificity(selector))
                                    .style(cssStyleRule.getAllDeclarations()
                                            .stream()
                                            .map(ICSSWriteable::getAsCSSString)
                                            .collect(Collectors.joining(";"))
                                            .replaceAll("^;", "")
                                            .replaceAll(";\\s*;", "")
                                    )
                                    .build()
                    );
                }
            }
        }
        }
        return styleRules;
    }

    static Specificity getSpecificity(CSSSelector selector) {
        Specificity specificity = new Specificity();
        selector.getAllMembers().forEach(member -> computeSpecificity(member, specificity));
        return specificity;
    }

    static void computeSpecificity(ICSSSelectorMember member, Specificity specificity) {
        if(member instanceof CSSSelectorSimpleMember) {
            CSSSelectorSimpleMember simpleMember = (CSSSelectorSimpleMember) member;
            if(simpleMember.isHash()) {
                specificity.value1++;
            } else if(simpleMember.isElementName()) {
                specificity.value3++;
            } else {
                specificity.value2++;
            }
        } else if(!(member instanceof ECSSSelectorCombinator)) {
            specificity.value2++;
        }
    }

    @Data
    @Builder
    static class StyleRule {
        String selector;

        Specificity specificity;

        String style;
    }

    @Data
    static class Specificity implements Comparable<Specificity> {
        int value1;

        int value2;

        int value3;

        @Override
        public int compareTo(Specificity other) {
            int result = Integer.compare(value1, other.value1);
            if (result != 0) return result;

            result = Integer.compare(value2, other.value2);
            if (result != 0) return result;

            result = Integer.compare(value3, other.value3);

            return result;
        }
    }
}
