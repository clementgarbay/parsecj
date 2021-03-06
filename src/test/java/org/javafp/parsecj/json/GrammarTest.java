package org.javafp.parsecj.json;

import org.javafp.parsecj.Reply;
import org.junit.*;

public class GrammarTest {
    @Test
    public void test1() throws Exception {
        final Reply<Character, Node> reply = Grammar.parse(
            "{\"array\":[1,2,3],\"boolean\":true,\"null\":null,\"number\":123,\"object\":{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"},\"string\":\"Hello\nWorld\"}"
        );
        final Node node = reply.getResult();
        final String s = node.toString();
        Assert.assertTrue(!s.isEmpty());
    }

    @Test
    public void test2() throws Exception {
        final Reply<Character, Node> reply = Grammar.parse(
            " { \"array\" : [ 1 , 2 , 3 ] , \"boolean\" : true , \"null\" : null , \"number\" : 123 , \"object\" : { \"a\" : \"b\" , \"c\" : \"d\" , \"e\" : \"f\" } , \"string\" : \"Hello World\" } "
        );
        final Node node = reply.getResult();
        final String s = node.toString();
        Assert.assertTrue(!s.isEmpty());
    }

    @Test
    public void test3() throws Exception {
        final Reply<Character, Node> reply = Grammar.parse(
            "{\n" +
                "  \"array\": [\n" +
                "    1,\n" +
                "    true,\n" +
                "    [1,2,3]\n" +
                "  ],\n" +
                "  \"boolean\": true,\n" +
                "  \"null\": null,\n" +
                "  \"number\": 123,\n" +
                "  \"object\": {\n" +
                "    \"a\": \"b\",\n" +
                "    \"c\": false,\n" +
                "    \"e\": [6,7,8]\n" +
                "  },\n" +
                "  \"string\": \"Hello World\u1234\"\n" +
                "}"
        );
        final Node node = reply.getResult();
        final String s = node.toString();
        Assert.assertTrue(!s.isEmpty());
    }
}
