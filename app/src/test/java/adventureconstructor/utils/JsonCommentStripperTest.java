package adventureconstructor.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;

class JsonCommentStripperTest {
    @Test
    void removesLineAndBlockCommentsBeforeParsing() throws Exception {
        String json =
                "{\n" + "  // line comment\n" + "  \"first\": 1, /* block comment */\n" + "  \"second\": 2\n" + "}";

        JSONObject result = (JSONObject) new JSONParser().parse(JsonCommentStripper.strip(json));

        assertEquals(1L, result.get("first"));
        assertEquals(2L, result.get("second"));
    }

    @Test
    void preservesCommentMarkersInsideJsonStrings() throws Exception {
        String json = "{\"url\":\"https://example.test/a/*b*/\"," + "\"text\":\"quote: \\\"; // not a comment\"}";

        JSONObject result = (JSONObject) new JSONParser().parse(JsonCommentStripper.strip(json));

        assertEquals("https://example.test/a/*b*/", result.get("url"));
        assertEquals("quote: \"; // not a comment", result.get("text"));
    }

    @Test
    void rejectsUnterminatedBlockComment() {
        assertThrows(IllegalArgumentException.class, () -> JsonCommentStripper.strip("{ /* unfinished"));
    }
}
