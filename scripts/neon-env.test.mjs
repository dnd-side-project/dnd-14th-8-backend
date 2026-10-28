import assert from "node:assert/strict";
import { test } from "node:test";
import { parseEnv } from "node:util";
import { jdbcEnvironment, mergeJdbcEnvironment } from "./neon-env.mjs";

const url = "postgresql://user:secret@ep-example.ap-southeast-1.aws.neon.tech/moyeorak?sslmode=require";

test("converts direct Neon variables into JDBC without embedding credentials in the URL", () => {
  const env = jdbcEnvironment({ DATABASE_URL_UNPOOLED: url, DATABASE_URL: "unused pooled URL" });
  assert.equal(env.DB_USERNAME, "user");
  assert.equal(env.DB_PASSWORD, "secret");
  assert.equal(env.DB_JDBC_URL, "jdbc:postgresql://ep-example.ap-southeast-1.aws.neon.tech/moyeorak?sslmode=require&channelBinding=require&connectTimeout=10");
  assert.ok(!env.DB_JDBC_URL.includes("secret"));
});

test("decodes escaped credentials and preserves API keys and comments when synchronizing", () => {
  const original = `# Keep me\nKAKAO_API_KEY="a#b=c"\nDB_USERNAME=old\nDB_PASSWORD=old\nDATABASE_URL_UNPOOLED='${url.replace("user:secret", "u%40ser:a%23%20b")}'\n`;
  const updated = mergeJdbcEnvironment(original);
  const env = parseEnv(updated);
  assert.equal(env.KAKAO_API_KEY, "a#b=c");
  assert.equal(env.DB_USERNAME, "u@ser");
  assert.equal(env.DB_PASSWORD, "a# b");
  assert.ok(updated.startsWith('# Keep me\nKAKAO_API_KEY="a#b=c"\n'));
  assert.equal(mergeJdbcEnvironment(updated), updated);
});

test("rejects missing, pooled, malformed, or non-Postgres URLs without printing secrets", () => {
  for (const raw of [undefined, "secret-invalid", url.replace("ep-example.", "ep-example-pooler."), url.replace("postgresql:", "https:")]) {
    assert.throws(() => jdbcEnvironment({ DATABASE_URL_UNPOOLED: raw }), error => !error.message.includes("secret"));
  }
});
