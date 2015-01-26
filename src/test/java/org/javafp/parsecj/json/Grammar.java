package org.javafp.parsecj.json;

import org.javafp.data.*;
import org.javafp.parsecj.*;

import java.util.LinkedHashMap;

import static org.javafp.parsecj.Combinators.between;
import static org.javafp.parsecj.Combinators.choice;
import static org.javafp.parsecj.Combinators.many;
import static org.javafp.parsecj.Combinators.retn;
import static org.javafp.parsecj.Combinators.satisfy;
import static org.javafp.parsecj.Combinators.sepBy;
import static org.javafp.parsecj.Text.chr;
import static org.javafp.parsecj.Text.dble;
import static org.javafp.parsecj.Text.string;
import static org.javafp.parsecj.Text.wspaces;

/**
 * A grammar for JSON.
 * Adapted from the Haskell Parsec-based JSON parser:
 * https://hackage.haskell.org/package/json
 */
public class Grammar {
    private static <T> Parser<Character, T> tok(Parser<Character, T> p) {
        return p.bind(x -> wspaces.then(retn(x)));
    }

    private static Parser.Ref<Character, Node> jvalue = Parser.Ref.of();

    private static Parser<Character, Node> jnull = tok(string("null")).then(retn(Node.nul())).label("null");

    private static Parser<Character, Boolean> jtrue = tok(string("true").then(retn(Boolean.TRUE)));
    private static Parser<Character, Boolean> jfalse = tok(string("false").then(retn(Boolean.FALSE)));

    private static Parser<Character, Node> jbool = tok(jtrue.or(jfalse).bind(b -> retn(Node.bool(b)))).label("boolean");

    private static Parser<Character, Node> jnumber = tok(dble.bind(d -> retn(Node.number(d)))).label("number");

    private static Parser<Character, Byte> hexDigit =
        satisfy((Character c) -> Character.digit(c, 16) != -1)
            .bind(c -> retn((byte) Character.digit(c, 16))).label("hex digit");

    private static Parser<Character, Character> uni =
        hexDigit.bind(
            d0 -> hexDigit.bind(
                d1 -> hexDigit.bind(
                    d2 -> hexDigit.bind(
                        d3 -> retn((d0<<0x3) & (d1<<0x2) & (d2<<0x1) & d0)))))
            .bind(i -> retn((char) i.intValue()));

    private static Parser<Character, Character> esc =
        choice(
            chr('"'),
            chr('\\'),
            chr('/'),
            chr('b').then(retn('\b')),
            chr('f').then(retn('\f')),
            chr('n').then(retn('\n')),
            chr('r').then(retn('\r')),
            chr('t').then(retn('\t')),
            chr('u').then(uni)
            ).label("escape character");

    private static Parser<Character, Character> stringChar =
        (chr('\\').then(esc)
        ).or(satisfy(c -> c != '"' && c != '\\')
        );

    private static Parser<Character, String> jstring =
        tok(between(
            chr('"'),
            chr('"'),
            many(stringChar).bind(l -> retn(IList.listToString(l)))
        ));

    private static Parser<Character, Node> jtext =
        jstring.bind(s -> retn(Node.text(s))).label("text");

    private static Parser<Character, Node> jarray =
        between(
            tok(chr('[')),
            tok(chr(']')),
            sepBy(
                jvalue,
                tok(chr(','))
            )
        ).bind(l -> retn(Node.array(IList.toList(l))))
            .label("array");

    static LinkedHashMap<String, Node> toMap(IList<Tuple2<String, Node>> fields) {
        final LinkedHashMap<String, Node> map = new LinkedHashMap<String, Node>();
        fields.forEach(field -> map.put(field.first, field.second));
        return map;
    }

    private static Parser<Character, Tuple2<String, Node>> jfield =
        jstring.bind(
            name -> tok(chr(':'))
                .then(jvalue)
                .bind(value -> retn(Tuple2.of(name, value)))
        );

    private static Parser<Character, Node> jobject =
        between(
            tok(chr('{')),
            tok(chr('}')),
            sepBy(
                jfield,
                tok(chr(','))
            ).bind(lf -> retn(Node.object(toMap(lf))))
        ).label("object");

    static {
        jvalue.set(
            choice(
                jnull,
                jbool,
                jnumber,
                jtext,
                jarray,
                jobject
            ).label("JSON value")
        );
    }

    public static final Parser<Character, Node> parser = wspaces.then(jvalue);

    public static Reply<Character, Node> parse(String str) {
        return parser.parse(State.state(str));
    }
}
