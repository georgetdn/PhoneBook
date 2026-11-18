import assert from "node:assert/strict";
import { once } from "node:events";
import { test } from "node:test";
import { createInterpretationServer } from "./interpret.mjs";

test("forwards only the question and returns a name, clarification, or service error", async t => {
    const calls = [];
    const server = createInterpretationServer({
        apiKey: "test-key",
        fetchResponse: async (url, options) => {
            const request = JSON.parse(options.body);
            calls.push({ url, authorization: options.headers.Authorization, request });
            if (request.input === "Unavailable") return new Response(null, { status: 429 });
            const name = request.input.includes("Smith")
                ? { status: "lookup", firstName: "Jane", lastName: "Smith" }
                : { status: "clarify", firstName: null, lastName: null };
            return Response.json({
                status: "completed",
                output: [{
                    type: "message",
                    role: "assistant",
                    content: [{ type: "output_text", text: JSON.stringify(name) }],
                }],
            });
        },
    });
    server.listen(0, "127.0.0.1");
    t.after(() => {
        server.close();
        server.closeAllConnections();
    });
    await once(server, "listening");
    const send = body => fetch(`http://127.0.0.1:${server.address().port}/interpret`, {
        method: "POST",
        body: JSON.stringify(body),
    });

    const question = "What's Jane Smith's number?";
    let response = await send({ question });
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), { status: "lookup", firstName: "Jane", lastName: "Smith" });
    assert.equal(calls[0].url, "https://api.openai.com/v1/responses");
    assert.equal(calls[0].authorization, "Bearer test-key");
    assert.equal(calls[0].request.input, question);
    assert.equal(calls[0].request.store, false);
    assert.equal(calls[0].request.text.format.strict, true);

    response = await send({ question: "What's Jane's number?" });
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), { status: "clarify" });

    response = await send({ question: "Unavailable" });
    assert.equal(response.status, 502);
    assert.deepEqual(await response.json(), { error: "Interpretation unavailable" });

    response = await send({ question, entries: [] });
    assert.equal(response.status, 400);
    response = await send({ question: "x".repeat(9_000) });
    assert.equal(response.status, 413);
    assert.equal(calls.length, 3);
});
