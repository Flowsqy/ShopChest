package de.epiceric.shopchest.config.hologram.line;

import de.epiceric.shopchest.config.hologram.provider.ConstantProvider;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatReplacer<P> {

    private List<Token<P>> tokens;

    public FormatReplacer(String original) {
        Objects.requireNonNull(original);
        this.tokens = new ArrayList<>(Collections.singletonList(new Token.StringToken<>(original)));
    }

    public FormatReplacer<P> replace(String regex, String replacement) {
        final List<Token<P>> outputTokens = new ArrayList<>();
        for (final Token<P> token : tokens) {
            if (token.isString()) {
                outputTokens.add(new Token.StringToken<>(token.getString().replace(regex, replacement)));
            } else {
                outputTokens.add(token);
            }
        }
        this.tokens = outputTokens;
        return this;
    }

    public FormatReplacer<P> replace(String regex, Function<Map<P, Object>, String> dynamic) {
        final List<Token<P>> outputTokens = new ArrayList<>();
        for (final Token<P> token : tokens) {
            if (token.isString()) {
                outputTokens.addAll(getTokenReplacement(token.getString(), regex, dynamic));
            } else {
                outputTokens.add(token);
            }
        }
        this.tokens = outputTokens;
        return this;
    }

    public FormattedLine<P> create() {
        final List<Function<Map<P, Object>, String>> components = new ArrayList<>();
        tokens.stream().map(Token::getDynamics).map(Arrays::asList).forEach(components::addAll);
        return new FormattedLine<>(components);
    }

    private List<Token<P>> getTokenReplacement(String input, String regex, Function<Map<P, Object>, String> dynamic) {
        final List<Token<P>> tokens = new ArrayList<>();
        final Matcher matcher = Pattern.compile(regex, Pattern.LITERAL).matcher(input);
        if (matcher.find()) {
            int cursor = 0;
            do {
                final String pre = input.substring(cursor, matcher.start());
                if (!pre.isEmpty()) {
                    tokens.add(new Token.StringToken<>(pre));
                }
                tokens.add(new Token.DynamicsToken<>(dynamic));
                cursor = matcher.end();
            } while (matcher.find());
            final String end = input.substring(cursor);
            if (!end.isEmpty()) {
                tokens.add(new Token.StringToken<>(end));
            }
            return tokens;
        }
        tokens.add(new Token.StringToken<>(input));
        return tokens;
    }

    public interface Token<P> {

        boolean isString();

        String getString();

        Function<Map<P, Object>, String> getDynamics();

        final class DynamicsToken<P> implements Token<P> {

            private final Function<Map<P, Object>, String> dynamics;

            public DynamicsToken(Function<Map<P, Object>, String> dynamics) {
                this.dynamics = dynamics;
            }

            @Override
            public boolean isString() {
                return false;
            }

            @Override
            public String getString() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Function<Map<P, Object>, String> getDynamics() {
                return dynamics;
            }
        }

        final class StringToken<P> implements Token<P> {

            private final String string;

            public StringToken(String string) {
                this.string = string;
            }

            @Override
            public boolean isString() {
                return true;
            }

            @Override
            public String getString() {
                return string;
            }

            @Override
            public Function<Map<P, Object>, String> getDynamics() {
                return new ConstantProvider<>(string);
            }

        }

    }
}
